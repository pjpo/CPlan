package com.github.pjpo.planning.jours;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.RecognitionException;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

import com.github.pjpo.planning.lignes.Position;

public class DateConstraints {

	
	public static HashMap<String, Position> getPositions(LocalDate date) {
		
		HashMap<String, Position> positions = new HashMap<>();
		
		// GET POSITIONS DEFINITIONS AND GENERATE A LIST OF ACCEPTABLE POSITIONS FOR THIS LOCALDATE
		try (
				InputStream resource = DateConstraints.class.getResourceAsStream("/com/github/pjpo/planning/positions.cfg");
				InputStreamReader isr = new InputStreamReader(resource);
				BufferedReader br = new BufferedReader(isr);) {
			String positionName = null;
			String separator = null;
			StringBuilder script = new StringBuilder();
			String readed = null;
			while ((readed = br.readLine()) != null) {
				// positionName not defined => define it
				if (positionName == null) {
					positionName = readed;
				}
				// positionName has been defined ; if separator has not been defined, define it
				else if (separator == null) {
					separator = readed;
				}
				// positionName and separator have been defined, see if we reached end of script
				else if (readed.equals(separator)) {
					// We have a new position defined, store it if position is active
					Position position = new Position(positionName, date, script.toString());
					if (position.isWorking()) positions.put(positionName, position);
					// Clean state
					positionName = separator = null;
					script = new StringBuilder();
				}
				// WE ARE IN SCRIPT, STORE IT
				else {
					script.append(readed);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return positions;
	}

	/**
	 * Get the constraints for the Day date, with workers defined in workers. workers contains the conditions for each worker.
	 * @param date
	 * @param workers
	 * @return
	 */
	public static List<Constraint> getConstraints(
			LocalDate date,
			HashMap<LocalDate, HashMap<String, IntVar>> workers) {

		try (
				final InputStream is = DateConstraints.class.getResourceAsStream("/com/github/pjpo/planning/conditions.cfg");
				) {
			ANTLRInputStream ais = new ANTLRInputStream(is);
			ExprLexer l = new ExprLexer(ais);
			ExprParser p = new ExprParser(new CommonTokenStream(l));
			p.addErrorListener(new BaseErrorListener() {
				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
					throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
				}
			});
			ExprCPlanListener parserListener = new ExprCPlanListener(workers, date);
			p.addParseListener(parserListener);
			p.file();
			return parserListener.getConstraints();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

}
