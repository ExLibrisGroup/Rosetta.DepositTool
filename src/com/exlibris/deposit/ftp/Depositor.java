package com.exlibris.deposit.ftp;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.exlibris.core.infra.common.shared.dataObjects.KeyValuePair;
import com.exlibris.digitool.deposit.service.xmlbeans.DepositDataDocument;
import com.exlibris.digitool.deposit.service.xmlbeans.DepositResultDocument;
import com.exlibris.digitool.deposit.service.xmlbeans.DepositDataDocument.DepositData;
import com.exlibris.digitool.deposit.service.xmlbeans.DepositResultDocument.DepositResult;
import com.exlibris.dps.DepositWebServices_Service;
import com.exlibris.dps.DepositWebServices;
import com.exlibris.dps.ProducerWebServices;
import com.exlibris.dps.ProducerWebServices_Service;
import com.exlibris.dps.sdk.pds.HeaderHandlerResolver;

public class Depositor extends LogObject {

	private static final String DEPOSIT_WSDL_URL = "DepositWebServices?wsdl";
	private static final String PRODUCER_WSDL_URL = "ProducerWebServices?wsdl";

	public void Deposit(String depositDirectory, String username, String password) throws Exception {

		logTitle("STEP 3 - EXECUTING A DEPOSIT ACTIVITY");

		// Get Deposit webservice handle
		log("Connecting to the deposit web services");

		DepositWebServices_Service dpws = null;
		try {
			dpws = new DepositWebServices_Service(new URL(DepositProperties.getValue(DepositProperties.DEPOSIT_URL) + DEPOSIT_WSDL_URL),
					new QName("http://dps.exlibris.com/", "DepositWebServices"));
			dpws.setHandlerResolver(new HeaderHandlerResolver(username, password, DepositProperties.getValue(DepositProperties.INSTITUTION)));
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		// Get Producer webservice handle
		log("Connecting to the producer management web services");
		ProducerWebServices_Service pws = null;
		try {
			pws = new ProducerWebServices_Service(new URL(DepositProperties.getValue(DepositProperties.BACKOFFICE_URL) + PRODUCER_WSDL_URL),
					new QName("http://dps.exlibris.com/", "ProducerWebServices"));
			pws.setHandlerResolver(new HeaderHandlerResolver(username, password, DepositProperties.getValue(DepositProperties.INSTITUTION)));
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//Get list of producers that the producerAgent is affiliated with
		log("Retrieving producer information");
		String producerAgentId = pws.getProducerWebServicesPort().getInternalUserIdByExternalId(username);
		String xmlReply = pws.getProducerWebServicesPort().getProducersOfProducerAgent(producerAgentId);
		DepositDataDocument depositDataDocument = DepositDataDocument.Factory.parse(xmlReply);
		DepositData depositData = depositDataDocument.getDepositData();

		String producerId = depositData.getDepDataArray(0).getId();

		log("Depositing content");
		String retval = dpws.getDepositWebServicesPort().submitDepositActivity(
				null, DepositProperties.getValue(DepositProperties.MATERIAL_FLOW), depositDirectory, producerId, "1");

		try {
			DepositResult result = DepositResultDocument.Factory.parse(retval).getDepositResult();
			System.out.println("\n\nDeposit Status\n==============");
			System.out.println("\nFinished successfully: " + !result.getIsError());
			if (result.getIsError()) {
				log("Deposit failed: " + result.getMessageDesc());
				logTitle("DEPOSIT FAILED");
			} else {
				log("Succesfully created a deposit activity");
				log("Deposit activity ID: " + result.getDepositActivityId());
				log("SIP ID: " + result.getSipId());
				logTitle("CONTENT WAS SUCCESSFULLY DEPOSITED !");
			}
		} catch (Exception e) {
			log("Error occurred: " + e.getMessage());
			System.out.println("Submit Deposit Result:\n" + retval);
		}
	}

}
