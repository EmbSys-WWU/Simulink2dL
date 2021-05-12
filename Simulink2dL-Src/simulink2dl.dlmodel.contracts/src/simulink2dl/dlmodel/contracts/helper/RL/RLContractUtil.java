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

import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.ExponentTerm;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.Term;

public class RLContractUtil {

	public static Term getEuclideanDistance(Term X, Term Y, Term otherX, Term otherY) {
		//X-OtherX
		AdditionTerm diffX = new AdditionTerm();
		diffX.add(X); diffX.subtract(otherX);
		//(X-OtherX)^2
		ExponentTerm squareDiffX = new ExponentTerm(diffX,new RealTerm(2));
		//Y-OtherY
		AdditionTerm diffY = new AdditionTerm();
		diffY.add(Y); diffY.subtract(otherY);
		//(Y-OtherY)^2
		ExponentTerm squareDiffY = new ExponentTerm(diffY,new RealTerm(2));
		//(X-OtherX)^2+(Y-OtherY)^2
		AdditionTerm sumOfSquaredDist = new AdditionTerm(squareDiffX, squareDiffY);
		//sqrt((X-OtherX)^2+(Y-OtherY)^2)
		ExponentTerm euclideanDistance = new ExponentTerm(sumOfSquaredDist,new RealTerm(0.5));
		return euclideanDistance;
	}
	
	public static Term getEuclideanDistance(Term X, Term Y) {

		//(X)^2
		ExponentTerm squareX = new ExponentTerm(X,new RealTerm(2));
		
		//(Y^2
		ExponentTerm squareY = new ExponentTerm(Y,new RealTerm(2));
		//(X)^2+(Y)^2
		AdditionTerm sumOfSquaredDist = new AdditionTerm(squareX, squareY);
		//sqrt((X)^2+(Y)^2)
		ExponentTerm euclideanDistance = new ExponentTerm(sumOfSquaredDist,new RealTerm(0.5));
		return euclideanDistance;
	}
	
	public static Term getManhattanDistance(Term X, Term Y, Term otherX, Term otherY) {
		//X-OtherX
		AdditionTerm diffX = new AdditionTerm();
		diffX.add(X); diffX.subtract(otherX);
		//|X-OtherX|
		ExponentTerm absX = new ExponentTerm(new ExponentTerm(diffX, new RealTerm(2.0)), new RealTerm(0.5));
		//Y-OtherY
		AdditionTerm diffY = new AdditionTerm();
		diffY.add(Y); diffY.subtract(otherY);
		//|Y-OtherY|
		ExponentTerm absY = new ExponentTerm(new ExponentTerm(diffY, new RealTerm(2.0)), new RealTerm(0.5));
		//|X-OtherX|+|Y-OtherY|
		AdditionTerm manhattanDistance = new AdditionTerm(absX, absY);
		return manhattanDistance;
	}
}
