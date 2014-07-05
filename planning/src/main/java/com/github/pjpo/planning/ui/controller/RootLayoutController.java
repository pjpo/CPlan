package com.github.pjpo.planning.ui.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.controlsfx.dialog.Dialogs;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;

import com.github.pjpo.planning.physician.Physician;
import com.github.pjpo.planning.physician.PhysicianBuilder;
import com.github.pjpo.planning.ui.PlanningMainUIApp;
import com.github.pjpo.planning.utils.Interval;

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
			 writer.append("01:docs");
			 for (Physician physician : mainApp.getPhysicians()) {
				 writer.append("\n02:");
				 writer.append(physician.getName() == null ? "N" : ":" + physician.getName());
				 writer.append("\n03:");
				 writer.append(physician.getTimePart() == null ? "N" : ":" + physician.getTimePart().toString());
				 writer.append("\n04:");
				 writer.append(physician.getWorkStart() == null ? "N" : ":" + physician.getWorkStart().toString());
				 writer.append("\n05:");
				 writer.append(physician.getWorkEnd() == null ? "N" : ":" + physician.getWorkEnd().toString());
				 for (Interval interval : physician.getPaidVacation()) {
					 writer.append("\n06:");
					 writer.append(interval.getStart() == null ? "N" : interval.getStart().toString());
					 writer.append(':');
					 writer.append(interval.getEnd() == null ? "N" : interval.getEnd().toString());
				 }
				 for (Interval interval : physician.getUnpaidVacation()) {
					 writer.append("\n07:");
					 writer.append(interval.getStart() == null ? "N" : interval.getStart().toString());
					 writer.append(':');
					 writer.append(interval.getEnd() == null ? "N" : interval.getEnd().toString());
				 }
				 for (Entry<LocalDate, ArrayList<String>> entry : physician.getWorkedVacs().entrySet()) {
					 for (String poste : entry.getValue()) {
						 writer.append("\n08:");
						 writer.append(entry.getKey().toString());
						 writer.append(':');
						 writer.append(poste);
					 }
				 }
				 writer.append("\n99:END");
			 }
			 writer.append('\n');
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
			 PhysicianBuilder physicianBuilder = new PhysicianBuilder();
			 while ((readedLine = reader.readLine()) != null && !readedLine.startsWith("01:plages")) {
				 if (readedLine.startsWith("02:") && readedLine.charAt(3) == ':') {
					 physicianBuilder.setName(readedLine.substring(4));
				 } else if (readedLine.startsWith("03:") && readedLine.charAt(3) == ':') {
					 physicianBuilder.setTimePart(Integer.decode(readedLine.substring(4)));
				 } else if (readedLine.startsWith("04:") && readedLine.charAt(3) == ':') {
					 physicianBuilder.setWorkStart(LocalDate.parse(readedLine.substring(4)));
				 } else if (readedLine.startsWith("05:") && readedLine.charAt(3) == ':') {
					 physicianBuilder.setWorkEnd(LocalDate.parse(readedLine.substring(4)));
				 } else if (readedLine.startsWith("06:")) {
					 int splitPosition = readedLine.indexOf(':', 3);
					 String start = readedLine.substring(3, splitPosition);
					 String end = readedLine.substring(splitPosition + 1);
					 physicianBuilder.addPaidVacation(new Interval(
							 start.equals("N") ? null : LocalDate.parse(start),
							 end.equals("N") ? null : LocalDate.parse(end)));
				 } else if (readedLine.startsWith("07:")) {
					 int splitPosition = readedLine.indexOf(':', 3);
					 String start = readedLine.substring(3, splitPosition);
					 String end = readedLine.substring(splitPosition + 1);
					 physicianBuilder.addUnpaidVacation(new Interval(
							 start.equals("N") ? null : LocalDate.parse(start),
							 end.equals("N") ? null : LocalDate.parse(end)));
				 } else if (readedLine.startsWith("08:")) {
					 int splitPosition = readedLine.indexOf(':', 3);
					 LocalDate date = LocalDate.parse(readedLine.substring(3, splitPosition));
					 String poste = readedLine.substring(splitPosition + 1);
					 physicianBuilder.addWorkedVac(date, poste);
				 } else if (readedLine.equals("99:END")) {
					 physicians.add(physicianBuilder.toPhysician());
					 physicianBuilder = new PhysicianBuilder();
				 }
			 }
		 }

		 // READING WAS FINE, CHANGE DATAS IN tHE UI
		 mainApp.getPhysicians().clear();
		 for (Physician physician : physicians) {
			 mainApp.getPhysicians().add(physician);
		 }
	 }

}
