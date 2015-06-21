package com.github.pjpo.planning.ui.controller;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import com.github.pjpo.planning.model.dao.ExprLexer;
import com.github.pjpo.planning.model.dao.ExprParser;
import com.github.pjpo.planning.ui.PlanningMainUIApp;

public class ConstraintsOverviewController {

	@FXML
    private TextArea constraintsCode;

    /** Reference to the main application */
    @SuppressWarnings("unused")
	private PlanningMainUIApp mainApp;

	@FXML
    private void initialize() {
        // clear code
        showConstraintsCode(null);

    }
	
	public void setMainApp(PlanningMainUIApp mainApp) {
		this.mainApp = mainApp;
        // Add observable string to the code Area
		constraintsCode.textProperty().bindBidirectional(mainApp.getConstraintsCode());
	}
	
    private void showConstraintsCode(String code) {
    	constraintsCode.setText(code == null ? "" : code);
    }

    @FXML
    public void validateHandler() {

    	final ANTLRInputStream ais = new ANTLRInputStream(constraintsCode.getText());
		final ExprLexer l = new ExprLexer(ais);
		final ExprParser p = new ExprParser(new CommonTokenStream(l));

		p.addErrorListener(
				new BaseErrorListener() {
					@Override
					public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
						throw new IllegalArgumentException("failed to parse at line " + line + " due to " + msg, e);
					}
				});

		try {
			p.file();
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText("Test des contraintes");
			alert.setContentText("Contraintes valides");
			alert.showAndWait();
		}  catch (IllegalArgumentException e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText("Erreur dans les contraintes");
			alert.setContentText(e.getMessage());
			alert.showAndWait();
		}

    }
    
    public String getConstraintsCode() {
    	return constraintsCode.getText();
    }

}
