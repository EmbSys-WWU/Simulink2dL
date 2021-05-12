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
package simulink2dl.dlmodel.contracts.helper.RL;

import simulink2dl.dlmodel.contracts.HybridContract;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.Relation;

public class EuclideanDistanceCalculatorContract extends HybridContract{
	/* EuclideanDistanceCalculator outputs the euclidean distance of the input positions */
	public EuclideanDistanceCalculatorContract(String serviceName) {
		super();
		Variable X = new Variable("R","X");
		Variable Y = new Variable("R","Y");
		Variable otherX = new Variable("R","OtherX");
		Variable otherY = new Variable("R","OtherY");
		inputs.add(X);
		inputs.add(Y);
		inputs.add(otherX);
		inputs.add(otherY);
		Variable EuclDist = new Variable("R","EuclDist");
		outputs.add(EuclDist);
		
		Relation distCalcGuarantee   = new Relation(EuclDist, EQUAL, RLContractUtil.getEuclideanDistance(X,Y,otherX,otherY));
		setAssumptionGuaranteePair(TRUE, distCalcGuarantee);
	}

}
