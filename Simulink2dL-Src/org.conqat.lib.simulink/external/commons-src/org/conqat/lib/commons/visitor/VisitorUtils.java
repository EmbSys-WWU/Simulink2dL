/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright 2005-2011 The ConQAT Project                                   |
|                                                                          |
| Licensed under the Apache License, Version 2.0 (the "License");          |
| you may not use this file except in compliance with the License.         |
| You may obtain a copy of the License at                                  |
|                                                                          |
|    http://www.apache.org/licenses/LICENSE-2.0                            |
|                                                                          |
| Unless required by applicable law or agreed to in writing, software      |
| distributed under the License is distributed on an "AS IS" BASIS,        |
| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. |
| See the License for the specific language governing permissions and      |
| limitations under the License.                                           |
+-------------------------------------------------------------------------*/
package org.conqat.lib.commons.visitor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.collections.IdentityHashSet;
import org.conqat.lib.commons.error.NeverThrownRuntimeException;

/**
 * Utility class for working with visitors.
 */
public class VisitorUtils {

	/**
	 * Visits all nodes of a tree in pre-order, i.e. a node is visited directly
	 * before its children.
	 * 
	 * @param root
	 *            the root of the tree.
	 * @param walker
	 *            the walker user for traversing the tree.
	 * @param visitor
	 *            the visitor used for visiting the nodes.
	 */
	public static <T, X1 extends Exception, X2 extends Exception> void visitAllPreOrder(T root,
			ITreeWalker<T, X1> walker, IVisitor<T, X2> visitor) throws X1, X2 {

		visitor.visit(root);
		for (T child : walker.getChildren(root)) {
			visitAllPreOrder(child, walker, visitor);
		}
	}

	/**
	 * Visits all leaves of a tree, i.e. those nodes without children.
	 * 
	 * @param root
	 *            the root of the tree.
	 * @param walker
	 *            the walker user for traversing the tree.
	 * @param visitor
	 *            the visitor used for visiting the nodes.
	 */
	public static <T, X1 extends Exception, X2 extends Exception> void visitLeaves(T root, ITreeWalker<T, X1> walker,
			IVisitor<T, X2> visitor) throws X1, X2 {

		Collection<T> children = walker.getChildren(root);
		if (children.isEmpty()) {
			visitor.visit(root);
		} else {
			for (T child : children) {
				visitLeaves(child, walker, visitor);
			}
		}
	}

	/**
	 * Visits all elements of a mesh in depth first order. It is made sure, that
	 * each reachable element is visited exactly once, where we use equality of
	 * references to determine elements that were seen before.
	 * 
	 * Does not use recursive function calls to enable listing large graphs.
	 * 
	 * @param start
	 *            the element to start the traversal from.
	 * @param walker
	 *            the walker user for traversing the mesh.
	 * @param visitor
	 *            the visitor used for visiting the elements.
	 */
	public static <T, X1 extends Exception, X2 extends Exception> void visitAllDepthFirst(T start,
			IMeshWalker<T, X1> walker, IVisitor<T, X2> visitor) throws X1, X2 {
		IdentityHashSet<T> visitedNodes = new IdentityHashSet<T>();
		Deque<T> todoNodes = new ArrayDeque<>();
		todoNodes.addFirst(start);
		while (!todoNodes.isEmpty()) {
			T current = todoNodes.removeFirst();
			if (visitedNodes.contains(current)) {
				continue;
			}
			visitor.visit(current);
			visitedNodes.add(current);
			for (T succ : CollectionUtils.reverse(walker.getAdjacentElements(current))) {
				if (!visitedNodes.contains(succ)) {
					todoNodes.addFirst(succ);
				}
			}
		}
	}

	/**
	 * Lists all elements of a mesh in depth first order. It is made sure, that
	 * each reachable element is visited exactly once, where we use equality of
	 * references to determine elements that were seen before.
	 * 
	 * @param start
	 *            the element to start the traversal from.
	 * @param walker
	 *            the walker user for traversing the mesh.
	 */
	public static <T, X extends Exception> List<T> listAllDepthFirst(T start, IMeshWalker<T, X> walker) throws X {
		final List<T> list = new ArrayList<>();
		visitAllDepthFirst(start, walker, new IVisitor<T, NeverThrownRuntimeException>() {

			@Override
			public void visit(T element) throws NeverThrownRuntimeException {
				list.add(element);
			}
		});
		return list;
	}

}