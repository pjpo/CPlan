package com.github.aiderpmsi.planning.ui.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.controlsfx.dialog.Dialogs;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;

import com.github.aiderpmsi.planning.physician.Physician;
import com.github.aiderpmsi.planning.ui.PlanningMainUIApp;

public class RootLayoutController {

	private PlanningMainUIApp mainApp;
	
	@FXML
	private void initialize() {
	}
	
	public void setMainApp(PlanningMainUIApp mainApp) {
		this.mainApp = mainApp;
	}
	 
	 public void handleSaveConfiguration() {
		 FileChooser fileChooser = new FileChooser();
		 fileChooser.setTitle("Open Resource File");
		 
		 File saveFile = fileChooser.showSaveDialog(mainApp.getPrimaryStage());
		 if (saveFile != null) {
			 try {
				 saveConfiguration(saveFile);
			 } catch (IOException e) {
				 Dialogs.create()
				 .owner(mainApp.getPrimaryStage())
				 .showException(e);
			 }
		 }
	 }
    
	 public void saveConfiguration(File file) throws IOException {
		 Path saveFile = Paths.get(file.toURI());
		 try (BufferedWriter writer = Files.newBufferedWriter(
				 saveFile, Charset.forName("UTF-8"),
				 StandardOpenOption.WRITE,
				 StandardOpenOption.CREATE,
				 StandardOpenOption.TRUNCATE_EXISTING)) {
			 // WRITE MEDS
			 writer.append("01:docs\n");
			 for (Physician physician : mainApp.getPhysicians()) {
				 writer.append("02:");
				 writer.append(physician.getName());
				 writer.append("\n03:");
				 writer.append(physician.getTimePart().toString());
				 writer.append('\n');
			 }
		 }
	 }
}
