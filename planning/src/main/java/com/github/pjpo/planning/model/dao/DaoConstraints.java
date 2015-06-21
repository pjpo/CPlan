package com.github.pjpo.planning.model.dao;

import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import com.github.pjpo.planning.model.PositionConstraintBase;

/**
 * Transforms the code of constraints written in pseudo-langage into java mode
 * @author jp@dm.lan
 *
 */
public class DaoConstraints {

	private final String code;
	
	/**
	 * Sets the source code
	 * @param code
	 */
	public DaoConstraints(final String code) {
		this.code = code;
	}
	
	/**
	 * Transforms the code into java model
	 * @return list of constraints objects
	 */
	public List<PositionConstraintBase> load() {
		final ANTLRInputStream ais = new ANTLRInputStream(code);
		final ExprLexer l = new ExprLexer(ais);
		final ExprParser p = new ExprParser(new CommonTokenStream(l));

		p.addErrorListener(
				new BaseErrorListener() {
					@Override
					public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
						throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
					}
				});

		final ExprCPlanListener parserListener = new ExprCPlanListener();
		p.addParseListener(parserListener);
		p.file();

		return parserListener.getConstraints();
	}
	

}
