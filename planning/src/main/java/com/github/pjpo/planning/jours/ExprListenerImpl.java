package com.github.pjpo.planning.jours;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.pjpo.planning.jours.ExprParser.Any_except_newlineContext;
import com.github.pjpo.planning.jours.ExprParser.CommentContext;
import com.github.pjpo.planning.jours.ExprParser.ConcatenationContext;
import com.github.pjpo.planning.jours.ExprParser.ContentContext;
import com.github.pjpo.planning.jours.ExprParser.Content_indexContext;
import com.github.pjpo.planning.jours.ExprParser.DifferenceContext;
import com.github.pjpo.planning.jours.ExprParser.EqualityContext;
import com.github.pjpo.planning.jours.ExprParser.FileContext;
import com.github.pjpo.planning.jours.ExprParser.IndexnegContext;
import com.github.pjpo.planning.jours.ExprParser.IndexposContext;
import com.github.pjpo.planning.jours.ExprParser.NoindexContext;


public class ExprListenerImpl implements ExprListener {

	@Override
	public void visitTerminal(TerminalNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitErrorNode(ErrorNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterFile(FileContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitFile(FileContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterConcatenation(ConcatenationContext ctx) {
	}

	@Override
	public void exitConcatenation(ConcatenationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterNoindex(NoindexContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitNoindex(NoindexContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterAny_except_newline(Any_except_newlineContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitAny_except_newline(Any_except_newlineContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterContent_index(Content_indexContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitContent_index(Content_indexContext ctx) {
		System.out.println("Hello");
		
	}

	@Override
	public void enterIndexpos(IndexposContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitIndexpos(IndexposContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterDifference(DifferenceContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitDifference(DifferenceContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterIndexneg(IndexnegContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitIndexneg(IndexnegContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterComment(CommentContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitComment(CommentContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterEquality(EqualityContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitEquality(EqualityContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterContent(ContentContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitContent(ContentContext ctx) {
		// TODO Auto-generated method stub
		
	}

}
