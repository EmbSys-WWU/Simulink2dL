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
package org.conqat.lib.commons.serialization.objects;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;

/**
 * This class contains code that was copied and adjusted from
 * {@link DataInputStream} and {@link DataOutputStream}. The reason is that
 * while <a href=
 * "http://docs.oracle.com/javase/6/docs/platform/serialization/spec/protocol.html"
 * >modified UTF8</a> handling is implemented in these classes, they can only
 * handle short strings (less than 2^16 characters). As the code is badly
 * modularized, we had to copy and modify it.
 */
public class LongStringUtils {

	/** Max size for char that can be encoded as single byte. */
	private static final int SINGLE_BYTE_LIMIT = 0x007F;

	/** Max size for char that can be encoded as two bytes. */
	private static final int DOUBLE_BYTE_LIMIT = 0x07FF;

	/** Maximal length of a short string. */
	public static final int MAX_SHORT_STRING_LENGTH = (1 << 16) - 1;

	/**
	 * This is a copy of {@link DataInputStream#readUTF(DataInput)} with the main
	 * difference that the length is not read as a short but as a long.
	 */
	public static String readLongString(DataInputStream in) throws IOException {

		// we need to read a long here as per specification, but are guaranteed to get
		// only int range (as an array in Java also can not hold more elements)
		int utfLength = (int) in.readLong();
		byte[] bytes = new byte[utfLength];
		in.readFully(bytes);

		int count = 0;
		int charCount = 0;
		char[] chars = new char[utfLength];
		while (count < utfLength) {
			int c = bytes[count] & 0xff;
			if (c <= SINGLE_BYTE_LIMIT) {
				count++;
				chars[charCount++] = (char) c;
				continue;
			}

			switch (c >> 4) {
			case 12:
			case 13:
				/* 110x xxxx 10xx xxxx */
				count = incrementChecked(count, utfLength, 2);
				chars[charCount++] = extractTwoByteChar(bytes, count - 2);
				break;
			case 14:
				/* 1110 xxxx 10xx xxxx 10xx xxxx */
				count = incrementChecked(count, utfLength, 3);
				chars[charCount++] = extractThreeByteChar(bytes, count - 3);
				break;
			default:
				throwMalformedIf(true, count);
			}
		}

		return new String(chars, 0, charCount);

	}

	/** Extracts a three byte character at given index position. */
	private static char extractThreeByteChar(byte[] bytes, int index) throws UTFDataFormatException {
		int char2 = bytes[index + 1];
		int char3 = bytes[index + 2];
		throwMalformedIf(((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80), index + 1);
		return (char) (((bytes[index] & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
	}

	/** Extracts a two byte character at given index position. */
	private static char extractTwoByteChar(byte[] bytes, int index) throws UTFDataFormatException {
		int char2 = bytes[index + 1];
		throwMalformedIf((char2 & 0xC0) != 0x80, index + 2);
		return (char) (((bytes[index] & 0x1F) << 6) | (char2 & 0x3F));
	}

	private static void throwMalformedIf(boolean condition, int count) throws UTFDataFormatException {
		if (condition) {
			throw new UTFDataFormatException("malformed input around byte " + count);
		}
	}

	/**
	 * Increments the count and checks whether count is still within the given
	 * length.
	 */
	private static int incrementChecked(int count, int utfLength, int increment) throws UTFDataFormatException {
		count += increment;
		if (count > utfLength) {
			throw new UTFDataFormatException("malformed input: partial character at end");
		}
		return count;
	}

	/**
	 * This is a copy of {@link DataOutputStream#writeUTF(String)} with the main
	 * difference that the length is not read as a short but as a long.
	 */
	public static void writeUTF(String string, DataOutputStream out) throws IOException {
		int utfLength = string.chars().map(LongStringUtils::utfSize).sum();
		out.writeLong(utfLength);

		byte[] bytes = new byte[utfLength];
		int index = 0;
		for (int i = 0; i < string.length(); i++) {
			int c = string.charAt(i);
			if ((c >= 1) && (c <= SINGLE_BYTE_LIMIT)) {
				bytes[index++] = (byte) c;
			} else {
				if (c > DOUBLE_BYTE_LIMIT) {
					bytes[index++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
					bytes[index++] = (byte) (0x80 | ((c >> 6) & 0x3F));
				} else {
					bytes[index++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
				}
				bytes[index++] = (byte) (0x80 | (c & 0x3F));
			}
		}
		out.write(bytes);
	}

	/** Returns UTF encoded size of a character. */
	private static int utfSize(int character) {
		if (character >= 1 && character <= SINGLE_BYTE_LIMIT) {
			return 1;
		}
		if (character > DOUBLE_BYTE_LIMIT) {
			return 3;
		}
		return 2;
	}
}
