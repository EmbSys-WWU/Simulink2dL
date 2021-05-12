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
package org.conqat.lib.simulink.model.stateflow;

import org.conqat.lib.commons.collections.UnmodifiableCollection;

/**
 * Interface for classes that may contain {@link StateflowChart}s.
 */
public interface IStateflowChartContainer<P extends IStateflowElement<?>> extends IStateflowElement<P> {

	/** Get charts of this container. */
	public UnmodifiableCollection<StateflowChart> getCharts();

	/** Returns access to the {@link StateflowMachine} of this model. */
	public StateflowMachine getMachine();

}
