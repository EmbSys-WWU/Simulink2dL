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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectStreamConstants;

import org.conqat.lib.commons.serialization.SerializedEntityParser;
import org.conqat.lib.commons.serialization.SerializedEntityPool;
import org.conqat.lib.commons.serialization.SerializedEntitySerializer;

/**
 * A serialized enum literal.
 */
public class SerializedEnumLiteral extends SerializedObjectBase {

	/** The name of the enum literal. */
	private final String literalName;

	/** Constructor from parser. */
	public SerializedEnumLiteral(SerializedEntityPool pool, SerializedEntityParser parser, int classHandle)
			throws IOException {
		super(pool, classHandle);
		literalName = parser.parseStringObject().getValue();
	}

	/** Constructor with explicit value. */
	public SerializedEnumLiteral(SerializedEntityPool pool, int classHandle, String literalName) {
		super(pool, classHandle);
		this.literalName = literalName;
	}

	/** {@inheritDoc} */
	@Override
	protected byte getObjectTagConstant() {
		return ObjectStreamConstants.TC_ENUM;
	}

	/** {@inheritDoc} */
	@Override
	protected void serializeObjectContent(DataOutputStream dos, SerializedEntitySerializer serializer)
			throws IOException {
		serializer.serializeStringObject(literalName, false);
	}

	/** Returns the name of the literal. */
	public String getLiteralName() {
		return literalName;
	}

	/**
	 * Creates a new literal for the given enum value with the given class and
	 * returns a handle to the new object.
	 */
	public static <E extends Enum<E>> int createLiteral(SerializedEntityPool entityPool, int enumClassHandle, E value) {
		SerializedEnumLiteral enumLiteral = new SerializedEnumLiteral(entityPool, enumClassHandle, value.name());
		return enumLiteral.getHandle();
	}
}
