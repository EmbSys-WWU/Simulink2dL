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
package org.conqat.lib.simulink.model.datahandler.simulink;

import org.conqat.lib.simulink.builder.ModelBuildingParameters;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkElementBase;
import org.conqat.lib.simulink.model.datahandler.EOrientation;
import org.conqat.lib.simulink.model.datahandler.ModelDataHandler;

/**
 * {@link ModelDataHandler} implementation for the 2009a version of Simulink.
 */
public class SimulinkLayoutHandler2009a extends SimulinkLayoutHandler {

	/** Constructor. */
	public SimulinkLayoutHandler2009a(ModelBuildingParameters parameters) {
		super(parameters);
	}

	/** {@inheritDoc} */
	@Override
	protected EOrientation extractOrientation(SimulinkElementBase element) {
		String rotationValue = element.getParameter(SimulinkConstants.PARAM_BLOCK_ROTATION);

		if (rotationValue == null) {
			return EOrientation.RIGHT;
		}

		EOrientation orientation = EOrientation.fromRotationValue(rotationValue, logger);
		String mirrorValue = element.getParameter(SimulinkConstants.PARAM_BLOCK_MIRROR);
		if (SimulinkConstants.VALUE_ON.equals(mirrorValue)) {
			orientation = orientation.getOpposite();
		}
		return orientation;
	}
}
