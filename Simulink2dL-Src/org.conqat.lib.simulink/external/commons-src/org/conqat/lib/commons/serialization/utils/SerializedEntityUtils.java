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
package org.conqat.lib.commons.serialization.utils;

import java.io.IOException;
import java.util.List;

import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.serialization.SerializedEntityPool;
import org.conqat.lib.commons.serialization.classes.SerializedClass;
import org.conqat.lib.commons.serialization.objects.SerializedObject;

/**
 * Utility methods for dealing with serialized entities.
 */
public class SerializedEntityUtils {

	/** Returns all instances of the given class. */
	public static List<SerializedObject> findInstancesOf(SerializedClass serializedClass,
			SerializedEntityPool entityPool) throws IOException {
		return CollectionUtils.filter(entityPool.getEntities(SerializedObject.class),
				entity -> entity.getClassHandle() == serializedClass.getHandle());
	}
}
