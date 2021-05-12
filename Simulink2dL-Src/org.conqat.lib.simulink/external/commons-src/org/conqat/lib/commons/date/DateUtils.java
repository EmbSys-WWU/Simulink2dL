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
package org.conqat.lib.commons.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.commons.error.NeverThrownRuntimeException;
import org.conqat.lib.commons.factory.IFactory;

/**
 * Utility methods for working on date objects.
 */
public class DateUtils {

	/** The number of milliseconds per second */
	public static final long MILLIS_PER_SECOND = 1000;

	/** The number of milliseconds per minute */
	public static final long MILLIS_PER_MINUTE = 1000 * 60;

	/** The number of milliseconds per hour */
	public static final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;

	/** The number of milliseconds per day */
	public static final long MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;

	/** Simple date format, which matches the UI format */
	private static final SimpleDateFormat MMM_DD_YYYY_HH_MM_FORMAT = new SimpleDateFormat("MMM dd yyyy HH:mm");

	/** Simple date format used by {@link #truncateToBeginOfDay(Date)} */
	private static final SimpleDateFormat YYYY_MM_DD_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	/** The factory used to create the date in {@link #getNow()}. */
	private static IFactory<Date, NeverThrownRuntimeException> nowFactory;

	/** Returns the latest date in a collection of dates */
	public static Date getLatest(Collection<Date> dates) {
		if (dates.isEmpty()) {
			return null;
		}
		return Collections.max(dates);
	}

	/** Returns the earliest date in a collection of dates */
	public static Date getEarliest(Collection<Date> dates) {
		if (dates.isEmpty()) {
			return null;
		}
		return Collections.min(dates);
	}

	/**
	 * Returns the earlier of two dates, or null, if one of the dates is null
	 */
	public static Date min(Date d1, Date d2) {
		if (d1 == null || d2 == null) {
			return null;
		}

		if (d1.compareTo(d2) < 0) {
			return d1;
		}
		return d2;
	}

	/** Returns the later of two dates or null, if one of the dates is null. */
	public static Date max(Date d1, Date d2) {
		if (d1 == null || d2 == null) {
			return null;
		}

		if (d2.compareTo(d1) > 0) {
			return d2;
		}
		return d1;
	}

	/**
	 * Retrieves the date which is exactly the given number of days before now.
	 */
	public static Date getDateBefore(int days) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -days);
		return calendar.getTime();
	}

	/**
	 * Returns the current time as a {@link Date} object. This is preferred to
	 * directly calling the {@link Date#Date()} constructor, as this method provides
	 * a central entry point that allows the notion of time to be tweaked (e.g. for
	 * testing, when we want a certain date to be returned).
	 * 
	 * The behavior of this method can be affected either by calling the
	 * {@link #testcode_setNowFactory(IFactory)} method, or by providing a fixed
	 * date in format "yyyyMMddHHmmss" as the system property
	 * "org.conqat.lib.commons.date.now".
	 */
	public static synchronized Date getNow() {
		if (nowFactory == null) {
			String property = System.getProperty("org.conqat.lib.commons.date.now");
			if (property == null) {
				nowFactory = new CurrentDateFactory();
			} else {
				try {
					nowFactory = new FixedDateFactory(new SimpleDateFormat("yyyyMMddHHmmss").parse(property));
				} catch (ParseException e) {
					// fail hard in case of misconfiguration
					throw new RuntimeException("Invalid date string provided via system property: " + property, e);
				}
			}
		}
		return nowFactory.create();
	}

	/**
	 * {@link #getNow()} truncated to the begin of day.
	 */
	public static Date getNowWithoutTime() {
		return truncateToBeginOfDay(getNow());
	}

	/**
	 * Returns the factory that affects the notion of "now" in {@link #getNow()} .
	 * This should be only called from test code!
	 */
	public static synchronized IFactory<Date, NeverThrownRuntimeException> testcode_getNowFactory() {
		return nowFactory;
	}

	/**
	 * Sets the factory that affects the notion of "now" in {@link #getNow()}. This
	 * should be only called from test code!
	 */
	public static synchronized void testcode_setNowFactory(IFactory<Date, NeverThrownRuntimeException> nowFactory) {
		DateUtils.nowFactory = nowFactory;
	}

	/**
	 * Sets the given fixed date for "now" in {@link #getNow()}. This should be only
	 * called from test code!
	 */
	public static synchronized void testcode_setFixedDate(Date date) {
		testcode_setNowFactory(new FixedDateFactory(date));
	}

	/** Reset the now factory to the default one. */
	public static synchronized void testcode_resetNowFactory() {
		nowFactory = null;
	}

	/** A factory returning a fixed date. */
	public static class FixedDateFactory implements IFactory<Date, NeverThrownRuntimeException> {

		/** The date used as now. */
		private final Date now;

		/** Constructor. */
		public FixedDateFactory(Date now) {
			this.now = now;
		}

		/** {@inheritDoc} */
		@Override
		public Date create() throws NeverThrownRuntimeException {
			return (Date) now.clone();
		}
	}

	/** A factory returning the current date. */
	public static class CurrentDateFactory implements IFactory<Date, NeverThrownRuntimeException> {

		/** {@inheritDoc} */
		@Override
		public Date create() throws NeverThrownRuntimeException {
			return new Date();
		}
	}

	/** Returns a new Date that is one day later than the given date. */
	public static Date incrementByOneDay(Date date) {
		return addDaysToDate(date, 1);
	}

	/**
	 * Returns a normalized version of the given date. Normalization is done by
	 * removing all time-of-day information.
	 */
	public static Date truncateToBeginOfDay(Date date) {
		if (date == null) {
			return null;
		}

		synchronized (YYYY_MM_DD_FORMAT) {
			String normalized = YYYY_MM_DD_FORMAT.format(date);
			try {
				return YYYY_MM_DD_FORMAT.parse(normalized);
			} catch (ParseException e) {
				throw new AssertionError("ParseException.", e);
			}
		}
	}

	/** Converts the given number of days into milliseconds. */
	public static long daysToMilliseconds(int days) {
		return days * MILLIS_PER_DAY;
	}

	/** Converts the given number of hours into milliseconds. */
	public static long hoursToMilliseconds(int hours) {
		return hours * MILLIS_PER_HOUR;
	}

	/** Converts the given number of minutes into milliseconds. */
	public static long minutesToMilliseconds(int minutes) {
		return minutes * MILLIS_PER_MINUTE;
	}

	/**
	 * Returns the difference between two dates in the given time unit.
	 */
	public static long diff(Date earlier, Date later, TimeUnit timeUnit) {
		long diffInMillies = later.getTime() - earlier.getTime();
		return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
	}

	/**
	 * Returns the difference between two dates in days.
	 */
	public static long diffDays(Date earlier, Date later) {
		return diff(earlier, later, TimeUnit.DAYS);
	}

	/** Create a new date and add <code>days</code> to it. */
	public static Date addDaysToDate(Date date, int days) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_YEAR, days);
		return calendar.getTime();
	}

	/** Create a new date and subtracts <code>days</code> from it. */
	public static Date subtractDaysFromDate(Date date, int days) {
		return addDaysToDate(date, -days);
	}

	/**
	 * Get an ordered list of dates that includes <code>startDate</code>, date
	 * points in between start and end date and the <code>endDate</code> if
	 * <code>includeEndDate</code> is true.
	 */
	public static List<Date> getDatePointsInTimespan(Date startDate, Date endDate, int daysBetweenDatePoints,
			boolean includeEndDate) {
		CCSMAssert.isNotNull(startDate);
		CCSMAssert.isNotNull(endDate);
		CCSMAssert.isFalse(startDate.after(endDate), "startDate is after endDate");
		CCSMAssert.isTrue(daysBetweenDatePoints > 0, "daysBetweenDatePoints must be > 0");

		List<Date> result = new ArrayList<>();
		result.add(startDate);

		Date currentDate = startDate;

		while (true) {
			currentDate = addDaysToDate(currentDate, daysBetweenDatePoints);

			if (!currentDate.before(endDate)) {
				break;
			}

			result.add(currentDate);
		}

		if (includeEndDate) {
			result.add(endDate);
		}

		return result;
	}

	/**
	 * Get the timestamp of a date. Returns null if <code>date</code> is null.
	 */
	public static Long getTimeOfDate(Date date) {
		if (date == null) {
			return null;
		}

		return date.getTime();
	}

	/**
	 * Create a date.
	 * 
	 * @param month
	 *            zero-based index
	 */
	public static Date createDate(int day, int month, int year) {
		if (month == 12) {
			throw new IllegalArgumentException("month is zero-based, use the Calendar instances");
		}

		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day);
		return DateUtils.truncateToBeginOfDay(calendar.getTime());
	}

	/**
	 * Takes a <code>timestamp</code> in milliseconds, creates a date based on this
	 * timestamp and returns the UI formatted String representation of this date.
	 */
	public static String getUiFormattedDateString(long milliseconds) {
		Date date = new Date(milliseconds);
		return MMM_DD_YYYY_HH_MM_FORMAT.format(date);
	}

	/** Formats a duration in a human-readable way (e.g. 1h 20min 5s 123ms). */
	public static String formatDurationHumanReadable(Duration duration) {
		long milliseconds = duration.toMillis() % 1000;
		long seconds = (duration.toMillis() / 1000) % 60;
		long minutes = (duration.toMillis() / 1000 / 60) % 60;
		long hours = (duration.toMillis() / 1000 / 60 / 60);

		StringBuilder builder = new StringBuilder();
		if (hours > 0) {
			builder.append(hours).append("h ");
		}
		if (hours > 0 || minutes > 0) {
			builder.append(minutes).append("min ");
		}
		if (hours > 0 || minutes > 0 || seconds > 0) {
			builder.append(seconds).append("s ");
		}
		builder.append(milliseconds).append("ms");
		return builder.toString();
	}
}