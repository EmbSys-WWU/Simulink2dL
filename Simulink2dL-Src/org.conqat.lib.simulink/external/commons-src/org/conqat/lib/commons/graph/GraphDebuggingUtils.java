/*-----------------------------------------------------------------------+
 | org.conqat.engine.index.incubator
 |                                                                       |
   $Id$
 |                                                                       |
 | Copyright (c)  2009-2013 CQSE GmbH                                 |
 +-----------------------------------------------------------------------*/
package org.conqat.lib.commons.graph;

import static org.conqat.lib.commons.collections.CollectionUtils.map;
import static org.conqat.lib.commons.string.StringUtils.concat;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.conqat.lib.commons.collections.IIdProvider;
import org.conqat.lib.commons.collections.IdManager;
import org.conqat.lib.commons.visitor.IMeshWalker;

/**
 * Useful methods for debugging arbitrary graphs.
 */
public class GraphDebuggingUtils {

	/**
	 * Returns a string representation of the graph. The individual nodes are
	 * represented using their toString() method. This is useful for debugging.
	 * 
	 * @param root
	 *            the root node of the graph to visualize.
	 * @param optionalIdProvider
	 *            an optional IdProvider that provides integer IDs for all Nodes. If
	 *            {@code null}, we will use an IdManager. The IDs are assumed to be
	 *            stable (the same on each query of the same node).
	 * @param walkers
	 *            the mesh walkers used to traverse the graph.
	 */
	@SafeVarargs
	public static <NodeT, X extends Exception> String getString(NodeT root,
			IIdProvider<Integer, NodeT> optionalIdProvider, IMeshWalker<NodeT, X>... walkers) throws X {
		return getString(root, optionalIdProvider, true, walkers);
	}

	/**
	 * Returns a human-readable string representation of the graph. The individual
	 * nodes are only represented as integers. This is useful for debugging.
	 * 
	 * @param root
	 *            the root node of the graph to visualize.
	 * @param optionalIdProvider
	 *            an optional IdProvider that provides integer IDs for all Nodes. If
	 *            {@code null}, we will use an IdManager. The IDs are assumed to be
	 *            stable (the same on each query of the same node).
	 * @param walkers
	 *            the mesh walkers used to traverse the graph.
	 */
	@SafeVarargs
	public static <NodeT, X extends Exception> String getShortString(NodeT root,
			IIdProvider<Integer, NodeT> optionalIdProvider, IMeshWalker<NodeT, X>... walkers) throws X {
		return getString(root, optionalIdProvider, false, walkers);
	}

	/**
	 * Returns a string representation of the graph.
	 * 
	 * @param root
	 *            the root node of the graph to visualize.
	 * @param optionalIdProvider
	 *            an optional IdProvider that provides integer IDs for all Nodes. If
	 *            {@code null}, we will use an IdManager. The IDs are assumed to be
	 *            stable (the same on each query of the same node).
	 * @param detailed
	 *            if <code>true</code>, each node's toString() representation is
	 *            included in the output.
	 * @param walkers
	 *            the mesh walkers used to traverse different classes of edges found
	 *            in the graph.
	 */
	@SafeVarargs
	private static <NodeT, X extends Exception> String getString(NodeT root,
			IIdProvider<Integer, NodeT> optionalIdProvider, boolean detailed, IMeshWalker<NodeT, X>... walkers)
			throws X {
		IIdProvider<Integer, NodeT> idProvider = getIdProvider(optionalIdProvider);

		INodeRenderer<NodeT> nodeRenderer = (node) -> {
			StringBuilder rendering = new StringBuilder();
			rendering.append(idProvider.obtainId(node));
			if (detailed) {
				rendering.append(": { ");
				rendering.append(node);
				rendering.append(" }");
			}
			return rendering.toString();
		};
		IEdgeRenderer<NodeT> edgeRenderer = (node, edgeClass, adjacentNodes) -> " --> "
				+ concat(map(adjacentNodes, idProvider::obtainId), ", ") + "\n";

		return renderGraph(root, nodeRenderer, edgeRenderer, walkers);
	}

	/**
	 * Returns a string representation of the graph in Dot format suitable for
	 * rendering with Graphviz.
	 *
	 * @param root
	 *            the root node of the graph to visualize.
	 * @param optionalIdProvider
	 *            an optional IdProvider that provides integer IDs for all Nodes. If
	 *            {@code null}, we will use an IdManager. The IDs are assumed to be
	 *            stable (the same on each query of the same node).
	 * @param walkers
	 *            the mesh walkers used to traverse different classes of edges found
	 *            in the graph.
	 * 
	 * @see <a href="https://graphviz.gitlab.io/_pages/doc/info/lang.html">The DOT
	 *      Language</a>
	 */
	@SafeVarargs
	public static <NodeT, X extends Exception> String getDotString(NodeT root,
			IIdProvider<Integer, NodeT> optionalIdProvider, IMeshWalker<NodeT, X>... walkers) throws X {
		IIdProvider<Integer, NodeT> idProvider = getIdProvider(optionalIdProvider);

		INodeRenderer<NodeT> nodeRenderer = (node) -> idProvider.obtainId(node) + //
				" [label = " + toQuotedString(node.toString()) + "];\n";
		IEdgeRenderer<NodeT> edgeRenderer = (node, edgeClass, adjacentNodes) -> idProvider.obtainId(node) + " -> " + //
				"{ " + concat(map(adjacentNodes, idProvider::obtainId), " ") + " }" + " [color=\""
				+ EDGE_COLORS[edgeClass] + "\"];\n";

		return "digraph {\n" + renderGraph(root, nodeRenderer, edgeRenderer, walkers) + "}\n";
	}

	/** Colors for different classes of edges */
	private static final String[] EDGE_COLORS = { "black", "blue", "red", "green" };

	private static <NodeT> IIdProvider<Integer, NodeT> getIdProvider(IIdProvider<Integer, NodeT> optionalIdProvider) {
		return Optional.ofNullable(optionalIdProvider).orElseGet(IdManager::new);
	}

	@SafeVarargs
	private static <NodeT, X extends Exception> String renderGraph(NodeT root, INodeRenderer<NodeT> nodeRenderer,
			IEdgeRenderer<NodeT> edgeRenderer, IMeshWalker<NodeT, X>... walkers) throws X {
		StringBuilder graph = new StringBuilder();

		Deque<NodeT> todo = new ArrayDeque<>(Collections.singleton(root));
		Set<NodeT> addedNodes = new HashSet<>();
		while (!todo.isEmpty()) {
			NodeT nodeToAdd = todo.poll();
			if (!addedNodes.contains(nodeToAdd)) {
				graph.append(nodeRenderer.render(nodeToAdd));

				for (int walkerIndex = 0; walkerIndex < walkers.length; walkerIndex++) {
					IMeshWalker<NodeT, X> walker = walkers[walkerIndex];
					Collection<NodeT> adjacentElements = walker.getAdjacentElements(nodeToAdd);
					graph.append(edgeRenderer.render(nodeToAdd, walkerIndex, adjacentElements));
					todo.addAll(adjacentElements);
				}

				addedNodes.add(nodeToAdd);
			}
		}

		return graph.toString();
	}

	/**
	 * Produces a double-quoted string as defined by the
	 * <a href="https://graphviz.gitlab.io/_pages/doc/info/lang.html">DOT
	 * language</a>.
	 */
	private static String toQuotedString(String string) {
		return '"' + string.replace("\"", "\\\"") + '"';
	}

	@FunctionalInterface
	private interface INodeRenderer<NodeT> {

		/** Renders the given node. */
		String render(NodeT node);
	}

	@FunctionalInterface
	private interface IEdgeRenderer<NodeT> {

		/**
		 * Renders the edges (of the given class) from the node to any adjacent nodes.
		 */
		String render(NodeT node, int edgeClass, Collection<NodeT> adjacentNodes);
	}
}
