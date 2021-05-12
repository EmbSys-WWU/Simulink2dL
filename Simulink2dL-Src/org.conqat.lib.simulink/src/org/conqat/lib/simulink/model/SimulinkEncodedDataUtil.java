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
package org.conqat.lib.simulink.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.conqat.lib.commons.string.StringUtils;

/**
 * Util class to work with the data provided by MatResource and MatData sections
 * (that usually appear in mdl files).
 * 
 * The encoding is similar to UUencode, but not the same: There are no line
 * breaks and no pre or suffixes. However, space characters (' ') need to be
 * replaced by '`'. This class removes escaping from special characters.
 */
public class SimulinkEncodedDataUtil extends SimulinkBlock {

	/** Gets the buffered image from the encoded data of a mdl file. */
	public static BufferedImage getImage(String data) throws IOException {
		if (StringUtils.isEmpty(data)) {
			return null;
		}

		InputStream encodedStream = new ByteArrayInputStream(changeDataToFitUUEncoding(data).getBytes());
		return ImageIO.read(new ByteArrayInputStream(decodeBuffer(encodedStream)));
	}

	/**
	 * Gets the text from a MatData section in the encoded data of a mdl file.
	 */
	public static String getText(String data) {
		if (StringUtils.isEmpty(data)) {
			return null;
		}

		InputStream encodedStream = new ByteArrayInputStream(changeDataToFitUUEncoding(data).getBytes());

		Scanner scanner = new Scanner(new ByteArrayInputStream(decodeBuffer(encodedStream)));

		// the decoded data contains some additional information and many
		// unreadable characters. After two 'B' matches, the interesting
		// information occurs. We only look for word and common whitespace
		// characters.
		scanner.findInLine("B");
		scanner.findInLine("B");

		String text = scanner.findInLine("[\\w\\s]+");
		scanner.close();

		return text;
	}

	/**
	 * Image content is nearly UUEncoded. To fit the encoding, data needs to be
	 * adapted. This method does not append pre- and suffixes as they are not
	 * necessary for our UUDecoder class.
	 * 
	 * @param data
	 *            the data from the mdl file that is nearly UUencoded.
	 */
	private static String changeDataToFitUUEncoding(String data) {
		String transformed = data.replace(" ", "`");

		// replace double backslash: \\
		transformed = transformed.replaceAll(Pattern.quote("\\\\"), Matcher.quoteReplacement("\\"));

		// replace \" by "
		transformed = transformed.replaceAll(Pattern.quote("\\\""), "\"");

		return transformed;
	}

	/** Decode the given buffer. */
	private static byte[] decodeBuffer(InputStream inputStream) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			decodeBuffer(inputStream, bos);
		} catch (IOException e) {
			throw new AssertionError("This can not happen as we work in memory!", e);
		}
		return bos.toByteArray();
	}

	/** Decode input buffer and write the decoded data to the outputStream. */
	private static void decodeBuffer(InputStream inputStream, OutputStream outputStream) throws IOException {

		byte decoderBuffer[] = new byte[4];

		while (true) {
			int decodedBytes[] = new int[3];

			// read 4 bytes
			for (int i = 0; i < 4; i++) {
				int readCharacter = inputStream.read();
				if (readCharacter == -1) { // end of stream
					return;
				}
				decoderBuffer[i] = (byte) ((readCharacter - ' ') & 0x3f);
			}

			// decode the 4 read bytes by writing them to three bytes.
			decodedBytes[0] = ((decoderBuffer[0] << 2) & 0xfc) | ((decoderBuffer[1] >>> 4) & 3);
			decodedBytes[1] = ((decoderBuffer[1] << 4) & 0xf0) | ((decoderBuffer[2] >>> 2) & 0xf);
			decodedBytes[2] = ((decoderBuffer[2] << 6) & 0xc0) | (decoderBuffer[3] & 0x3f);

			// write the decoded bytes to the output stream
			outputStream.write((byte) (decodedBytes[0] & 0xff));
			outputStream.write((byte) (decodedBytes[1] & 0xff));
			outputStream.write((byte) (decodedBytes[2] & 0xff));
		}
	}
}
