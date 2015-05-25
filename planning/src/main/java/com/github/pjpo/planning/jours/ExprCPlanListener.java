package com.github.pjpo.planning.jours;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.chocosolver.solver.constraints.Arithmetic;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.binary.PropEqualX_Y;
import org.chocosolver.solver.constraints.nary.alldifferent.AllDifferent;
import org.chocosolver.solver.constraints.unary.PropEqualXC;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.impl.BitsetIntVarImpl;
import org.chocosolver.solver.variables.impl.FixedIntVarImpl;
import org.chocosolver.util.iterators.DisposableValueIterator;

public class ExprCPlanListener extends ExprBaseListener {

	final private List<Constraint> constraints = new LinkedList<Constraint>();
	
	final private HashMap<LocalDate, HashMap<String, IntVar>> positionsIndices;
	
	final private LocalDate currentDate;
	
	public ExprCPlanListener(
			HashMap<LocalDate, HashMap<String, IntVar>> positionsIndices,
			LocalDate currentDate) {
		this.positionsIndices = positionsIndices;
		this.currentDate = currentDate;
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * When exiting equality, we have to add these constraints to solver
	 */
	@Override public void exitEquality(ExprParser.EqualityContext ctx) {
		List<IntVar> existingPositionsIndices = getExistingIndices(ctx.rules.rules);
		IntVar lastIndice = null;
		for (IntVar indice : existingPositionsIndices) {
			if (lastIndice == null) {
				lastIndice = indice;
			} else {
				constraints.add(IntConstraintFactory.arithm(lastIndice, "=", indice));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * When exiting differences, we have to add these constraints to solver
	 */
	@Override public void exitDifference(ExprParser.DifferenceContext ctx) {
		List<IntVar> existingPositionsIndices = getExistingIndices(ctx.rules.rules);
		if (existingPositionsIndices.size() >= 2)
			constraints.add(IntConstraintFactory.alldifferent(existingPositionsIndices.toArray(new IntVar[existingPositionsIndices.size()])));
	}

	/**
	 * Lists the existing positions in positionsIndices that are defined by rules
	 * @param equality
	 * @param rules
	 */
	private List<IntVar> getExistingIndices(List<ChocoRule> rules) {
		List<IntVar> existingPositionsIndices = new LinkedList<>();
		rule : for (ChocoRule rule : rules) {
			// Get date reference or this rule
			HashMap<String, IntVar> subPositionsIndices =
					positionsIndices.get(currentDate.plusDays(rule.getValue()));
			// If nothing defined, go to next element
			if (subPositionsIndices == null)
				continue rule;
			// Get reference of this position
			IntVar indice = subPositionsIndices.get(rule.getName());
			// If no reference is defined, go to next element
			if (indice == null)
				continue rule;
			// This element exists, store it in the existingPositionsIndices
			existingPositionsIndices.add(indice);
		}
		return existingPositionsIndices;
	}		

	public List<Constraint> getConstraints() {
		System.out.println("<<< Date : " + currentDate.toString() + " >>>");
		for (Constraint cstr : constraints) {
			if (cstr instanceof AllDifferent) {
				AllDifferent cstrC = (AllDifferent) cstr;
				Propagator<?> propagators[] = cstrC.getPropagators();
				if (propagators.length != 0) {
					System.out.println("=== Different : ");
					for (Variable var : propagators[0].getVars()) {
						System.out.println(debugGetAllDefined(var));
					}
					System.out.println("================");
				}
			} else if (cstr instanceof Arithmetic) {
				Arithmetic cstrC = (Arithmetic) cstr;
				Propagator<?> propagators[] = cstrC.getPropagators();
				if (propagators.length != 0) {
					if (propagators[0] instanceof PropEqualXC || propagators[0] instanceof PropEqualX_Y) {
						System.out.println("=== Equals : ");
					} else {
						System.out.println("=== " + propagators[0].getClass());
					}
					for (Variable var : propagators[0].getVars()) {
						System.out.println(debugGetAllDefined(var));
					}
					System.out.println("================");
				}
			} else {
				System.out.println(cstr.toString());
			}
		}
		System.out.println("<<< >>>");
		return constraints;
	}
	
	private String debugGetAllDefined(Variable var) {
		StringBuilder ret = new StringBuilder("- " + var.getName());
		if (var instanceof BitsetIntVarImpl) {
			DisposableValueIterator it = ((BitsetIntVarImpl) var).getValueIterator(true);
			ret.append("{");
			while (it.hasNext()) {
				int value = it.next();
				ret.append(" ").append(value);
			}
			ret.append(" }");
		} else if (var instanceof FixedIntVarImpl) {
			DisposableValueIterator it = ((FixedIntVarImpl) var).getValueIterator(true);
			ret.append("{");
			while (it.hasNext()) {
				int value = it.next();
				ret.append(" ").append(value);
			}
			ret.append(" }");
		}
		return ret.toString();
	}
}
