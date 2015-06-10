package com.github.pjpo.planning.model.dao;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import com.github.pjpo.planning.model.dao.ExprLexer;
import com.github.pjpo.planning.model.dao.ExprParser;
import com.github.pjpo.planning.model.PositionConstraintBase;

public class DaoConstraints {

	@SuppressWarnings("unused")
	private final BufferedWriter writer; 

	private final InputStream input;
	
	public DaoConstraints(final BufferedWriter writer) {
		this.writer = writer;
		this.input = null;
	}
	
	public DaoConstraints(final InputStream input) {
		this.input = input;
		this.writer = null;
	}
	
	public List<PositionConstraintBase> load() throws IOException {
		final ANTLRInputStream ais = new ANTLRInputStream(input);
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
