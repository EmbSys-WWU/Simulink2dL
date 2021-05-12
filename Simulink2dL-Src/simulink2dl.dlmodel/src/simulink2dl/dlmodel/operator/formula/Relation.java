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
package simulink2dl.dlmodel.operator.formula;

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.dlmodel.term.VectorTerm;
import simulink2dl.util.PluginLogger;

/**
 * A comparison that contains two terms and relates them with a relation
 * operator. Possible types for the relation are "<", "<=", "=", ">=", ">", and
 * "!=".
 * 
 * @author Timm Liebrenz
 *
 */
public class Relation implements Formula {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((leftSide == null) ? 0 : leftSide.hashCode());
		result = prime * result + ((rightSide == null) ? 0 : rightSide.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Relation other = (Relation) obj;
		if (leftSide == null) {
			if (other.leftSide != null)
				return false;
		} else if (!leftSide.equals(other.leftSide))
			return false;
		if (rightSide == null) {
			if (other.rightSide != null)
				return false;
		} else if (!rightSide.equals(other.rightSide))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	/**
	 * Left hand side of the relation.
	 */
	private Term leftSide;

	/**
	 * Right hand side of the relation.
	 */
	private Term rightSide;

	/**
	 * Type of the relation.
	 */
	private RelationType type;

	/**
	 * Enumeration that contains the possible relation types.
	 * 
	 * @author Timm Liebrenz
	 *
	 */
	public enum RelationType {
		LESS_THAN, LESS_EQUAL, EQUAL, GREATER_EQUAL, GREATER_THAN, NOT_EQUAL;

		/**
		 * String representation of the relation type.
		 */
		private String asString;

		static {
			LESS_THAN.asString = "<";
			LESS_EQUAL.asString = "<=";
			EQUAL.asString = "=";
			GREATER_EQUAL.asString = ">=";
			GREATER_THAN.asString = ">";
			NOT_EQUAL.asString = "!=";
		}

		public String toString() {
			return asString;
		}

		/**
		 * Negation of the relation type.
		 */
		private RelationType negated;

		static {
			LESS_THAN.negated = GREATER_EQUAL;
			LESS_EQUAL.negated = GREATER_THAN;
			EQUAL.negated = NOT_EQUAL;
			GREATER_EQUAL.negated = LESS_THAN;
			GREATER_THAN.negated = LESS_EQUAL;
			NOT_EQUAL.negated = EQUAL;
		}

		public RelationType negate() {
			return negated;
		}

		/**
		 * Negation of the relation type.
		 */
		private RelationType negatedOverlap;

		static {
			LESS_THAN.negatedOverlap = GREATER_EQUAL;
			LESS_EQUAL.negatedOverlap = GREATER_EQUAL;
			EQUAL.negatedOverlap = NOT_EQUAL;
			GREATER_EQUAL.negatedOverlap = LESS_EQUAL;
			GREATER_THAN.negatedOverlap = LESS_EQUAL;
			NOT_EQUAL.negatedOverlap = EQUAL;
		}

		public RelationType negateWithOverlap() {
			return negatedOverlap;
		}

	}

	/**
	 * Construction for a relation.
	 * 
	 * @param leftSide
	 * @param type
	 * @param rightSide
	 */
	public Relation(Term leftSide, RelationType type, Term rightSide) {
		this.leftSide = leftSide;
		this.type = type;
		this.rightSide = rightSide;
	}

	/**
	 * @return the leftSide
	 */
	public Term getLeftSide() {
		return leftSide;
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	/**
	 * @return the rightSide
	 */
	public Term getRightSide() {
		return rightSide;
	}

	/**
	 * @return the type
	 */
	public RelationType getType() {
		return type;
	}

	@Override
	public String toString() {
		return leftSide.toString() + " " + type.toString() + " " + rightSide.toString();
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		return leftSide.toString() + " " + type.toString() + " " + rightSide.toString();
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		if (leftSide.equals(toReplace)) {
			leftSide = replaceWith;
		} else {
			leftSide.replaceTermRecursive(toReplace, replaceWith);
		}
		if (rightSide.equals(toReplace)) {
			rightSide = replaceWith;
		} else {
			rightSide.replaceTermRecursive(toReplace, replaceWith);
		}
	}

	@Override
	public boolean containsTerm(Term term) {
		if (leftSide.equals(term)) {
			return true;
		}
		if (rightSide.equals(term)) {
			return true;
		}
		if (leftSide.containsTerm(term)) {
			return true;
		}
		if (rightSide.containsTerm(term)) {
			return true;
		}
		return false;
	}

	@Override
	public Relation createDeepCopy() {
		return new Relation(leftSide.createDeepCopy(), type, rightSide.createDeepCopy());
	}

	/**
	 * Creates a new relation that is the negation of this relation.
	 * 
	 * @return
	 */
	@Override
	public Relation createNegation() {
		return new Relation(leftSide.createDeepCopy(), type.negate(), rightSide.createDeepCopy());

	}

	/**
	 * Creates a new relation that is the negation of this relation. The flag
	 * determines whether the resulting relation overlaps
	 * 
	 * @return
	 */
	public Relation createNegation(boolean useOverlappingBounds) {
		if (useOverlappingBounds) {
			return new Relation(leftSide.createDeepCopy(), type.negateWithOverlap(), rightSide.createDeepCopy());
		} else {
			return createNegation();
		}
	}

	public void setRightSide(Term t) {
		this.rightSide = t;
	}

	public void setLeftSide(Term t) {
		this.leftSide = t;
	}

	public void setType(RelationType r) {
		this.type = r;
	}
	
	/* TODO: refactor
	/*
	 * Two Cases: initcondition with variables
	 * different Terms
	 */
	@Override
	public Formula expand() {
		VectorTerm vectorLeft = null;
		VectorTerm vectorRight = null;
		if(leftSide instanceof VectorTerm) {
			vectorLeft = (VectorTerm) leftSide;
		}
		if(leftSide instanceof Variable && ((Variable) leftSide).getSize() > 0) {
			vectorLeft = ((Variable) leftSide).getVector();
		}
		if(rightSide instanceof VectorTerm) {
			vectorRight = (VectorTerm) rightSide;
		}
		if(rightSide instanceof Variable && ((Variable) rightSide).getSize() > 0) {
			vectorRight = ((Variable) rightSide).getVector();
		}
		if(vectorLeft==null & vectorRight!=null) {
			vectorLeft = new VectorTerm();
			for(int i = 0; i < vectorRight.size(); i++) {
				vectorLeft.add(leftSide.createDeepCopy());//copy initial conditions
			}
		}		
		if(vectorLeft!=null & vectorRight==null) {
			vectorRight = new VectorTerm();
			for(int i = 0; i < vectorLeft.size(); i++) {
				vectorRight.add(rightSide.createDeepCopy());//copy initial conditions
			}
		}
		if(vectorLeft != null && vectorRight != null) {
			if(vectorLeft.size() == vectorRight.size()) {
				Conjunction relations = new Conjunction();
				for(int i = 0; i<vectorLeft.size(); i++) {
					relations.addElement(new Relation(vectorLeft.get(i),type, vectorRight.get(i)));
				}
				return relations;
			} else {
				PluginLogger.error("Vector expansion of " + this.getClass().toString()+ 
								   " failed: size of leftSide != size of rightSide");
			}
		}

		return this;
	}
}
