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
package org.conqat.lib.commons.collections;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class manages a set of counters (i.e. is a mapping from some key objects
 * to integers). As the implementation is based on hash maps, key objects must
 * provide suitable hash keys.
 */
public class CounterSet<E> implements Serializable, Iterable<Pair<E, Integer>> {

	/** Version used for serialization. */
	private static final long serialVersionUID = 1;

	/** The underlying map. */
	protected final Map<E, Integer> map = new LinkedHashMap<>();

	/** Stores total value. */
	protected int total = 0;

	/** Constructs an empty {@link CounterSet}. */
	public CounterSet() {
		// nothing to do
	}

	/**
	 * Constructs a new {@link CounterSet} from the given keys. Initializes all keys
	 * with 1.
	 */
	public CounterSet(Collection<E> keys) {
		incAll(keys);
	}

	/** Constructs a CounterSet with one value. */
	public CounterSet(E key, int value) {
		inc(key, value);
	}

	/**
	 * Add the given increment to an element. If the element was not present before,
	 * it is interpreted as if it was present with value 0. Returns the new value.
	 *
	 * @param key
	 *            the key of the counter to increment.
	 * @param increment
	 *            the increment.
	 */
	public int inc(E key, int increment) {
		Integer value = map.get(key);
		int newValue;
		if (value == null) {
			newValue = increment;
		} else {
			newValue = value + increment;
		}
		map.put(key, newValue);

		// update total sum
		total += increment;
		return getValue(key);
	}

	/**
	 * Same as <code>inc(key, 1)</code>.
	 *
	 * @see #inc(Object, int)
	 */
	public int inc(E key) {
		return inc(key, 1);
	}

	/**
	 * Add the given increment to the given keys. If a key was not present before,
	 * it is interpreted as if it was present with value 0.
	 *
	 * @param keys
	 *            the keys of the counter to increment.
	 * @param increment
	 *            the increment.
	 */
	public void incAll(Collection<E> keys, int increment) {
		for (E key : keys) {
			inc(key, increment);
		}
	}

	/** Increments the given elements by 1 */
	public void incAll(Collection<E> keys) {
		for (E key : keys) {
			inc(key);
		}
	}

	/**
	 * Adds the given {@link CounterSet} to this {@link CounterSet} by incrementing
	 * all keys contained from other.
	 */
	public void add(CounterSet<E> other) {
		for (E key : other.getKeys()) {
			inc(key, other.getValue(key));
		}
	}

	/**
	 * Removes the second CounterSet from the first one and returns a new
	 * CounterSet.
	 */
	public static <E> CounterSet<E> removeSecondFromFirst(CounterSet<E> first, CounterSet<E> second) {
		CounterSet<E> merged = new CounterSet<>();
		for (E key : CollectionUtils.unionSet(first.getKeys(), second.getKeys())) {
			merged.inc(key, first.getValue(key));
			merged.inc(key, -second.getValue(key));
			if (merged.getValue(key) == 0) {
				merged.remove(key);
			}
		}
		return merged;
	}

	/**
	 * Remove the entry with the given key, i.e. sets its value to 0. In case the
	 * entry does not exist, nothing happens.
	 */
	public void remove(E key) {
		total -= getValue(key);
		map.remove(key);
	}

	/** Removes all entries with the given keys. */
	public void removeAll(Collection<E> keys) {
		for (E key : keys) {
			remove(key);
		}
	}

	/** Clears the counter set. */
	public void clear() {
		map.clear();
		total = 0;
	}

	/**
	 * Checks if an element is stored in the array.
	 */
	public boolean contains(E key) {
		return map.containsKey(key);
	}

	/**
	 * Get the value for an element. If the the element is not stored in the counter
	 * <code>0</code> is returned.
	 */
	public int getValue(E key) {
		Integer value = map.get(key);
		if (value == null) {
			return 0;
		}
		return value;
	}

	/**
	 * Returns the set of all elements used a keys for counters.
	 */
	public UnmodifiableSet<E> getKeys() {
		return CollectionUtils.asUnmodifiable(map.keySet());
	}

	/** Returns a list of all keys ordered by their value ascending */
	public List<E> getKeysByValueAscending() {
		return CollectionUtils.sort(getKeys(), new Comparator<E>() {
			@Override
			public int compare(E key1, E key2) {
				return map.get(key1).compareTo(map.get(key2));
			}
		});
	}

	/** Returns a list of all keys ordered by their value descending */
	public List<E> getKeysByValueDescending() {
		return CollectionUtils.reverse(getKeysByValueAscending());
	}

	/** Get total sum of all elements. */
	public int getTotal() {
		return total;
	}

	/** Returns a collection of all values */
	public Collection<Integer> values() {
		return map.values();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return map.toString();
	}

	/**
	 * Prints the distribution of values (ascending or descending) to System.out,
	 * where each value is printed on a separate line in the form &lt;key&gt; :
	 * &lt;value&gt;.
	 *
	 * <p>
	 * <i>Example:</i><br>
	 *
	 * foo : 4 <br>
	 * bar : 2
	 * </p>
	 */
	public void printValueDistribution(boolean ascending) {
		printValueDistribution(new PrintWriter(System.out), ascending);
	}

	/**
	 * Prints the distribution of values (ascending or descending) to the given
	 * stream, where each value is printed on a separate line in the form
	 * &lt;key&gt; : &lt;value&gt;.
	 *
	 * <p>
	 * <i>Example:</i><br>
	 *
	 * foo : 4 <br>
	 * bar : 2
	 * </p>
	 */
	public void printValueDistribution(PrintWriter writer, boolean ascending) {
		List<E> keys = null;
		if (ascending) {
			keys = getKeysByValueAscending();
		} else {
			keys = getKeysByValueDescending();
		}
		for (E key : keys) {
			writer.print(String.valueOf(key));
			writer.print(" : ");
			writer.print(getValue(key));
			writer.println();
		}
		writer.flush();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CounterSet) {
			@SuppressWarnings("rawtypes")
			CounterSet other = (CounterSet) obj;
			return map.equals(other.map);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return map.hashCode();
	}

	/** Returns the values as {@link Map}. */
	public Map<E, Integer> toMap() {
		return new LinkedHashMap<>(map);
	}

	/** Returns the values as a {@link Map}, but omits keys where the value is 0. */
	public Map<E, Integer> toMapWithoutZeroEntries() {
		Map<E, Integer> map = toMap();
		map.keySet().removeIf(key -> map.get(key) == 0);
		return map;
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<Pair<E, Integer>> iterator() {
		return new Iterator<Pair<E, Integer>>() {
			private Iterator<Map.Entry<E, Integer>> delegate = map.entrySet().iterator();

			/** {@inheritDoc} */
			@Override
			public boolean hasNext() {
				return delegate.hasNext();
			}

			/** {@inheritDoc} */
			@Override
			public Pair<E, Integer> next() {
				Entry<E, Integer> next = delegate.next();
				if (next == null) {
					return null;
				}
				return new Pair<>(next.getKey(), next.getValue());
			}
		};
	}
}
