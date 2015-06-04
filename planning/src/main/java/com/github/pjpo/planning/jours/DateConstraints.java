package com.github.pjpo.planning.jours;

import java.io.IOException;
import java.io.InputStream;
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

public class DateConstraints {

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
