/*******************************************************************************
 * Copyright (c) 2020
 * AG Embedded Systems, University of Münster
 * SESE Software and Embedded Systems Engineering, TU Berlin
 * 
 * Authors:
 * 	Paula Herber
 * 	Sabine Glesner
 * 	Timm Liebrenz
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package simulink2dl.util.satisfiability;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.operator.formula.BooleanConstant;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.operator.formula.Negation;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.StringFormula;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.BracketTerm;
import simulink2dl.dlmodel.term.ExponentTerm;
import simulink2dl.dlmodel.term.MultiplicationTerm;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.StringTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.util.PluginLogger;

/**
 * 
 * Please follow the steps outlined in section "Build Process of Z3 DLL and JAR
 * files" of the linked article in order to build the necessary JAR. Apparently,
 * Python is no longer necessary now (2018Q1).
 * 
 * Assumes that Terms don't contain Formulas.
 * 
 * @see <a href=
 *      "https://concolic.wordpress.com/2016/11/26/z3-java-for-matlab/">z3-java-
 *      for-matlab</a>
 * @author nick
 *
 */
public class FormulaChecker {

	public enum ResultType {
		SATISFIABLE, UNSATISFIABLE, UNKNOWN, ERROR
	}

	/**
	 * Z3 needs a context to produce a solver for a query. Everything should be
	 * announced to the context.
	 */
	private Context Z3Context;

	public FormulaChecker() {
		this.Z3Context = new Context();
	}

	// Term //

	/**
	 * Expects a Term object that need not be split any further. The simulink2dl.dlmodel
	 * classes Constant, Variable, RealTerm and StringTerm represent such atomic
	 * Terms. Others might exist.
	 *
	 * Acts as a end point for conversion.
	 * 
	 * @param term
	 * @return
	 */
	private ArithExpr simulink2dlAtomToZ3ArithExpr(Term term) {
		if (term instanceof Constant) {
			return this.Z3Context.mkRealConst(((Constant) term).getName());
		} else if (term instanceof RealTerm) {
			return this.Z3Context.mkReal(((RealTerm) term).toString());
		} else if (term instanceof Variable) {
			return this.Z3Context.mkRealConst(((Variable) term).getName());
		} else if (term instanceof StringTerm) {
			return this.Z3Context.mkReal(((StringTerm) term).toString());
		} else {
			return this.Z3Context.mkRealConst(term.toString());
		}
	}

	/**
	 * Expects a non-Formula Term and converts it to an equivalent ArithExpr. It is
	 * assumed, that the descendants of the input Term do not contain Formulas.
	 * 
	 * This method is the entry point for converting mathematical terms like a, b,
	 * a+b, a-b, a*b, a/b, a^b...
	 * 
	 * @param term
	 * @return
	 */
	private ArithExpr simlink2dlTermToZ3ArithExpr(Term term) {
		// handle leafs
		if (term.isAtomic()) {
			return simulink2dlAtomToZ3ArithExpr(term);
		}

		// handle internal nodes
		if (term instanceof AdditionTerm) {
			return this.simulink2dlAdditionTermToZ3ArithExpr((AdditionTerm) term);
		} else if (term instanceof BracketTerm) {
			return this.simulink2dlBracketTermToZ3ArithExpr((BracketTerm) term);
		} else if (term instanceof ExponentTerm) {
			return this.simulink2dlExponentTermToZ3ArithExpr((ExponentTerm) term);
		} else if (term instanceof MultiplicationTerm) {
			return this.simulink2dlMultiplicationTermToZ3ArithExpr((MultiplicationTerm) term);
		} else { // default case
			PluginLogger.error("Can not convert " + term.getClass() + " to ArithExpr.");
			return null;
		}
	}

	/**
	 * Converts a simulink2dl AdditionTerm into a Z3 ArithExpr. See
	 * simulink2dlTermToZ3ArithExpr for more information.
	 * 
	 * @param addTerm
	 * @return
	 */
	private ArithExpr simulink2dlAdditionTermToZ3ArithExpr(AdditionTerm addTerm) {
		ArithExpr result = this.Z3Context.mkReal(0);
		for (Term addition : addTerm.getSummands()) {
			ArithExpr add = this.simlink2dlTermToZ3ArithExpr(addition);
			result = this.Z3Context.mkAdd(result, add);
		}

		for (Term subtraction : addTerm.getSubtrahends()) {
			ArithExpr sub = this.simlink2dlTermToZ3ArithExpr(subtraction);
			result = this.Z3Context.mkSub(result, sub);
		}
		return result;
	}

	/**
	 * Converts a simulink2dl BracketTerm into a Z3 ArithExpr. See
	 * simulink2dlTermToZ3ArithExpr for more information.
	 * 
	 * @param brackTerm
	 * @return
	 */
	private ArithExpr simulink2dlBracketTermToZ3ArithExpr(BracketTerm brackTerm) {
		// Z3 takes care of bracket precedence
		return this.simlink2dlTermToZ3ArithExpr(brackTerm.getInnerTerm());
	}

	/**
	 * Converts a simulink2dl ExponentTerm into a Z3 ArithExpr. See
	 * simulink2dlTermToZ3ArithExpr for more information.
	 * 
	 * @param expTerm
	 * @return
	 */
	private ArithExpr simulink2dlExponentTermToZ3ArithExpr(ExponentTerm expTerm) {
		ArithExpr base = this.simlink2dlTermToZ3ArithExpr(expTerm.getBase());
		ArithExpr exp = this.simlink2dlTermToZ3ArithExpr(expTerm.getExponent());
		return this.Z3Context.mkPower(base, exp);
	}

	/**
	 * Converts a simulink2dl MultiplicationTerm into a ArithExpr. See
	 * simulink2dlTermToZ3ArithExpr for more information.
	 * 
	 * @param multTerm
	 * @return
	 */
	private ArithExpr simulink2dlMultiplicationTermToZ3ArithExpr(MultiplicationTerm multTerm) {
		ArithExpr result = this.Z3Context.mkReal(1);
		for (Term factor : multTerm.getFactors()) {
			ArithExpr fac = this.simlink2dlTermToZ3ArithExpr(factor);
			result = this.Z3Context.mkMul(result, fac);
		}

		for (Term divisor : multTerm.getDivisors()) {
			ArithExpr div = this.simlink2dlTermToZ3ArithExpr(divisor);
			result = this.Z3Context.mkDiv(result, div);
		}
		return result;
	}

	// Terms //
	// Formula //

	/**
	 * Expects a simulink2dl Relation and hands off the conversion of its left and right
	 * hand side to simulink2dlTermToZ3ArithExpr. Produces a Z3 BoolExpr representing the
	 * same relation.
	 * 
	 * @param relation
	 * @return
	 */
	private BoolExpr simulink2dlRelationToZ3Relation(Relation relation) {
		ArithExpr lhs = simlink2dlTermToZ3ArithExpr(relation.getLeftSide());
		ArithExpr rhs = simlink2dlTermToZ3ArithExpr(relation.getRightSide());

		BoolExpr result = null;
		RelationType rel = relation.getType();
		switch (rel) {
		// LESS_THAN, LESS_EQUAL, EQUAL, GREATER_EQUAL, GREATER_THAN, NOT_EQUAL
		case LESS_THAN:
			result = this.Z3Context.mkLt(lhs, rhs);
			break;
		case LESS_EQUAL:
			result = this.Z3Context.mkLe(lhs, rhs);
			break;
		case EQUAL:
			result = this.Z3Context.mkEq(lhs, rhs);
			break;
		case GREATER_EQUAL:
			result = this.Z3Context.mkGe(lhs, rhs);
			break;
		case GREATER_THAN:
			result = this.Z3Context.mkGt(lhs, rhs);
			break;
		case NOT_EQUAL:
			result = this.Z3Context.mkEq(lhs, rhs);
			result = this.Z3Context.mkNot(result);
			break;
		default:
			PluginLogger.error("Relation of type " + rel.toString() + " not implemented!");
			break;
		}
		return result;
	}

	/**
	 * Expects a simulink2dl Formula and hands off handling to the specialized functions.
	 * Main entry point for conversion.
	 * 
	 * @param form
	 * @return
	 */
	private BoolExpr simulink2dlFomulaToZ3BoolExpr(Operator form) {
		if (form instanceof BooleanConstant) {
			return this.simulink2dlBooleanConstantToZ3Bool((BooleanConstant) form);
		} else if (form instanceof Relation) {
			return this.simulink2dlRelationToZ3Relation((Relation) form);
		} else if (form instanceof Negation) {
			return this.simulink2dlNegationToZ3Negation((Negation) form);
		} else if (form instanceof Conjunction) {
			return this.simulink2dlConjunctionToZ3And((Conjunction) form);
		} else if (form instanceof Disjunction) {
			return this.simulink2dlDisjunctionToZ3Or((Disjunction) form);
		} else if (form instanceof StringFormula) {
			return this.simulink2dlStringFormulaToZ3String((StringFormula) form);
		} else {
			PluginLogger.error("Unknown Formula type: " + form.getClass());
			return null;
		}
	}

	/**
	 * Converts a simulink2dl BooleanConstant into a BoolExpr. See
	 * simulink2dlTermToZ3ArithExpr for more information.
	 * 
	 * @param bool
	 * @return
	 */
	private BoolExpr simulink2dlBooleanConstantToZ3Bool(BooleanConstant bool) {
		return this.Z3Context.mkBool(bool.isTrue());
	}

	/**
	 * Converts a simulink2dl Negation into a BoolExpr. See simulink2dlTermToZ3ArithExpr for
	 * more information.
	 * 
	 * @param neg
	 * @return
	 */
	private BoolExpr simulink2dlNegationToZ3Negation(Negation neg) {
		return this.Z3Context.mkNot(this.simulink2dlFomulaToZ3BoolExpr(neg.getInnerFormula()));
	}

	/**
	 * Converts a simulink2dl Conjunction into a BoolExpr. See simulink2dlTermToZ3ArithExpr
	 * for more information.
	 * 
	 * @param conj
	 * @return
	 */
	private BoolExpr simulink2dlConjunctionToZ3And(Conjunction conj) {
		BoolExpr query = this.Z3Context.mkBool(true);
		for (Operator lit : conj.getElements()) {
			BoolExpr q = this.simulink2dlFomulaToZ3BoolExpr(lit);
			query = this.Z3Context.mkAnd(query, q);
		}
		return query;
	}

	/**
	 * Converts a simulink2dl Disjunction into a BoolExpr. See simulink2dlTermToZ3ArithExpr
	 * for more information.
	 * 
	 * @param disj
	 * @return
	 */
	private BoolExpr simulink2dlDisjunctionToZ3Or(Disjunction disj) {
		BoolExpr query = this.Z3Context.mkBool(true);
		for (Operator lit : disj.getElements()) {
			BoolExpr q = this.simulink2dlFomulaToZ3BoolExpr(lit);
			query = this.Z3Context.mkOr(query, q);
		}
		return query;
	}

	/**
	 * Converts a simulink2dl FormulaString into a BoolExpr. See simulink2dlTermToZ3ArithExpr
	 * for more information.
	 * 
	 * The use of StringFormula is not expected.
	 * 
	 * @param strForm
	 * @return
	 */
	private BoolExpr simulink2dlStringFormulaToZ3String(StringFormula strForm) {
		PluginLogger.error("StringFormula shouldn't be passed to the FormulaChecker!");
		// return this.this.Z3Context.mkBoolConst(strForm.toString()); // might
		// be the right thing to do here
		return null;
	}

	// Formular //

	// User interface //
	/**
	 * Pass this method a Formula representing the formula you want to test for
	 * satisfiability. See simulink2dl.dlmodel packages elements,
	 * formula and term for more information.
	 * 
	 * Returns a ResultType representing one of the following states:
	 * <ul>
	 * <li>SATISFIABLE: The Formula can be satisfied and verifiably so.</li>
	 * <li>UNSATISFIABLE: The Formula can not be satisfied and verifiably so.</li>
	 * <li>UNKNOWN: Z3 is not able to tell whether the Formula can be satisfied.
	 * </li>
	 * <li>ERROR: Z3 returned a state that is not handled.</li>
	 * </ul>
	 * 
	 * @param form
	 * @return
	 */
	public ResultType checkSingleFormula(Formula form) {
		this.Z3Context = new Context();
		Solver solver = this.Z3Context.mkSolver();
		BoolExpr query = this.simulink2dlFomulaToZ3BoolExpr(form);
		solver.add(query);
		Status checkRes = solver.check();
		this.Z3Context.close();

		switch (checkRes) {
		case SATISFIABLE:
			return ResultType.SATISFIABLE;
		case UNSATISFIABLE:
			return ResultType.UNSATISFIABLE;
		case UNKNOWN:
			return ResultType.UNKNOWN;
		default:
			return ResultType.ERROR;
		}
	}

	// public Status prepareMultiFormulaCheck() {
	// return null;
	// }
	//
	// public FormulaChecker pushFormula(Formula... formulas) {
	// return this;
	// }
	//
	// public Status popFormula() {
	// Status result = null;
	// return result;
	// }

	/**
	 * Generic getter.
	 * 
	 * @return the this.Z3Context
	 */
	public Context getZ3Context() {
		return this.Z3Context;
	}

	/**
	 * Generic setter.
	 * 
	 * @param this.Z3Context the this.Z3Context to set
	 */
	public void setZ3Context(Context context) {
		this.Z3Context = context;
	}

	/**
	 * Users should probably dispose of the Z3Context after usage.
	 */
	public void closeZ3Context() {
		this.Z3Context.close();
	}
}
