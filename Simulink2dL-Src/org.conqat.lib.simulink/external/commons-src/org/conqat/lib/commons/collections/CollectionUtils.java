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

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.commons.string.StringUtils;

/**
 * This class offers utility methods for collections. In can be seen as an
 * extension to {@link Collections}.
 */
public class CollectionUtils {

	/** Empty unmodifiable list. */
	private static final UnmodifiableList<?> EMPTY_LIST = new UnmodifiableList<>(Collections.emptyList());

	/** Empty unmodifiable map. */
	private static final UnmodifiableMap<?, ?> EMPTY_MAP = new UnmodifiableMap<>(Collections.emptyMap());

	/** Empty unmodifiable set. */
	private static final UnmodifiableSet<?> EMPTY_SET = new UnmodifiableSet<>(Collections.emptySet());

	/** Delimiter used in strings to separate list values. */
	public static final String MULTI_VALUE_DELIMITER = ",";

	/**
	 * Create a hashed set from an array.
	 *
	 * @param <T>
	 *            type
	 * @param elements
	 *            elements in the set.
	 * @return the set.
	 * @see Arrays#asList(Object[])
	 */
	@SafeVarargs
	public static <T> HashSet<T> asHashSet(T... elements) {
		HashSet<T> result = new HashSet<>(elements.length);
		Collections.addAll(result, elements);
		return result;
	}

	/** Creates a new unmodifiable {@link HashMap} based on given pairs. */
	@SafeVarargs
	public static <K, V> UnmodifiableMap<K, V> asMap(Pair<K, V>... pairs) {
		return asMap(new HashMap<>(), pairs);
	}

	/**
	 * Extends the given map with the pairs and returns an unmodifiable version.
	 */
	@SafeVarargs
	public static <K, V> UnmodifiableMap<K, V> asMap(Map<K, V> baseMap, Pair<K, V>... pairs) {
		for (Pair<K, V> pair : pairs) {
			baseMap.put(pair.getFirst(), pair.getSecond());
		}
		return asUnmodifiable(baseMap);
	}

	/** Creates an unmodifiable hash set from an array. */
	@SafeVarargs
	public static <T> UnmodifiableSet<T> asUnmodifiableHashSet(T... elements) {
		return new UnmodifiableSet<>(CollectionUtils.asHashSet(elements));
	}

	/**
	 * Returns a new unmodifiable collection wrapping the given collection. This
	 * method is here to avoid retyping the generic argument for the collection
	 * class.
	 */
	public static <T> UnmodifiableCollection<T> asUnmodifiable(Collection<T> c) {
		return new UnmodifiableCollection<>(c);
	}

	/**
	 * Returns a new unmodifiable list wrapping the given list. This method is here
	 * to avoid retyping the generic argument for the list class.
	 */
	public static <T> UnmodifiableList<T> asUnmodifiable(List<T> l) {
		return new UnmodifiableList<>(l);
	}

	/**
	 * Returns a new unmodifiable map wrapping the given map. This method is here to
	 * avoid retyping the generic arguments for the map class.
	 */
	public static <S, T> UnmodifiableMap<S, T> asUnmodifiable(Map<S, T> m) {
		return new UnmodifiableMap<>(m);
	}

	/**
	 * Returns a new unmodifiable set wrapping the given set. This method is here to
	 * avoid retyping the generic argument for the set class.
	 */
	public static <T> UnmodifiableSet<T> asUnmodifiable(Set<T> s) {
		return new UnmodifiableSet<>(s);
	}

	/**
	 * Returns a new unmodifiable sorted map wrapping the given sorted map. This
	 * method is here to avoid retyping the generic arguments for the sorted map
	 * class.
	 */
	public static <S, T> UnmodifiableSortedMap<S, T> asUnmodifiable(SortedMap<S, T> m) {
		return new UnmodifiableSortedMap<>(m);
	}

	/**
	 * Returns a new unmodifiable sorted set wrapping the given sorted set. This
	 * method is here to avoid retyping the generic argument for the sorted set
	 * class.
	 */
	public static <T> UnmodifiableSortedSet<T> asUnmodifiable(SortedSet<T> s) {
		return new UnmodifiableSortedSet<>(s);
	}

	/**
	 * The way this method is defined allows to assign it to all parameterized
	 * types, i.e. both of the following statements are accepted by the compiler
	 * without warnings:
	 *
	 * <pre>
	 * UnmodifiableList&lt;String&gt; emptyList = CollectionUtils.emptyList();
	 *
	 * UnmodifiableList&lt;Date&gt; emptyList = CollectionUtils.emptyList();
	 * </pre>
	 *
	 * @return the empty list
	 */
	@SuppressWarnings("unchecked")
	public static final <T> UnmodifiableList<T> emptyList() {
		return (UnmodifiableList<T>) EMPTY_LIST;
	}

	/** Returns an empty map. See {@link #emptyList()} for further details. */
	@SuppressWarnings("unchecked")
	public static final <S, T> UnmodifiableMap<S, T> emptyMap() {
		return (UnmodifiableMap<S, T>) EMPTY_MAP;
	}

	/** Returns an empty set. See {@link #emptyList()} for further details. */
	@SuppressWarnings("unchecked")
	public static final <T> UnmodifiableSet<T> emptySet() {
		return (UnmodifiableSet<T>) EMPTY_SET;
	}

	/**
	 * Sorts the specified list into ascending order, according to the <i>natural
	 * ordering</i> of its elements.
	 * <p>
	 * All elements in the list must implement the Comparable interface.
	 * Furthermore, all elements in the list must be mutually comparable (that is,
	 * e1.compareTo(e2) must not throw a <code>ClassCastException</code> for any
	 * elements e1 and e2 in the list).
	 * <p>
	 * This method does not modify the original collection.
	 */
	public static <T extends Comparable<? super T>> List<T> sort(Collection<T> collection) {
		ArrayList<T> list = new ArrayList<>(collection);
		Collections.sort(list);
		return list;
	}

	/**
	 * Returns a list that contains the elements of the specified list in reversed
	 * order.
	 * <p>
	 * This method does not modify the original collection.
	 */
	public static <T> List<T> reverse(Collection<T> list) {
		ArrayList<T> reversed = new ArrayList<>(list);
		Collections.reverse(reversed);
		return reversed;
	}

	/**
	 * Returns all values, which occur multiple times in the list.
	 * <p>
	 * It doesn't tell you how often it occurs, just that it is more than once.
	 */
	public static <T> Set<T> getDuplicates(List<T> values) {
		Set<T> duplicates = new HashSet<>();
		Set<T> temp = new HashSet<>();
		for (T key : values) {
			if (!temp.add(key)) {
				duplicates.add(key);
			}
		}
		return duplicates;
	}

	/**
	 * Applies the mapper {@link Function} to all items in the collection and
	 * returns the resulting {@link List}.
	 * <p>
	 * This method does not modify the original collection.
	 */
	public static <T, R> List<R> map(Collection<T> list, Function<? super T, ? extends R> mapper) {
		List<R> result = new ArrayList<>();
		for (T entry : list) {
			result.add(mapper.apply(entry));
		}
		return result;
	}

	/**
	 * Applies the mapper {@link Function} to all items in the collection and
	 * returns the resulting {@link List}.
	 * <p>
	 * This method does not modify the original collection.
	 */
	public static <K1, V1, K2, V2> Map<K2, V2> map(Map<K1, V1> map, Function<K1, ? extends K2> keyMapper,
			Function<V1, ? extends V2> valueMapper) {
		Map<K2, V2> result = new HashMap<>();
		map.forEach((key, value) -> {
			result.put(keyMapper.apply(key), valueMapper.apply(value));
		});
		return result;
	}

	/**
	 * Applies the mapper {@link Function} to all items in the array and returns the
	 * resulting {@link List}.
	 * <p>
	 * This method does not modify the original array.
	 */
	public static <T, R> List<R> map(T[] array, Function<? super T, ? extends R> mapper) {
		return map(Arrays.asList(array), mapper);
	}

	/**
	 * Applies the mapper {@link Function} to all items in the collection, but only
	 * adds the result item to the return list if it is not already in the list.
	 * Returns the resulting {@link List}.
	 * <p>
	 * This method does not modify the original collection.
	 */
	public static <T, R> List<R> mapDistinct(Collection<T> list, Function<? super T, ? extends R> mapper) {
		Set<R> encounteredItems = new HashSet<>();
		List<R> result = new ArrayList<>();
		for (T entry : list) {
			R resultItem = mapper.apply(entry);
			if (encounteredItems.add(resultItem)) {
				result.add(resultItem);
			}
		}
		return result;
	}

	/**
	 * Applies the mapper {@link Function} to all items in the collection, but only
	 * adds the result item to the return list if it is not already in the list.
	 * Returns the resulting {@link List}.
	 * <p>
	 * This method does not modify the original collection.
	 */
	public static <T, R> List<R> mapDistinct(T[] array, Function<? super T, ? extends R> mapper) {
		return mapDistinct(Arrays.asList(array), mapper);
	}

	/**
	 * Applies the mapper {@link FunctionWithException} to all items in the list and
	 * returns the resulting {@link List}.
	 * <p>
	 * This method does not modify the original collection.
	 *
	 * @throws E
	 *             if the mapper function throws this exception for any of the
	 *             elements of the original list.
	 */
	public static <T, R, E extends Exception> List<R> mapWithException(Collection<T> list,
			FunctionWithException<? super T, ? extends R, ? extends E> mapper) throws E {
		List<R> result = new ArrayList<>();
		for (T entry : list) {
			result.add(mapper.apply(entry));
		}
		return result;
	}

	/**
	 * This interface is the same as {@link Supplier}, except it allows throwing a
	 * checked exception.
	 */
	@FunctionalInterface
	public static interface SupplierWithException<T, E extends Exception> {

		/**
		 * Returns the supplied value. May throw a declared exception.
		 */
		public T get() throws E;
	}

	/**
	 * This interface is the same as {@link Function}, except it allows throwing a
	 * checked exception.
	 */
	@FunctionalInterface
	public static interface FunctionWithException<T, R, E extends Exception> {

		/**
		 * Applies the function to the given argument. May throw the declared exception.
		 */
		public R apply(T t) throws E;

	}

	/**
	 * This interface is the same as {@link Consumer}, except it allows throwing a
	 * checked exception.
	 */
	@FunctionalInterface
	public static interface ConsumerWithException<T, E extends Exception> {

		/**
		 * Performs this operation on the given argument.
		 *
		 * @param t
		 *            the input argument
		 */
		void accept(T t) throws E;

	}

	/**
	 * This interface is the same as {@link Consumer}, except it allows throwing two
	 * checked exceptions.
	 */
	@FunctionalInterface
	public static interface ConsumerWithTwoExceptions<T, E1 extends Exception, E2 extends Exception> {

		/**
		 * Performs this operation on the given argument.
		 *
		 * @param t
		 *            the input argument
		 */
		void accept(T t) throws E1, E2;

	}

	/**
	 * Filters the list by testing all items against the {@link Predicate} and
	 * returning a {@link List} of those for which it returns <code>true</code>.
	 *
	 * <p>
	 * This method does not modify the original collection.
	 */
	public static <T> List<T> filter(Collection<T> list, Predicate<? super T> filter) {
		List<T> result = new ArrayList<>();
		for (T entry : list) {
			if (filter.test(entry)) {
				result.add(entry);
			}
		}
		return result;
	}

	/**
	 * Applies the mapper to all items in the list, for which the filter
	 * {@link Predicate} returns <code>true</code> and returns the results as a
	 * {@link List}.
	 * <p>
	 * This method does not modify the original collection.
	 */
	public static <T, R> List<R> filterAndMap(Collection<T> list, Predicate<? super T> filter,
			Function<? super T, ? extends R> mapper) {
		List<R> result = new ArrayList<>();
		for (T entry : list) {
			if (filter.test(entry)) {
				result.add(mapper.apply(entry));
			}
		}
		return result;
	}

	/**
	 * Returns a list that contains all elements of the specified list <B>except</B>
	 * the element at the specified index. Removes the index from the new List.
	 * <p>
	 * This method does not modify the original list.
	 */
	public static <T> List<T> remove(List<T> list, int index) {
		ArrayList<T> result = new ArrayList<>(list);
		result.remove(index);
		return result;
	}

	/**
	 * Sorts the specified list according to the order induced by the specified
	 * comparator.
	 * <p>
	 * All elements in the list must implement the Comparable interface.
	 * Furthermore, all elements in the list must be mutually comparable (that is,
	 * e1.compareTo(e2) must not throw a <code>ClassCastException</code> for any
	 * elements e1 and e2 in the list).
	 * <p>
	 * This method does not modify the original collection.
	 */
	public static <T> List<T> sort(Collection<T> collection, Comparator<? super T> comparator) {
		ArrayList<T> list = new ArrayList<>(collection);
		Collections.sort(list, comparator);
		return list;
	}

	/** Returns a sorted unmodifiable list for a collection. */
	public static <T extends Comparable<? super T>> UnmodifiableList<T> asSortedUnmodifiableList(
			Collection<T> collection) {
		return asUnmodifiable(sort(collection));
	}

	/**
	 * Returns a sorted unmodifiable list for a collection.
	 *
	 * @param comparator
	 *            Comparator used for sorting
	 */
	public static <T> UnmodifiableList<T> asSortedUnmodifiableList(Collection<T> collection,
			Comparator<? super T> comparator) {
		return asUnmodifiable(sort(collection, comparator));
	}

	/**
	 * Returns one object from an {@link Iterable} or <code>null</code> if the
	 * iterable is empty.
	 */
	public static <T> T getAny(Iterable<T> iterable) {
		Iterator<T> iterator = iterable.iterator();
		if (!iterator.hasNext()) {
			return null;
		}
		return iterator.next();
	}

	/**
	 * Convert collection to array. This is a bit cleaner version of
	 * {@link Collection#toArray(Object[])}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(Collection<? extends T> collection, Class<T> type) {
		T[] result = (T[]) java.lang.reflect.Array.newInstance(type, collection.size());

		Iterator<? extends T> it = collection.iterator();
		for (int i = 0; i < collection.size(); i++) {
			result[i] = it.next();
		}

		return result;
	}

	/**
	 * Copy an array. This is a shortcut for {@link Arrays#copyOf(Object[], int)}
	 * that does not require to specify the length.
	 */
	public static <T> T[] copyArray(T[] original) {
		return Arrays.copyOf(original, original.length);
	}

	/**
	 * Compute list of unordered pairs for all elements contained in a collection.
	 */
	public static <T> List<ImmutablePair<T, T>> computeUnorderedPairs(Collection<T> collection) {
		List<T> elements = new ArrayList<>(collection);
		List<ImmutablePair<T, T>> pairs = new ArrayList<>();

		int size = elements.size();
		for (int firstIndex = 0; firstIndex < size; firstIndex++) {
			for (int secondIndex = firstIndex + 1; secondIndex < size; secondIndex++) {
				pairs.add(new ImmutablePair<>(elements.get(firstIndex), elements.get(secondIndex)));
			}
		}
		return pairs;
	}

	/**
	 * Returns the last element in list or <code>null</code>, if list is empty.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getLast(List<T> list) {
		if (list.isEmpty()) {
			return null;
		}
		if (list instanceof Deque<?>) {
			return ((Deque<T>) list).getLast();
		}
		return list.get(list.size() - 1);
	}

	/**
	 * Returns the sublist of all but the first element in list or
	 * <code>null</code>, if list is empty.
	 */
	public static <T> List<T> getRest(List<T> list) {
		if (list.isEmpty()) {
			return null;
		}
		return list.subList(1, list.size());
	}

	/** Sorts the pair list by first index. */
	public static <S extends Comparable<S>, T> void sortByFirst(final PairList<S, T> list) {
		sortByFirst(list, null);
	}

	/** Sorts the pair list with the given comparator. */
	public static <S extends Comparable<S>, T> void sortByFirst(final PairList<S, T> list,
			final Comparator<S> comparator) {
		SortableDataUtils.sort(new ISortableData() {

			@Override
			public void swap(int i, int j) {
				list.swapEntries(i, j);
			}

			@Override
			public int size() {
				return list.size();
			}

			@Override
			public boolean isLess(int i, int j) {
				if (comparator != null) {
					return comparator.compare(list.getFirst(i), list.getFirst(j)) < 0;
				}
				return list.getFirst(i).compareTo(list.getFirst(j)) < 0;
			}
		});
	}

	/**
	 * Returns a list implementation that allows for efficient random access.
	 *
	 * If the passed collection already supports random access, it gets returned
	 * directly. Otherwise a list that supports random access is returned with the
	 * same content as the passed list.
	 */
	public static <T> List<T> asRandomAccessList(Collection<T> list) {
		// It is not guaranteed that implementations of RandomAccess also
		// implement List. Hence, we check for both.
		if (list instanceof List<?> && list instanceof RandomAccess) {
			return (List<T>) list;
		}

		return new ArrayList<>(list);
	}

	/**
	 * Return a set containing the union of all provided collections. We use a
	 * {@link HashSet}, i.e. the elements should support hashing.
	 *
	 * We use two separate arguments to ensure on the interface level that at least
	 * one collection is provided. This is transparent for the caller.
	 * 
	 * All arguments will be null. The result will always be non-null and will be an
	 * empty set if all arguments are null.
	 */
	@SafeVarargs
	public static <T> HashSet<T> unionSet(Collection<T> collection1, Collection<T>... furtherCollections) {
		HashSet<T> result = new HashSet<>();
		if (collection1 != null) {
			result.addAll(collection1);
		}
		for (Collection<T> collection : furtherCollections) {
			if (collection != null) {
				result.addAll(collection);
			}
		}
		return result;
	}

	/**
	 * Return a set containing the union of all elements of the provided sets. We
	 * use a {@link HashSet}, i.e. the elements should support hashing.
	 */
	public static <T> HashSet<T> unionSetAll(Collection<? extends Collection<T>> sets) {
		HashSet<T> result = new HashSet<>();
		for (Collection<T> set : sets) {
			result.addAll(set);
		}
		return result;
	}

	/**
	 * Creates a new set only containing those elements of the given collection that
	 * are not in elementsToRemove. Substracts elementsToRemove from collection.
	 * Both collections may be <code>null</code>.
	 */
	public static <T> Set<T> subtract(Collection<T> collection, Collection<T> elementsToRemove) {
		Set<T> result = new HashSet<>();
		if (collection != null) {
			result.addAll(collection);
		}
		if (elementsToRemove != null) {
			result.removeAll(elementsToRemove);
		}
		return result;
	}

	/**
	 * Adds all elements of collection2 to collection1. collection2 may be
	 * <code>null</code>, in which case nothing happens.
	 */
	public static <T> void addAllSafe(Collection<T> collection1, Collection<T> collection2) {
		if (collection2 != null) {
			collection1.addAll(collection2);
		}
	}

	/**
	 * Return a set containing the intersection of all provided collections. We use
	 * a {@link HashSet}, i.e. the elements should support hashing.
	 *
	 * We use two separate arguments to ensure on the interface level that at least
	 * one collection is provided. This is transparent for the caller.
	 */
	@SafeVarargs
	public static <T> HashSet<T> intersectionSet(Collection<T> collection1, Collection<T>... furtherCollections) {
		HashSet<T> result = new HashSet<>(collection1);
		for (Collection<T> collection : furtherCollections) {
			if (collection instanceof Set) {
				result.retainAll(collection);
			} else {
				// if the collection is not already a set, it will be
				// significantly faster to first build a set, to speed up the
				// containment query in the following call.
				result.retainAll(new HashSet<>(collection));
			}
		}
		return result;
	}

	/**
	 * Returns the set-theoretic difference between the first and the additional
	 * collections, i.e. a set containing all elements that occur in the first, but
	 * not in any of the other collections. We use a {@link HashSet}, so the
	 * elements should support hashing.
	 */
	@SafeVarargs
	public static <T> HashSet<T> differenceSet(Collection<T> collection1,
			Collection<? extends T>... furtherCollections) {
		HashSet<T> result = new HashSet<>(collection1);
		for (Collection<? extends T> collection : furtherCollections) {
			if (collection instanceof Set) {
				result.removeAll(collection);
			} else {
				// if the collection is not already a set, it will be
				// significantly faster to first build a set, to speed up the
				// containment query in the following call.
				result.removeAll(new HashSet<>(collection));
			}
		}
		return result;
	}

	/** Checks whether collection is null or empty */
	public static boolean isNullOrEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	/** Checks whether map is null or empty */
	public static boolean isNullOrEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * Truncates the given list by removing elements from the end such that
	 * numElements entries remain. If the list has less than numElements entries, it
	 * remains unchanged. Thus, this method ensures that the size of the list is <=
	 * numElements.
	 */
	public static void truncateEnd(List<?> list, int numElements) {
		if (list.size() > numElements) {
			list.subList(numElements, list.size()).clear();
		}
	}

	/**
	 * Divides the given set randomly but, as far as possible, evenly into k subsets
	 * of equal size, i.e., if set.size() is not a multiple of k, (k-(set.size()
	 * modulo k)) of the resulting sets have one element less.
	 *
	 * @param <S>
	 *            must support hashing.
	 */
	public static <S> List<Set<S>> divideEvenlyIntoKSubsets(Set<S> set, int k, Random rnd) {
		int n = set.size();
		CCSMAssert.isTrue(n >= k, "The size of the set must be at least k");
		CCSMAssert.isTrue(k > 0, "k must be greater than 0");

		List<S> shuffled = new ArrayList<>(set);
		Collections.shuffle(shuffled, rnd);

		List<Set<S>> result = new ArrayList<>();

		// we can fill (n%k) buckets with (n/k+1) elements
		int subsetSize = n / k + 1;
		for (int i = 0; i < n % k; i++) {
			result.add(new HashSet<>(shuffled.subList(i * subsetSize, (i + 1) * subsetSize)));
		}

		int offset = n % k * subsetSize;

		// fill the rest of the buckets with (n/k) elements
		subsetSize = n / k;
		for (int i = 0; i < k - n % k; i++) {
			result.add(new HashSet<>(shuffled.subList(offset + i * subsetSize, offset + (i + 1) * subsetSize)));
		}

		return result;
	}

	/** Obtain all permutations of the provided elements. */
	public static <T> List<List<T>> getAllPermutations(@SuppressWarnings("unchecked") T... elements) {
		List<List<T>> result = new ArrayList<>();
		permute(Arrays.asList(elements), 0, result);
		return result;
	}

	/** Recursively creates permutations. */
	private static <T> void permute(List<T> list, int index, List<List<T>> result) {
		for (int i = index; i < list.size(); i++) {
			Collections.swap(list, i, index);
			permute(list, index + 1, result);
			Collections.swap(list, index, i);
		}
		if (index == list.size() - 1) {
			result.add(new ArrayList<>(list));
		}
	}

	/**
	 * Returns the power set of the given input list. Note that elements are treated
	 * as unique, i.e. we do not really use set semantics here. Also note that the
	 * returned list has 2^n elements for n input elements, so the input list should
	 * not be too large.
	 */
	public static <T> List<List<T>> getPowerSet(List<T> input) {
		return getPowerSet(input, 0);
	}

	/**
	 * Returns the power set of the given input list, only considering elements at
	 * or after index <code>start</code>.
	 */
	private static <T> List<List<T>> getPowerSet(List<T> input, int start) {
		ArrayList<List<T>> result = new ArrayList<>();
		if (start >= input.size()) {
			result.add(new ArrayList<T>());
		} else {
			T element = input.get(start);
			for (List<T> list : getPowerSet(input, start + 1)) {
				List<T> copy = new ArrayList<>();
				copy.add(element);
				copy.addAll(list);

				result.add(list);
				result.add(copy);
			}
		}
		return result;
	}

	/**
	 * Returns the input list, or {@link #emptyList()} if the input is null.
	 */
	public static <T> List<T> emptyIfNull(List<T> input) {
		if (input == null) {
			return emptyList();
		}
		return input;
	}

	/**
	 * Returns the input map, or uses the given supplier to create a new one.
	 */
	public static <K, V, M extends Map<K, V>> M emptyIfNull(M input, Supplier<M> supplier) {
		return Optional.ofNullable(input).orElseGet(supplier);
	}

	/** Removes an element from the array and returns the new array. */
	@SuppressWarnings("unchecked")
	public static <T> T[] removeElementFromArray(T element, T[] array) {
		ArrayList<T> result = new ArrayList<>(Arrays.asList(array));
		result.remove(element);
		return toArray(result, (Class<T>) element.getClass());
	}

	/**
	 * Returns a new list containing only the non-null elements of the given list.
	 */
	public static <T> List<T> filterNullEntries(List<T> list) {
		return filter(list, element -> element != null);
	}

	/**
	 * Returns a new list containing only the non-null elements of the given array.
	 */
	public static <T> List<T> filterNullEntries(T[] array) {
		return filterNullEntries(Arrays.asList(array));
	}

	/**
	 * Concatenate two arrays to a new one. See also:
	 * http://stackoverflow.com/a/80503/205903
	 */
	public static <T> T[] concatenateArrays(T[] a, T[] b) {
		int length1 = a.length;
		int length2 = b.length;

		@SuppressWarnings("unchecked")
		T[] newArray = (T[]) Array.newInstance(a.getClass().getComponentType(), length1 + length2);
		System.arraycopy(a, 0, newArray, 0, length1);
		System.arraycopy(b, 0, newArray, length1, length2);

		return newArray;
	}

	/** Returns if any element in the collection matches the predicate. */
	public static <T> boolean anyMatch(Collection<? extends T> collection, Predicate<T> predicate) {
		for (T element : collection) {
			if (predicate.test(element)) {
				return true;
			}
		}
		return false;
	}

	/** Returns if all elements in the collection match the predicate. */
	public static <T> boolean allMatch(Collection<? extends T> collection, Predicate<T> predicate) {
		for (T element : collection) {
			if (!predicate.test(element)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Sorts the given collection topologically. The given
	 * <code>getSuccessors</code> function must establish an order on the elements
	 * of the collection (more details below).
	 * 
	 * The sorting is stable if the stream() iterator on the given collection is
	 * stable.
	 * 
	 * The result of this function is a <code>Pair</code>. The first item of the
	 * pair contains the sorted collection as a list. The second item contains a set
	 * of elements that could not be fit into the topological order (in case there
	 * are cycles in the partial order). This second item is empty if there are no
	 * cycles. Elements of the second item are not contained in the sorted list, so
	 * callers should check whether the second item is empty.
	 * 
	 * Direction of the sorting: if <code>getSuccessors(A).contains(B)</code>, then
	 * <code>A</code> will be before <code>B</code> in the sorted list.
	 * 
	 * Details on the order established by <code>getSuccessors</code>: In
	 * literature, this should be a partial order, but we don't seem to require the
	 * properties of partial orders (reflexivity, transitivity, antisymmetry). We
	 * just need an order relation that maps each element to its direct successors.
	 * In fact this method will be faster when the order relation is smaller, so
	 * forget reflexivity and transitivity. Antisymmetry would be nice (otherwise
	 * you'll get a cycle for sure).
	 * 
	 * If <code>getSuccessors</code> returns null for an element, we will treat this
	 * as if it was an empty collection.
	 * 
	 * @param collection
	 *            the collection to be sorted
	 * @param getSuccessors
	 *            the order relation (described above)
	 * @return a topologically sorted collection
	 */
	public static <T> Pair<List<T>, List<T>> topSort(Collection<T> collection,
			Function<T, Collection<T>> getSuccessors) {
		UnmodifiableSet<T> inputSet = CollectionUtils.asUnmodifiable(new HashSet<>(collection));
		// counts how many unhandled predecessors an element has
		// elements not in this list have no unhandled predecessors
		CounterSet<T> numUnhandledPredecessors = new CounterSet<>();
		for (T element : collection) {
			Collection<? extends T> successors = getSuccessors.apply(element);
			if (successors != null) {
				successors.stream().filter(inputSet::contains).forEach(pred -> numUnhandledPredecessors.inc(pred));
			}
		}
		// elements that have no predecessors (they can be inserted in the
		// sorted list)
		Deque<T> freeElements = new ArrayDeque<>(collection.stream()
				.filter(element -> !numUnhandledPredecessors.contains(element)).collect(Collectors.toList()));
		List<T> result = new ArrayList<>(collection.size());
		while (!freeElements.isEmpty()) {
			T element = freeElements.remove();
			result.add(element);
			handleTopSortElementSuccessors(getSuccessors.apply(element), numUnhandledPredecessors, freeElements,
					inputSet);
		}
		// need to sort for stable result
		List<T> cycle = numUnhandledPredecessors.getKeys().stream().sorted().collect(Collectors.toList());
		return new Pair<>(result, cycle);
	}

	/**
	 * This method is part of the {@link #topSort(Collection, Function)} algorithm.
	 * It handles the successors of one element that has been inserted in the
	 * top-sorted list.
	 * 
	 * This method considers only successors that are part of the given filterSet.
	 * For each of these successors, it decreases the numUnhandledPredecessors entry
	 * by one. If a successor has no predecessors any more after the decrease, it is
	 * added to the given freeElements.
	 */
	private static <T> void handleTopSortElementSuccessors(Collection<? extends T> successors,
			CounterSet<T> numUnhandledPredecessors, Deque<T> freeElements, UnmodifiableSet<T> filterSet) {
		if (successors != null) {
			successors.stream().filter(filterSet::contains).forEach(successor -> {
				numUnhandledPredecessors.inc(successor, -1);
				if (numUnhandledPredecessors.getValue(successor) <= 0) {
					freeElements.add(successor);
					numUnhandledPredecessors.remove(successor);
				}
			});
		}
	}

	/**
	 * Sorts the given collection topologically, even if it contains cycles
	 * (deterministically random elements in the cycle are removed). The given
	 * <code>getSuccessors</code> function must establish an order on the elements
	 * of the collection (more details below). The given
	 * <code>getPredecessors</code> function must establish the reverse order of
	 * <code>getSuccessors</code>.
	 * 
	 * 
	 * The sorting is stable if the stream() iterator on the given collection is
	 * stable.
	 * 
	 * The result of this function is a <code>Pair</code>. The first item of the
	 * pair contains the sorted collection as a list. The second item contains a
	 * list of cycle elements that were removed to enable topological sorting.
	 * 
	 * Direction of the sorting: if <code>getSuccessors(A).contains(B)</code>, then
	 * <code>A</code> will be before <code>B</code> in the sorted list.
	 * 
	 * For detail requirements on <code>getSuccessors</code> and
	 * <code>getPredecessors</code>, see {@link #topSort(Collection, Function)}.
	 * 
	 * @param collection
	 *            the collection to be sorted
	 * @param getSuccessors
	 *            the order relation (described above)
	 * @return a topologically sorted collection
	 */
	public static <T> Pair<List<T>, List<T>> topSortRemoveCycles(Collection<T> collection,
			Function<T, Collection<T>> getSuccessors, Function<T, Collection<T>> getPredecessors) {
		Pair<List<T>, List<T>> topSorted = topSort(collection, getSuccessors);
		if (topSorted.getSecond().isEmpty()) {
			return topSorted;
		}
		/*
		 * Implementation idea: After remove non-cycle vertices alternatingly from "top"
		 * and "bottom" and store them for the result. From the resulting cycle, remove
		 * one vertex and repeat.
		 */
		List<T> removedCycleElements = new ArrayList<>();
		List<T> tailElements = new ArrayList<>();
		List<T> headElements = topSorted.getFirst();
		List<T> cycle = topSorted.getSecond();
		while (!cycle.isEmpty()) {
			Pair<List<T>, List<T>> inverseTopSortedCycle = topSort(cycle, getPredecessors);
			tailElements.addAll(inverseTopSortedCycle.getFirst());
			cycle = inverseTopSortedCycle.getSecond();
			if (!cycle.isEmpty()) {
				removedCycleElements.add(cycle.remove(cycle.size() - 1));
			}
			Pair<List<T>, List<T>> topSortedCycle = topSort(cycle, getSuccessors);
			headElements.addAll(topSortedCycle.getFirst());
			cycle = topSortedCycle.getSecond();
		}
		headElements.addAll(reverse(tailElements));
		return new Pair<>(headElements, removedCycleElements);
	}

	/**
	 * Maps all entries of the given Collection to their string representations.
	 * This method always returns a list. The order of elements in the returned list
	 * corresponds to the order of element returned by input.stream(), i.e., if
	 * input is a List then the order is stable, if input is a set then the order is
	 * random.
	 */
	public static List<String> toStringSet(Collection<?> input) {
		return input.stream().map(entry -> entry.toString()).collect(Collectors.toList());
	}

	/**
	 * Returns a {@link Comparator} that compares lists. Shorter lists are
	 * considered "smaller" than longer lists. If lists have the same length,
	 * elements are compared until one is found that is does not return 0 when
	 * compared to its counterpart. If list lengths are equal and all elements
	 * return 0 on comparison, the returned {@link Comparator} returns 0.
	 */
	public static <L extends List<T>, T extends Comparable<T>> Comparator<L> getListComparator() {
		return new Comparator<L>() {
			@Override
			public int compare(L o1, L o2) {
				if (o1.size() != o2.size()) {
					return o1.size() - o2.size();
				}
				for (int i = 0; i < o1.size(); i++) {
					int currentComparison = o1.get(i).compareTo(o2.get(i));
					if (currentComparison != 0) {
						return currentComparison;
					}
				}
				return 0;
			}
		};
	}

	/**
	 * Returns a {@link Comparator} that compares {@link Pair}s of
	 * {@link Comparable}s. First compares the first elements and if their
	 * comparison returns 0, compares the second elements. If their comparison also
	 * return 0, this {@link Comparator} returns 0.
	 */
	public static <P extends Pair<T, S>, T extends Comparable<T>, S extends Comparable<S>> Comparator<P> getPairComparator() {
		return new Comparator<P>() {
			@Override
			public int compare(P o1, P o2) {
				int firstComparison = o1.getFirst().compareTo(o2.getFirst());
				if (firstComparison != 0) {
					return firstComparison;
				}
				return o1.getSecond().compareTo(o2.getSecond());
			}
		};
	}

	/** Returns an empty {@link Consumer} that does nothing. */
	public static <T> Consumer<T> emptyConsumer() {
		return x -> {
			// empty
		};
	}

	/**
	 * Parses a string representing a list of values to a list of the single values.
	 * The value entries must be either separated by line breaks or by
	 * '{@value #MULTI_VALUE_DELIMITER}'.
	 * 
	 * @throws IllegalArgumentException
	 *             if duplicate commas (empty values) are in the string
	 */
	public static List<String> parseMultiValueStringToList(String valueList) throws IllegalArgumentException {
		List<String> lines = StringUtils.splitWithEscapeCharacter(valueList, "\\n");
		List<String> results = new ArrayList<>();
		for (String line : lines) {
			List<String> result = StringUtils.splitWithEscapeCharacter(line, MULTI_VALUE_DELIMITER);
			for (String string : result) {
				if (StringUtils.isEmpty(string)) {
					throw new IllegalArgumentException("Found duplicate comma (empty value) in list: " + valueList);
				}
			}
			results.addAll(result);
		}
		return results;
	}
}
