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
package org.conqat.lib.simulink.model.datahandler;

import org.conqat.lib.commons.error.FormatException;
import org.conqat.lib.commons.version.Version;
import org.conqat.lib.simulink.builder.ModelBuildingParameters;
import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;
import org.conqat.lib.simulink.model.datahandler.simulink.SimulinkLayoutHandler;
import org.conqat.lib.simulink.model.datahandler.simulink.SimulinkLayoutHandler2008b;
import org.conqat.lib.simulink.model.datahandler.simulink.SimulinkLayoutHandler2009a;
import org.conqat.lib.simulink.model.datahandler.stateflow.StateflowLayoutHandler;

/**
 * Factory for creating the {@link ModelDataHandler} for a given model. This
 * uses the version information contained in the model.
 */
public class ModelDataHandlerFactory {

	/**
	 * Returns a {@link ModelDataHandler} based on the model version and
	 * parameters provided. This is expected to be only called from the builder
	 * package.
	 * 
	 * @param versionString
	 *            the version expected in MAJOR.MINOR format. An exception is
	 *            thrown if this is null or does not follow this format.
	 */
	public static ModelDataHandler createModelHandler(String versionString, boolean isSlxFormat,
			ModelBuildingParameters parameters) throws SimulinkModelBuildingException {
		if (versionString == null) {
			throw new SimulinkModelBuildingException("Model version parameter missing in model!");
		}

		try {
			Version version = Version.parseVersion(versionString);
			return createModelHandler(version, isSlxFormat, parameters);
		} catch (FormatException e) {
			throw new SimulinkModelBuildingException("Could not parse simulink model version: " + versionString, e);
		}
	}

	/**
	 * Returns a {@link ModelDataHandler} based on the model version and
	 * parameters provided.
	 */
	private static ModelDataHandler createModelHandler(Version version, boolean isSlxFormat,
			ModelBuildingParameters parameters) {
		return new ModelDataHandler(parameters, createSimulinkLayoutHandler(version, isSlxFormat, parameters),
				new StateflowLayoutHandler(parameters));
	}

	/**
	 * Returns a {@link ModelDataHandler} based on the model version and
	 * parameters provided.
	 */
	private static SimulinkLayoutHandler createSimulinkLayoutHandler(Version version, boolean isSlxFormat,
			ModelBuildingParameters parameters) {
		if (!isSlxFormat && isVersionAtOrBefore2008b(version)) {
			return new SimulinkLayoutHandler2008b(parameters);
		}
		return new SimulinkLayoutHandler2009a(parameters);
	}

	/**
	 * Returns if the version is at or before version 2008b, which internally is
	 * 7.2.
	 */
	private static boolean isVersionAtOrBefore2008b(Version version) {
		return version.getMajor() < 7 || (version.getMajor() == 7 && version.getMinor() <= 2);
	}
}
