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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.commons.collections.CollectionUtils.FunctionWithException;
import org.conqat.lib.commons.equals.HashCodeUtils;

/**
 * A list for storing pairs in a specific order.
 */
public class PairList<S, T> implements Serializable, Iterable<Pair<S, T>> {

	/** Version used for serialization. */
	private static final long serialVersionUID = 1;

	/** The current size. */
	private int size = 0;

	/** The array used for storing the S. */
	private Object[] firstElements;

	/** The array used for storing the T. */
	private Object[] secondElements;

	/** Constructor. */
	public PairList() {
		this(16);
	}

	/** Constructor. */
	public PairList(int initialCapacity) {
		if (initialCapacity < 1) {
			initialCapacity = 1;
		}
		firstElements = new Object[initialCapacity];
		secondElements = new Object[initialCapacity];
	}

	/** Copy constructor. */
	public PairList(PairList<S, T> other) {
		this(other.size);
		addAll(other);
	}

	/**
	 * Constructor to convert a map into a pair list.
	 */
	public PairList(Map<S, T> map) {
		this(map.size());
		for (Entry<S, T> entry : map.entrySet()) {
			add(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Creates a new pair list initialized with a single key/value pair. This is
	 * especially helpful for construction of small pair lists, as type inference
	 * reduces writing overhead.
	 */
	public static <S, T> PairList<S, T> from(S key, T value) {
		PairList<S, T> result = new PairList<>();
		result.add(key, value);
		return result;
	}

	/** Returns whether the list is empty. */
	public boolean isEmpty() {
		return size == 0;
	}

	/** Returns the size of the list. */
	public int size() {
		return size;
	}

	/** Add the given pair to the list. */
	public void add(S first, T second) {
		ensureSpace(size + 1);
		firstElements[size] = first;
		secondElements[size] = second;
		++size;
	}

	/** Adds all pairs from another list. */
	public void addAll(PairList<S, T> other) {
		// we have to store this in a local var, as other.size may change if
		// other == this
		int otherSize = other.size;

		ensureSpace(size + otherSize);
		for (int i = 0; i < otherSize; ++i) {
			firstElements[size] = other.firstElements[i];
			secondElements[size] = other.secondElements[i];
			++size;
		}
	}

	/** Make sure there is space for at least the given amount of elements. */
	protected void ensureSpace(int space) {
		if (space <= firstElements.length) {
			return;
		}

		Object[] oldFirst = firstElements;
		Object[] oldSecond = secondElements;
		int newSize = firstElements.length * 2;
		while (newSize < space) {
			newSize *= 2;
		}

		firstElements = new Object[newSize];
		secondElements = new Object[newSize];
		System.arraycopy(oldFirst, 0, firstElements, 0, size);
		System.arraycopy(oldSecond, 0, secondElements, 0, size);
	}

	/** Returns the first element at given index. */
	@SuppressWarnings("unchecked")
	public S getFirst(int i) {
		checkWithinBounds(i);
		return (S) firstElements[i];
	}

	/**
	 * Checks whether the given <code>i</code> is within the bounds. Throws an
	 * exception otherwise.
	 */
	private void checkWithinBounds(int i) {
		if (i < 0 || i >= size) {
			throw new IndexOutOfBoundsException("Out of bounds: " + i);
		}
	}

	/** Sets the first element at given index. */
	public void setFirst(int i, S value) {
		checkWithinBounds(i);
		firstElements[i] = value;
	}

	/** Returns the second element at given index. */
	@SuppressWarnings("unchecked")
	public T getSecond(int i) {
		checkWithinBounds(i);
		return (T) secondElements[i];
	}

	/** Sets the first element at given index. */
	public void setSecond(int i, T value) {
		checkWithinBounds(i);
		secondElements[i] = value;
	}

	/** Creates a new list containing all first elements. */
	@SuppressWarnings("unchecked")
	public List<S> extractFirstList() {
		List<S> result = new ArrayList<S>(size + 1);
		for (int i = 0; i < size; ++i) {
			result.add((S) firstElements[i]);
		}
		return result;
	}

	/** Creates a new list containing all second elements. */
	@SuppressWarnings("unchecked")
	public List<T> extractSecondList() {
		List<T> result = new ArrayList<T>(size + 1);
		for (int i = 0; i < size; ++i) {
			result.add((T) secondElements[i]);
		}
		return result;
	}

	/**
	 * Swaps the pairs of this list. Is S and T are different types, this will be
	 * extremely dangerous.
	 */
	public void swapPairs() {
		Object[] temp = firstElements;
		firstElements = secondElements;
		secondElements = temp;
	}

	/** Swaps the entries located at indexes i and j. */
	public void swapEntries(int i, int j) {
		S tmp1 = getFirst(i);
		T tmp2 = getSecond(i);
		setFirst(i, getFirst(j));
		setSecond(i, getSecond(j));
		setFirst(j, tmp1);
		setSecond(j, tmp2);
	}

	/** Clears this list. */
	public void clear() {
		size = 0;
	}

	/** Removes the last element of the list. */
	public void removeLast() {
		CCSMAssert.isTrue(size > 0, "Size must be positive!");
		size -= 1;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append('[');
		for (int i = 0; i < size; i++) {
			if (i != 0) {
				result.append(',');
			}
			result.append('(');
			result.append(String.valueOf(firstElements[i]));
			result.append(',');
			result.append(String.valueOf(secondElements[i]));
			result.append(')');
		}
		result.append(']');
		return result.toString();
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		int prime = 31;
		int hash = size;
		hash = prime * hash + HashCodeUtils.hashArrayPart(firstElements, 0, size);
		return prime * hash + HashCodeUtils.hashArrayPart(secondElements, 0, size);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PairList)) {
			return false;
		}

		PairList<?, ?> other = (PairList<?, ?>) obj;
		if (size != other.size) {
			return false;
		}
		for (int i = 0; i < size; i++) {
			if (!Objects.equals(firstElements[i], other.firstElements[i])
					|| !Objects.equals(secondElements[i], secondElements[i])) {
				return false;
			}
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<Pair<S, T>> iterator() {
		return new Iterator<Pair<S, T>>() {
			int index = 0;

			/** {@inheritDoc} */
			@Override
			public boolean hasNext() {
				return index < size;
			}

			/** {@inheritDoc} */
			@Override
			public Pair<S, T> next() {
				checkWithinBounds(index);
				int oldIndex = index;
				index++;
				return createPairForIndex(oldIndex);
			}
		};
	}

	/**
	 * Returns a non-parallel stream of {@link Pair}s from this list.
	 * <p>
	 * NOTE: the underlying {@link Spliterator} implementation does currently not
	 * support splitting. Thus, trying to use the returned stream in parallel
	 * execution will fail.
	 *
	 * @see PairListSpliterator#trySplit()
	 */
	public Stream<Pair<S, T>> stream() {
		return StreamSupport.stream(new PairListSpliterator(), false);
	}

	/**
	 * Converts this {@link PairList} into a {@link Map}.
	 *
	 * If the first elements contain duplicates (according to
	 * {@link Object#equals(Object)}, an {@link IllegalStateException} is thrown. If
	 * the first elements may have duplicates, use {@link #toMap(BinaryOperator)}
	 * instead.
	 */
	public Map<S, T> toMap() {
		Map<S, T> result = new HashMap<>();
		// must use explicit put, as stream collector does not support null
		// values
		forEach((key, value) -> result.put(key, value));
		return result;
	}

	/**
	 * Converts this {@link PairList} into a {@link Map}.
	 *
	 * If the first elements contain duplicates (according to
	 * {@link Object#equals(Object)}, the respective second elements are merged
	 * using the provided merging function..
	 */
	public Map<S, T> toMap(BinaryOperator<T> mergeFunction) {
		return stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, mergeFunction));
	}

	/**
	 * Creates a pair from the values at the given index in this list.
	 *
	 * We suppress unchecked cast warnings since the PairList stores all elements as
	 * plain Objects.
	 */
	@SuppressWarnings("unchecked")
	private Pair<S, T> createPairForIndex(int index) {
		return new Pair<S, T>((S) firstElements[index], (T) secondElements[index]);
	}

	/**
	 * Spliterator for the {@link PairList}.
	 */
	private final class PairListSpliterator implements Spliterator<Pair<S, T>> {

		/**
		 * The next element to process when {@link #tryAdvance(Consumer)} is called.
		 */
		private int nextElementToProcess;

		/** {@inheritDoc} */
		@Override
		public int characteristics() {
			// the Spliterator is not subsized since we don't support splitting
			// at the moment
			return Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.SIZED;
		}

		/** {@inheritDoc} */
		@Override
		public long estimateSize() {
			return size - nextElementToProcess;
		}

		/** {@inheritDoc} */
		@Override
		public boolean tryAdvance(Consumer<? super Pair<S, T>> action) {
			if (nextElementToProcess == size) {
				return false;
			}

			action.accept(createPairForIndex(nextElementToProcess));
			nextElementToProcess++;
			return true;
		}

		/**
		 * {@inheritDoc}
		 *
		 * We return <code>null</code> to indicate that this {@link Spliterator} cannot
		 * be split. If parallel execution is required, this method must be implemented
		 * and the subsized characteristic should be adhered to.
		 */
		@Override
		public Spliterator<Pair<S, T>> trySplit() {
			return null;
		}
	}

	/**
	 * Collects a stream of pairs into a {@link PairList}.
	 */
	private static class PairListCollector<S, T> implements Collector<Pair<S, T>, PairList<S, T>, PairList<S, T>> {

		/** {@inheritDoc} */
		@Override
		public Supplier<PairList<S, T>> supplier() {
			return PairList::new;
		}

		/** {@inheritDoc} */
		@Override
		public BiConsumer<PairList<S, T>, Pair<S, T>> accumulator() {
			return (list, pair) -> list.add(pair.getFirst(), pair.getSecond());
		}

		/** {@inheritDoc} */
		@Override
		public BinaryOperator<PairList<S, T>> combiner() {
			return (list1, list2) -> {
				list1.addAll(list2);
				return list1;
			};
		}

		/** {@inheritDoc} */
		@Override
		public Function<PairList<S, T>, PairList<S, T>> finisher() {
			return list -> list;
		}

		/** {@inheritDoc} */
		@Override
		public Set<java.util.stream.Collector.Characteristics> characteristics() {
			return EnumSet.of(Characteristics.IDENTITY_FINISH);
		}

	}

	/**
	 * Returns a collector for collecting a stream of pairs into a {@link PairList}.
	 */
	public static <S, T> PairListCollector<S, T> toPairList() {
		return new PairListCollector<>();
	}

	/**
	 * Creates a {@link PairList} that consists of pairs of values taken from the
	 * two {@link List}s. I.e. entry i in the resulting {@link PairList} will
	 * consists of the pair (a, b) where a is the entry at index i in the first
	 * collection and b is the entry at index i of the second collection.
	 *
	 * Both collections must be of the same size. The order of insertion into the
	 * new {@link PairList} is determined by the order imposed by the collections'
	 * iterators.
	 */
	public static <S, T> PairList<S, T> zip(List<S> firstValues, List<T> secondValues) {
		CCSMAssert.isTrue(firstValues.size() == secondValues.size(),
				"Can only zip together collections of the same size.");
		PairList<S, T> result = new PairList<>(firstValues.size());
		Iterator<S> firstIterator = firstValues.iterator();
		Iterator<T> secondIterator = secondValues.iterator();
		while (firstIterator.hasNext()) {
			result.add(firstIterator.next(), secondIterator.next());
		}
		return result;
	}

	/**
	 * For each element pair in this list, calls the given consumer with the first
	 * and second value from the pair as the only arguments.
	 */
	public void forEach(BiConsumer<S, T> consumer) {
		for (int i = 0; i < size; i++) {
			consumer.accept(getFirst(i), getSecond(i));
		}
	}

	/**
	 * Returns a new {@link PairList}, where both mappers are applied to the
	 * elements of the current {@link PairList} (input for each mapper is the pair
	 * of elements at each position).
	 */
	public <S2, T2> PairList<S2, T2> map(BiFunction<S, T, S2> firstMapper, BiFunction<S, T, T2> secondMapper) {
		PairList<S2, T2> result = new PairList<S2, T2>(size);
		forEach((key, value) -> result.add(firstMapper.apply(key, value), secondMapper.apply(key, value)));
		return result;
	}

	/**
	 * Returns a new {@link PairList}, where the first elements are obtained by
	 * applying the given mapper function to the first elements of this list.
	 */
	public <U, E extends Exception> PairList<U, T> mapFirst(FunctionWithException<S, U, ? extends E> mapper) throws E {
		PairList<U, T> mappedList = new PairList<>(size);
		for (int i = 0; i < size; i++) {
			mappedList.add(mapper.apply(getFirst(i)), getSecond(i));
		}
		return mappedList;
	}

	/**
	 * Returns a new {@link PairList}, where the second elements are obtained by
	 * applying the given mapper function to the second elements of this list.
	 */
	public <U, E extends Exception> PairList<S, U> mapSecond(FunctionWithException<T, U, ? extends E> mapper) throws E {
		PairList<S, U> mappedList = new PairList<S, U>(size);
		for (int i = 0; i < size; i++) {
			mappedList.add(getFirst(i), mapper.apply(getSecond(i)));
		}
		return mappedList;
	}

	/** Returns a reversed copy of this list. */
	public PairList<S, T> reverse() {
		PairList<S, T> reversed = new PairList<>();
		for (int i = size() - 1; i >= 0; i--) {
			reversed.add(getFirst(i), getSecond(i));
		}
		return reversed;
	}

	/**
	 * Returns a newly allocated array containing all of the elements in this
	 * PairList as an array of Pair<S,T>s.
	 */
	@SuppressWarnings("unchecked")
	public Pair<S, T>[] toArray() {
		Pair<S, T>[] result = new Pair[size];
		for (int i = 0; i < size; i++) {
			result[i] = new Pair<>((S) firstElements[i], (T) secondElements[i]);
		}
		return result;
	}

	/**
	 * Sorts the PairList according to the given comparator.
	 */
	public void sort(Comparator<Pair<S, T>> comparator) {
		Pair<S, T>[] sorted = this.toArray();
		Arrays.sort(sorted, comparator);
		for (int i = 0; i < sorted.length; i++) {
			setFirst(i, sorted[i].getFirst());
			setSecond(i, sorted[i].getSecond());
		}
	}

	/**
	 * The empty list (immutable). This list is serializable.
	 */
	@SuppressWarnings("rawtypes")
	private static final PairList EMPTY_PAIR_LIST = new PairList<Object, Object>(Collections.emptyMap()) {

		/** default */
		private static final long serialVersionUID = 1;

		/** {@inheritDoc} */
		@Override
		public Iterator<Pair<Object, Object>> iterator() {
			return Collections.emptyIterator();
		}

		/** {@inheritDoc} */
		@Override
		public Spliterator<Pair<Object, Object>> spliterator() {
			return Spliterators.emptySpliterator();
		}

		/** {@inheritDoc} */
		@Override
		public void add(Object first, Object second) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public void addAll(PairList<Object, Object> other) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public List<Object> extractFirstList() {
			return Collections.emptyList();
		}

		/** {@inheritDoc} */
		@Override
		public List<Object> extractSecondList() {
			return Collections.emptyList();
		}
	};

	/**
	 * Returns an empty list (immutable). This list is serializable. This is
	 * inspired by the Java Collections.emptyList() method.
	 *
	 * <p>
	 * This example illustrates the type-safe way to obtain an empty Pairlist:
	 *
	 * <pre>
	 * PairList&lt;String, String&gt; s = PairList.emptyPairList();
	 * </pre>
	 *
	 * @param <S>
	 *            key type of elements, if there were any, in the list
	 * @param <T>
	 *            value type of elements, if there were any, in the list
	 * @return an empty immutable list
	 *
	 */
	@SuppressWarnings("unchecked")
	public static final <S, T> PairList<S, T> emptyPairList() {
		return EMPTY_PAIR_LIST;
	}

	/** Add the given pair to the list, if it is present. */
	public void addIfPresent(Optional<Pair<S, T>> pair) {
		if (pair.isPresent()) {
			add(pair.get().getFirst(), pair.get().getSecond());
		}
	}

	/**
	 * Maps a pair list to another one using separate mappers for keys and values.
	 */
	public <S2, T2> PairList<S2, T2> map(Function<S, S2> keyMapper, Function<T, T2> valueMapper) {
		PairList<S2, T2> result = new PairList<>();
		forEach((key, value) -> result.add(keyMapper.apply(key), valueMapper.apply(value)));
		return result;
	}
}
