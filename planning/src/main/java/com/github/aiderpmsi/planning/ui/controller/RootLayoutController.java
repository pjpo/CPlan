package com.github.aiderpmsi.planning.ui.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;

import org.controlsfx.dialog.Dialogs;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;

import com.github.aiderpmsi.planning.physician.Physician;
import com.github.aiderpmsi.planning.physician.PhysicianBuilder;
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

	 public void handleLoadConfiguration() {
		 FileChooser fileChooser = new FileChooser();
		 fileChooser.setTitle("Open Resource File");
		 
		 File saveFile = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
		 if (saveFile != null) {
			 try {
				 loadConfiguration(saveFile);
			 } catch (IOException e) {
				 Dialogs.create()
				 .owner(mainApp.getPrimaryStage())
				 .showException(e);
			 }
		 }
	 }

	 public void loadConfiguration(File file) throws IOException {
		 Path saveFile = Paths.get(file.toURI());
		 
		 // READED LIST OF PHSYCICIANS
		 LinkedList<Physician> physicians = new LinkedList<>();
		 
		 try (BufferedReader reader = Files.newBufferedReader(
				 saveFile, Charset.forName("UTF-8"))) {
			 // READED LINE IN FILE
			 String readedLine = null;
			 
			 // IGNORE UNTIL DOCS DEFINITION
			 while ((readedLine = reader.readLine()) != null && !readedLine.equals("01:docs")) {}

			 // READ DOCS DEFINITIONS
			 PhysicianBuilder tempPhysBuilder = new PhysicianBuilder();
			 while ((readedLine = reader.readLine()) != null && !readedLine.startsWith("01:plages")) {
				 if (readedLine.startsWith("02:")) {
					 tempPhysBuilder.setName(readedLine.substring(3));
				 } else if (readedLine.startsWith("03:")) {
					 tempPhysBuilder.setTimePart(Integer.decode(readedLine.substring(3)));
					 physicians.add(tempPhysBuilder.toPhysician());
					 tempPhysBuilder = new PhysicianBuilder();
				 }
				 ;
			 }
		 }

		 // READING WAS FINE, CHANGE DATAS IN tHE UI
		 mainApp.getPhysicians().clear();
		 for (Physician physician : physicians) {
			 mainApp.getPhysicians().add(physician);
		 }
	 }

}
