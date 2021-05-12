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
package org.conqat.lib.commons.assertion;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.string.StringUtils;

/**
 * This class provides simple methods to implement assertions. Please refer to
 * the {@linkplain org.conqat.lib.commons.assertion package documentation} for a
 * discussion of assertions vs preconditions.
 */
public class CCSMAssert {

	/**
	 * Checks if a condition is <code>true</code>.
	 * 
	 * @param condition
	 *                      condition to check
	 * @param message
	 *                      exception message
	 * @throws AssertionError
	 *             if the condition is <code>false</code>
	 */
	public static void isTrue(boolean condition, String message) throws AssertionError {
		throwAssertionErrorIfTestFails(condition, message);
	}

	/**
	 * Checks if a condition is <code>true</code>.
	 *
	 * @param condition
	 *            condition to check
	 * @param messageSupplier
	 *            supplier for the exception message evaluated in case the assertion
	 *            fails
	 * @throws AssertionError
	 *             if the condition is <code>false</code>
	 */
	public static void isTrue(boolean condition, Supplier<String> messageSupplier) throws AssertionError {
		throwAssertionErrorIfTestFails(condition, messageSupplier);
	}

	/**
	 * Checks if a condition is <code>false</code>.
	 * 
	 * @param condition
	 *                      condition to check
	 * @param message
	 *                      exception message
	 * @throws AssertionError
	 *             if the condition is <code>true</code>
	 */
	public static void isFalse(boolean condition, String message) throws AssertionError {
		throwAssertionErrorIfTestFails(!condition, message);
	}

	/**
	 * Checks if a condition is <code>false</code>.
	 *
	 * @param condition
	 *            condition to check
	 * @param messageSupplier
	 *            supplier for the exception message evaluated in case the assertion
	 *            fails
	 * @throws AssertionError
	 *             if the condition is <code>true</code>
	 */
	public static void isFalse(boolean condition, Supplier<String> messageSupplier) throws AssertionError {
		throwAssertionErrorIfTestFails(!condition, messageSupplier);
	}

	/** Checks that the object is a instance of the class. */
	public static void isInstanceOf(Object object, Class<?> clazz) {
		CCSMAssert.isNotNull(clazz, () -> "Object " + object + " can't be an instance of class null.");
		CCSMAssert.isTrue(clazz.isInstance(object), () -> {
			// Object could be null. Hence the concatenation with the empty string.
			String message = object + "";
			if (object != null) {
				message += " of type " + object.getClass().getName();
			}
			return message + " must be an instance of " + clazz.getClass().getName();
		});
	}

	/**
	 * This calls {@link #isInstanceOf(Object, Class)} and, if this doesn't fail
	 * returns the casted object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T checkedCast(Object object, Class<T> clazz) {
		isInstanceOf(object, clazz);
		return (T) object;
	}

	/**
	 * @throws AssertionError
	 *                            with message
	 */
	public static void fail(String message) throws AssertionError {
		throw new AssertionError(message);
	}

	/**
	 * Checks whether a reference is <code>null</code>.
	 * 
	 * @param reference
	 *                      reference to check
	 * @throws AssertionError
	 *                            if the reference is <code>null</code>
	 */
	public static void isNotNull(Object reference) throws AssertionError {
		isNotNull(reference, "Reference must not be null");
	}

	/**
	 * Checks whether a reference is <code>null</code>.
	 * 
	 * @param reference
	 *                      reference to check
	 * @param message
	 *                      exception message
	 * @throws AssertionError
	 *                            if the reference is <code>null</code>
	 */
	public static void isNotNull(Object reference, String message) throws AssertionError {
		throwAssertionErrorIfTestFails(reference != null, message);
	}

	/**
	 * Checks whether a reference is <code>null</code>.
	 *
	 * @param reference
	 *            reference to check
	 * @param messageSupplier
	 *            supplier for the exception message evaluated in case the assertion
	 *            fails
	 * @throws AssertionError
	 *             if the reference is <code>null</code>
	 */
	public static void isNotNull(Object reference, Supplier<String> messageSupplier) throws AssertionError {
		throwAssertionErrorIfTestFails(reference != null, messageSupplier);
	}

	/** @see #isNotNull(Object, Supplier) */
	public static void isNotEmpty(String s, String message) throws AssertionError {
		throwAssertionErrorIfTestFails(!StringUtils.isEmpty(s), message);
	}

	/**
	 * Checks whether a String is <code>null</code> or empty.
	 * 
	 * @param s
	 *            String to check
	 * @param messageSupplier
	 *            supplier for the exception message evaluated in case the assertion
	 *            fails
	 * @throws AssertionError
	 *                            if the String is empty
	 */
	public static void isNotEmpty(String s, Supplier<String> messageSupplier) throws AssertionError {
		throwAssertionErrorIfTestFails(!StringUtils.isEmpty(s), messageSupplier);
	}

	/** @see #isNotEmpty(Collection, Supplier) */
	public static void isNotEmpty(Collection<?> collection, String message) throws AssertionError {
		throwAssertionErrorIfTestFails(!CollectionUtils.isNullOrEmpty(collection), message);
	}

	/**
	 * Checks whether a collection is <code>null</code> or empty.
	 * 
	 * @param collection
	 *            Collection to check
	 * @param messageSupplier
	 *            supplier for the exception message evaluated in case the assertion
	 *            fails
	 * @throws AssertionError
	 *                            if the collection is empty
	 */
	public static void isNotEmpty(Collection<?> collection, Supplier<String> messageSupplier) throws AssertionError {
		throwAssertionErrorIfTestFails(!CollectionUtils.isNullOrEmpty(collection), messageSupplier);
	}

	/** @see #isPresent(Optional, Supplier) */
	public static void isPresent(Optional<?> optional, String message) {
		throwAssertionErrorIfTestFails(optional.isPresent(), message);
	}

	/**
	 * Checks whether an Optional is present.
	 * 
	 * @param optional
	 *            Optional to check
	 * @param messageSupplier
	 *            supplier for the exception message evaluated in case the assertion
	 *            fails
	 * @throws AssertionError
	 *             if the Optional is not present
	 */
	public static void isPresent(Optional<?> optional, Supplier<String> messageSupplier) {
		throwAssertionErrorIfTestFails(optional.isPresent(), messageSupplier);
	}

	/**
	 * Throws an {@link AssertionError} if the test fails.
	 *
	 * @param test
	 *            test which should be true
	 * @param messageSupplier
	 *            supplier for a message evaluated only in case the assertion fails
	 * @throws AssertionError
	 *             if the test fails
	 */
	private static void throwAssertionErrorIfTestFails(boolean test, Supplier<String> messageSupplier) {
		if (!test) {
			throw new AssertionError(messageSupplier.get());
		}
	}

	/**
	 * Throws an {@link AssertionError} if the test fails.
	 *
	 * @param test
	 *            test which should be true
	 * @param message
	 *                     exception message
	 * @throws AssertionError
	 *             if the test fails
	 */
	private static void throwAssertionErrorIfTestFails(boolean test, String message) {
		if (!test) {
			throw new AssertionError(message);
		}
	}
}