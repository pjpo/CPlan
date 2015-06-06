package com.github.pjpo.planning.ui.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;

import com.github.pjpo.planning.dao.DaoPhysician;
import com.github.pjpo.planning.dao.DaoPositionCode;
import com.github.pjpo.planning.model.Physician;
import com.github.pjpo.planning.model.PositionCode;
import com.github.pjpo.planning.ui.PlanningMainUIApp;

public class RootLayoutController {

	private PlanningMainUIApp mainApp;

	@FXML
	private void initialize() {
	}

	public void setMainApp(PlanningMainUIApp mainApp) {
		this.mainApp = mainApp;
	}

	@FXML
	public void handleSaveConfiguration() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");

		File saveFile = fileChooser.showSaveDialog(mainApp.getPrimaryStage());
		if (saveFile != null) {
			try {
				saveConfiguration(saveFile);
			} catch (IOException e) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Information");
				alert.setHeaderText("Erreur");
				alert.setContentText(e.getMessage());
				alert.showAndWait();
			}
		}
	}

	public void saveConfiguration(File file) throws IOException {

		// Deletes the defined file
		try {
			Files.delete(file.toPath());
		} catch (NoSuchFileException e) {
			// Do nothing, the file will be created
		}
		
		// Select the zip file to write into
		final URI uri = URI.create("jar:file:" + file.toURI().getPath());
		
		// Select the env parameters
		final Map<String, String> env = new HashMap<>();
		env.put("create", "true");
		
		try (final FileSystem zipFile = FileSystems.newFileSystem(uri, env)) {
			final Path physicians = zipFile.getPath("/", "physicians");
			try (final BufferedWriter writer = Files.newBufferedWriter(
					physicians, Charset.forName("UTF-8"),
					StandardOpenOption.WRITE,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING)) {
				final DaoPhysician daoPhysician = new DaoPhysician(writer);
				for (final Physician physician : mainApp.getPhysicians()) {
					daoPhysician.store(physician);
				}
			}
		}

		try (final FileSystem zipFile = FileSystems.newFileSystem(uri, env)) {
			final Path positionsCodesFile = zipFile.getPath("/", "positions");
			try (final BufferedWriter writer = Files.newBufferedWriter(
					positionsCodesFile, Charset.forName("UTF-8"),
					StandardOpenOption.WRITE,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING)) {
				final DaoPositionCode daoPositionCode = new DaoPositionCode(writer);
				for (final PositionCode positionCode : mainApp.getPositions()) {
					daoPositionCode.store(positionCode);
				}
			}
		}
}

	@FXML
	public void handleLoadConfiguration() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");

		File saveFile = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
		if (saveFile != null) {
			try {
				loadConfiguration(saveFile);
			} catch (IOException e) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Information");
				alert.setHeaderText("Erreur");
				alert.setContentText(e.getMessage());
				alert.showAndWait();
			}
		}
	}

	public void loadConfiguration(File file) throws IOException {
		
		// Path de l'archive
		final Path zipFile = Paths.get(file.toURI());
		// Création d'une instance de FileSystem pour gérer les zip
		final FileSystem fs = FileSystems.newFileSystem(zipFile, null);
		
		// Récupération du fichier des physicians dans l'archive
		final Path physiciansFile = fs.getPath("/", "physicians");

		// READED LIST OF PHSYCICIANS
		final LinkedList<Physician> physicians = new LinkedList<>();

		// lecture du fichier contenu dans l'archive
		try (final BufferedReader reader = Files.newBufferedReader(
				physiciansFile, Charset.forName("UTF-8"))) {
			
			final DaoPhysician daoPhysician = new DaoPhysician(reader);

			Physician readedPhysician = null;
			
			while ((readedPhysician = daoPhysician.load()) != null) {
				physicians.add(readedPhysician);
			}
		}
		
		// READING WAS FINE, CHANGE DATAS IN tHE UI
		mainApp.getPhysicians().clear();
		for (Physician physician : physicians) {
			mainApp.getPhysicians().add(physician);
		}

		// Récupération du fichier des positions dans l'archive
		final Path positionsFile = fs.getPath("/", "positions");

		// READED LIST OF POSITIONS CODES
		final LinkedList<PositionCode> positionCodes = new LinkedList<>();
		
		try (final BufferedReader reader = Files.newBufferedReader(
				positionsFile, Charset.forName("UTF-8"))) {
			
			final DaoPositionCode daoPositionCode = new DaoPositionCode(reader);

			PositionCode readedPositionCode = null;
			
			while ((readedPositionCode = daoPositionCode.load()) != null) {
				positionCodes.add(readedPositionCode);
			}
		}

		// READING WAS FINE, CHANGE DATAS IN tHE UI
		mainApp.getPositions().clear();
		for (PositionCode positionCode : positionCodes) {
			mainApp.getPositions().add(positionCode);
		}

	}

}
