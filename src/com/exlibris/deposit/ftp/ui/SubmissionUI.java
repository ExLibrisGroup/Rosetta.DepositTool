package com.exlibris.deposit.ftp.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.exlibris.core.infra.common.shared.dataObjects.KeyValuePair;
import com.exlibris.deposit.ftp.DepositProperties;
import com.exlibris.deposit.ftp.Depositor;
import com.exlibris.deposit.ftp.FTPUploader;
import com.exlibris.deposit.ftp.DCCreator;
import com.exlibris.deposit.ftp.LogObject;

public class SubmissionUI extends LogObject {

	private final Display display = Display.getDefault();
	private final Shell shell = new Shell(display, SWT.SHELL_TRIM & (~SWT.RESIZE));
	
	private final Color colorWhite = new Color(null, new RGB(255, 255, 255));

	private void addTableItem(String filename, Table fileTable) {
		File file = new File(filename);
		if ((file.exists()) && (file.isFile())) {

			boolean exists = false;
			for (TableItem item : fileTable.getItems()) {
				if (item.getData("filename").toString().equals(file.getAbsolutePath())) {
					exists = true;
				}
			}

			if (!exists) {
				TableItem tableItem = new TableItem(fileTable, SWT.NONE);
				tableItem.setText(0, file.getName());
				tableItem.setText(1, file.getParent());
				tableItem.setData("filename", file.getAbsolutePath());
			}
		}

	}

	public SubmissionUI(String[] filenames) {

		int totalHeight = (MetadataProperties.getMetadataFields().size() * 27) + 400;

		// set shell size
		final int screenMidWidth = display.getBounds().width / 2;
		final int screenMidHeight = display.getBounds().height / 2;

		shell.setBounds(screenMidWidth - 230, screenMidHeight - (totalHeight / 2), 460, totalHeight);
		shell.setText(UILabels.getLabel("title"));
		shell.setImage(new Image(display, System.getProperty("user.dir") + "/" + "favicon.ico"));
		shell.setBackground(colorWhite);

		Text text;
		Label label;

		int height = 20;
		for (KeyValuePair<String, String> entry : MetadataProperties.getMetadataFields()) {
			label = new Label(shell, SWT.FLAT);
			label.setText(MetadataProperties.getValue(entry.getKey()));
			label.setBounds(10, height, 65, 20);
			label.setBackground(colorWhite);
			label.setAlignment(SWT.LEFT);

			text = new Text(shell, SWT.BORDER);		
			text.setText(entry.getValue());
						
			if(Objects.equals(entry.getKey(),"dc:date") && Objects.equals(entry.getValue(), "")){
				text.setText(new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()));
			}
			
			text.setBounds(85, height, 355, 20);
			text.setData("field", entry.getKey());
						
			height += 27;
		}

		// Enable a table as a Drop Target
		final Table fileTable = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);

		fileTable.setBounds(10, height + 30, 430, 204);
		fileTable.setHeaderVisible(true);
		fileTable.setLinesVisible(true);

		TableColumn column1 = new TableColumn(fileTable, SWT.NONE);
		column1.setText(UILabels.getLabel("filename"));
		column1.setWidth(150);

		TableColumn column2 = new TableColumn(fileTable, SWT.NONE);
		column2.setText(UILabels.getLabel("folder"));
		column2.setWidth(255);

		fileTable.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 127) {
					fileTable.remove(fileTable.getSelectionIndices());
				}
			}
		});

		DropTarget dt = new DropTarget(fileTable, DND.DROP_DEFAULT | DND.DROP_MOVE);
		dt.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		dt.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event) {
				String fileList[] = null;
				FileTransfer ft = FileTransfer.getInstance();
				if (ft.isSupportedType(event.currentDataType)) {
					fileList = (String[]) event.data;
				}

				if ((fileList != null) && (fileList.length > 0)) {
					for (String filename : fileList) {
						addTableItem(filename, fileTable);
					}
				}
			}
		});

		Button submitButton = new Button(shell, SWT.NONE);
		
		Button addButton = new Button(shell, SWT.NONE);
		addButton.setText(UILabels.getLabel("add.files"));
		addButton.setBounds(135, height + 245, 90, 25);
		addButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent arg0) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
				fd.setText(UILabels.getLabel("open"));
				fd.setFilterPath(DepositProperties.getValue(DepositProperties.UI_ROOT_FOLDER));
				String[] filterExt = {"*.*"};
				fd.setFilterExtensions(filterExt);
				fd.open();

				String path = fd.getFilterPath();
				for (String name : fd.getFileNames()) {
					addTableItem(path + File.separator + name, fileTable);
				}
				if (!Objects.equals(path, ""))
					submitButton.setEnabled(true);				
			}
		});

		Button removeButton = new Button(shell, SWT.NONE);
		removeButton.setText(UILabels.getLabel("remove"));
		removeButton.setBounds(235, height + 245, 90, 25);
		removeButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				fileTable.remove(fileTable.getSelectionIndices());
				
				if(fileTable.getItemCount() == 0)
					submitButton.setEnabled(false);
			}
		});

		FontData fontData = new FontData();
		fontData.setStyle(SWT.BOLD);
		fontData.setHeight(10);

		// After Submission UI
		final Label loadingLabel = new Label(shell, SWT.NONE | SWT.CENTER);
		loadingLabel.setText("Deposit Status Log");
		loadingLabel.setBounds(10, 15, 430, 20);
		loadingLabel.setBackground(colorWhite);
		loadingLabel.setFont(new Font(null, fontData));
		loadingLabel.setVisible(false);
		loadingLabel.setData("submit", true);

		final Text logText = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.READ_ONLY | SWT.WRAP);
		
		logText.setBounds(10, 40, 430, totalHeight - 150);
		logText.setVisible(false);
		logText.setData("submit", true);

		final Button exitButton = new Button(shell, SWT.NONE);
		exitButton.setText("Exit");
		exitButton.setBounds(170, height + 290, 120, 35);
		exitButton.setFont(new Font(null, fontData));
		exitButton.setEnabled(false);
		exitButton.setVisible(false);
		exitButton.setData("submit", true);
		exitButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent w) {
				shell.dispose();
			}
		});

		submitButton.setEnabled(false);
		submitButton.setText(UILabels.getLabel("submit"));
		submitButton.setBounds(170, height + 290, 120, 35);
		submitButton.setFont(new Font(null, fontData));
		submitButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent event) {
				if (fileTable.getItemCount() > 0) {
					List<String> filenames = new LinkedList<String>();
					for (TableItem item : fileTable.getItems()) {
						filenames.add(item.getData("filename").toString());
					}

					List<KeyValuePair<String, String>> metadata = new LinkedList<KeyValuePair<String, String>>();
					for (Control control : shell.getChildren()) {
						if ((control.getClass() == Text.class) && (control.getData("field") != null)){
							Text text = (Text) control;
							metadata.add(new KeyValuePair<String, String>(
									text.getData("field").toString(), text.getText()));
						}
					}

					try {

						setLog(logText);
						for (Control control : shell.getChildren()) {
							if (control.getData("submit") == null) {
								control.dispose();
							} else {
								control.setVisible(true);
							}
						}
						shell.update();


						DCCreator creator = new DCCreator();
						creator.setLog(logText);
						creator.createIE(filenames, metadata);

						FTPUploader uploader = new FTPUploader();
						uploader.setLog(logText);
						String depositDirectory = uploader.upload();

						Depositor depositor = new Depositor();
						depositor.setLog(logText);
						depositor.Deposit(depositDirectory);


					} catch (Exception e) {
						log("Error occurred: " + e.getMessage());
						e.printStackTrace();
					}

					exitButton.setEnabled(true);
				}
			}
		});

		if ((filenames != null) && (filenames.length > 0)) {
			for (String filename : filenames) {
				addTableItem(filename, fileTable);
			}
		}

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}

		display.dispose();
	}

	public static void main(String[] args) {
		new SubmissionUI(args);
	}
}
