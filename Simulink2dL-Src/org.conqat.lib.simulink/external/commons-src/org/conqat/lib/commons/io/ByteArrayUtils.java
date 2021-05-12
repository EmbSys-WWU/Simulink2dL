/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright 2005-2011 the ConQAT Project                                   |
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
package org.conqat.lib.commons.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.OptionalLong;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.commons.filesystem.FileSystemUtils;
import org.conqat.lib.commons.string.StringUtils;

/**
 * Utility methods for dealing with raw byte arrays. This is located in the I/O
 * package, as the typical application for these methods is binary I/O on byte
 * array level.
 */
public class ByteArrayUtils {

	/**
	 * Converts an integer value to a byte array. The returned array has a length of
	 * {@link Integer#BYTES}.
	 */
	public static byte[] intToByteArray(int value) {
		byte[] bytes = new byte[Integer.BYTES];
		storeIntInStartOfArray(value, bytes);
		return bytes;
	}

	/** Stores the given int at the first 4 bytes of the array. */
	public static void storeIntInStartOfArray(int value, byte[] bytes) {
		bytes[0] = (byte) (value >> 24);
		bytes[1] = (byte) (value >> 16);
		bytes[2] = (byte) (value >> 8);
		bytes[3] = (byte) (value);
	}

	/**
	 * Converts a double value to a byte array. The returned array has a length of
	 * {@link Double#BYTES}
	 */
	public static byte[] doubleToByteArray(double value) {
		long longBits = Double.doubleToRawLongBits(value);
		return ByteArrayUtils.longToByteArray(longBits);
	}

	/**
	 * Converts a long value to a byte array. The returned array has a length of
	 * {@link Long#BYTES}
	 */
	public static byte[] longToByteArray(long value) {
		byte[] bytes = new byte[Long.BYTES];
		bytes[0] = (byte) (value >> 56);
		bytes[1] = (byte) (value >> 48);
		bytes[2] = (byte) (value >> 40);
		bytes[3] = (byte) (value >> 32);
		bytes[4] = (byte) (value >> 24);
		bytes[5] = (byte) (value >> 16);
		bytes[6] = (byte) (value >> 8);
		bytes[7] = (byte) (value);
		return bytes;
	}

	/**
	 * Converts a byte array to an integer value.
	 * 
	 * Overall, this method is only guaranteed to work if the input array was
	 * created by {@link #intToByteArray(int)}.
	 */
	public static int byteArrayToInt(byte[] bytes) {
		CCSMAssert.isTrue(bytes.length == Integer.BYTES, "bytes.length must be 4");
		return readIntFromStartOfArray(bytes);
	}

	/**
	 * Reads an int stored with {@link #storeIntInStartOfArray(int, byte[])} from
	 * the first 4 bytes of the array.
	 */
	public static int readIntFromStartOfArray(byte[] bytes) {
		int value = 0;
		value |= unsignedByte(bytes[0]) << 24;
		value |= unsignedByte(bytes[1]) << 16;
		value |= unsignedByte(bytes[2]) << 8;
		value |= unsignedByte(bytes[3]);
		return value;
	}

	/**
	 * Converts a byte array to a double value.
	 * 
	 * Overall, this method is only guaranteed to work if the input array was
	 * created by {@link #doubleToByteArray(double)}.
	 */
	public static double byteArrayToDouble(byte[] value) {
		long longBits = ByteArrayUtils.byteArrayToLong(value);
		return Double.longBitsToDouble(longBits);
	}

	/**
	 * Converts a byte array to a long value.
	 * 
	 * Overall, this method is only guaranteed to work if the input array was
	 * created by {@link #longToByteArray(long)}.
	 */
	public static long byteArrayToLong(byte[] bytes) {
		CCSMAssert.isTrue(bytes.length == Long.BYTES, "bytes.length must be 8");
		long value = 0l;
		value |= unsignedByteAsLong(bytes[0]) << 56;
		value |= unsignedByteAsLong(bytes[1]) << 48;
		value |= unsignedByteAsLong(bytes[2]) << 40;
		value |= unsignedByteAsLong(bytes[3]) << 32;
		value |= unsignedByteAsLong(bytes[4]) << 24;
		value |= unsignedByteAsLong(bytes[5]) << 16;
		value |= unsignedByteAsLong(bytes[6]) << 8;
		value |= unsignedByteAsLong(bytes[7]);
		return value;
	}

	/**
	 * Converts a byte array to an optional long value, by mapping a null input
	 * array to empty.
	 */
	public static OptionalLong byteArrayToOptionalLong(byte[] bytes) {
		if (bytes == null) {
			return OptionalLong.empty();
		}
		return OptionalLong.of(byteArrayToLong(bytes));
	}

	/**
	 * Decompresses a single byte[] using GZIP. A null input array will cause this
	 * method to return null.
	 * 
	 * @throws IOException
	 *             if the input array is not valid GZIP compressed data (as created
	 *             by {@link #compress(byte[])}).
	 */
	public static byte[] decompress(byte[] value) throws IOException {
		if (value == null) {
			return null;
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream(value.length);
		ByteArrayInputStream bis = new ByteArrayInputStream(value);
		GZIPInputStream gzis = new GZIPInputStream(bis);

		FileSystemUtils.copy(gzis, bos);

		// it does not matter if we close in case of exceptions, as these are
		// in-memory resources
		gzis.close();
		bos.close();

		return bos.toByteArray();
	}

	/**
	 * Compresses a single byte[] using GZIP. A null input array will cause this
	 * method to return null.
	 */
	public static byte[] compress(byte[] value) {
		if (value == null) {
			return null;
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream(value.length);
		try {
			GZIPOutputStream gzos = new GZIPOutputStream(bos);
			gzos.write(value);

			// it does not matter if we close in case of exceptions, as this is
			// an in-memory resource
			gzos.close();
		} catch (IOException e) {
			throw new AssertionError("Can not happen as we work in memory: " + e.getMessage());
		}

		return bos.toByteArray();
	}

	/** Returns whether the prefix is a prefix of the given key. */
	public static boolean isPrefix(byte[] prefix, byte[] key) {
		return isPrefix(prefix, key, 0);
	}

	/**
	 * Returns whether the <code>prefix</code> is a prefix of the given
	 * <code>key</code> when only looking at the part of <code>key</code> starting
	 * at <code>startIndex</code>.
	 */
	public static boolean isPrefix(byte[] prefix, byte[] key, int startIndex) {

		if (key.length - startIndex < prefix.length) {
			return false;
		}
		for (int i = 0; i < prefix.length; ++i) {
			if (prefix[i] != key[i + startIndex]) {
				return false;
			}
		}
		return true;
	}

	/** Returns true if a1 is (lexicographically) less than a2. */
	public static boolean isLess(byte[] a1, byte[] a2, boolean resultIfEqual) {
		int limit = Math.min(a1.length, a2.length);
		for (int i = 0; i < limit; ++i) {
			if (unsignedByte(a1[i]) < unsignedByte(a2[i])) {
				return true;
			}
			if (unsignedByte(a1[i]) > unsignedByte(a2[i])) {
				return false;
			}
		}

		if (a1.length < a2.length) {
			return true;
		}
		if (a1.length > a2.length) {
			return false;
		}

		return resultIfEqual;
	}

	/** Returns the unsigned byte interpretation of the parameter. */
	public static int unsignedByte(byte b) {
		return b & 0xff;
	}

	/** Returns the unsigned byte interpretation of the parameter as long. */
	public static long unsignedByteAsLong(byte b) {
		return b & 0xffL;
	}

	/** Returns the concatenation of the given arrays. */
	public static byte[] concat(byte[]... arrays) {
		return concat(Arrays.asList(arrays));
	}

	/** Returns the concatenation of the given arrays. */
	public static byte[] concat(Iterable<byte[]> arrays) {
		int length = 0;
		for (byte[] array : arrays) {
			length += array.length;
		}

		byte[] result = new byte[length];
		int start = 0;
		for (byte[] array : arrays) {
			System.arraycopy(array, 0, result, start, array.length);
			start += array.length;
		}
		return result;
	}

	/**
	 * Creates a hex dump of the provided bytes. This is similar to output from
	 * hexdump tools and primarily used for debugging. The output string will
	 * contain in each line 16 bytes of data first printed as hex numbers and then
	 * as a string interpretation. Each line is also prefixed with an offset.
	 */
	public static String hexDump(byte[] data) {
		return hexDump(data, 16);
	}

	/**
	 * Creates a hex dump of the provided bytes. This is similar to output from
	 * hexdump tools and primarily used for debugging. The output string will
	 * contain in each line <code>width</code> bytes of data first printed as hex
	 * numbers and then as a string interpretation. Each line is also prefixed with
	 * an offset.
	 */
	public static String hexDump(byte[] data, int width) {
		CCSMAssert.isTrue(width >= 1, "Width must be positive!");

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < data.length; i += width) {
			hexDumpAppendLine(data, i, Math.min(data.length, i + width), width, builder);
		}
		return builder.toString();
	}

	/**
	 * Appends a single line to the hex dump for {@link #hexDump(byte[], int)}. The
	 * start is inclusive, the end is exclusive.
	 */
	private static void hexDumpAppendLine(byte[] data, int startOffset, int endOffset, int width,
			StringBuilder builder) {
		builder.append(String.format("%06d: ", startOffset));
		for (int i = startOffset; i < endOffset; ++i) {
			builder.append(String.format("%02x ", data[i]));
		}

		if (endOffset - startOffset < width) {
			builder.append(StringUtils.fillString((width - (endOffset - startOffset)) * 3, StringUtils.SPACE_CHAR));
		}

		builder.append(StringUtils.SPACE_CHAR);
		for (int i = startOffset; i < endOffset; ++i) {
			boolean isInPrintableAsciiRange = (33 <= data[i] && data[i] <= 126);
			if (isInPrintableAsciiRange) {
				builder.append((char) data[i]);
			} else {
				builder.append('.');
			}
		}

		builder.append(StringUtils.LINE_SEPARATOR);
	}

	/**
	 * Returns whether the given bytes start with the
	 * <a href="http://en.wikipedia.org/wiki/Zip_%28file_format%29#File_headers"
	 * >magic bytes</a> that mark a ZIP file.
	 */
	public static boolean startsWithZipMagicBytes(byte[] data) {
		return isPrefix(new byte[] { 0x50, 0x4b, 0x03, 0x04 }, data);
	}

	/**
	 * Returns the first index in <code>searchIn</code> at or after the start index
	 * containing <code>searchFor</code> (or -1 if not found).
	 */
	public static int indexOf(byte[] searchFor, byte[] searchIn, int startIndex) {
		return indexOf(searchFor, searchIn, startIndex, searchIn.length);
	}

	/**
	 * Returns the first index in <code>searchIn</code> at or after the start index
	 * containing <code>searchFor</code> (or -1 if not found). endIndex is the index
	 * of the first byte that is not considered in the match (exclusive).
	 */
	public static int indexOf(byte[] searchFor, byte[] searchIn, int startIndex, int endIndex) {
		if (startIndex + searchFor.length >= endIndex) {
			return -1;
		}

		for (int i = startIndex; i <= endIndex - searchFor.length; ++i) {
			if (isPrefix(searchFor, searchIn, i)) {
				return i;
			}
		}

		return -1;
	}
}