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
package org.conqat.lib.simulink.builder;

import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_BLOCK_TYPE;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_NAME;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_SID;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_ID;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_BLOCK;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_CHILDREN;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_MODEL;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_MODEL_INFORMATION;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_STATEFLOW;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_SYSTEM;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_CHART;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_JUNCTION;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_MACHINE;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_STATE;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_TRANSITION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.conqat.lib.simulink.testutils.SimulinkTestBase;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Tests for {@link SLXModelHandler}. This tests if the model is <em>parsed</em>
 * properly.
 */
public class SLXHandlerTest extends SimulinkTestBase {

	/** Test if block name is set properly. */
	@Test
	public void testSlxBlockName() throws Exception {

		Attributes emptyAttributes = mock(Attributes.class);

		SLXModelHandler handler = createModelInformationHead();
		handler.startElement(null, SECTION_MODEL, null, emptyAttributes);

		Attributes blockAttributes = mock(Attributes.class);

		String blockType = "Sum";
		String blockName = "Sum1";
		String blockSID = "12";

		when(blockAttributes.getValue(PARAM_BLOCK_TYPE)).thenReturn(blockType);
		when(blockAttributes.getValue(PARAM_NAME)).thenReturn(blockName);
		when(blockAttributes.getValue(PARAM_SID)).thenReturn(blockSID);

		handler.startElement(null, SECTION_SYSTEM, null, emptyAttributes);
		handler.startElement(null, SECTION_BLOCK, null, blockAttributes);

		handler.endElement(null, SECTION_BLOCK, null);
		handler.endElement(null, SECTION_SYSTEM, null);
		handler.endElement(null, SECTION_MODEL, null);
		createModelInformationTail(handler);

		MutableMDLSection rootSection = handler.getRootModelSection();

		verify(blockAttributes).getValue(PARAM_BLOCK_TYPE);
		verify(blockAttributes).getValue(PARAM_NAME);
		verify(blockAttributes).getValue(PARAM_SID);

		MutableMDLSection model = rootSection.getFirstSubSection(SECTION_MODEL);
		MutableMDLSection system = model.getFirstSubSection(SECTION_SYSTEM);
		MutableMDLSection block = system.getFirstSubSection(SECTION_BLOCK);

		String resolvedBlockType = block.getParameter(PARAM_BLOCK_TYPE);
		String resolvedBlockName = block.getParameter(PARAM_NAME);
		String resolvedBlockSID = block.getParameter(PARAM_SID);

		assertThat(resolvedBlockType, equalTo(blockType));
		assertThat(resolvedBlockName, equalTo(blockName));
		assertThat(resolvedBlockSID, equalTo(blockSID));

	}

	/** Test stateflow model creation. */
	@Test
	public void testStateflowModelCreation() throws Exception {
		Attributes emptyAttributes = mock(Attributes.class);

		SLXModelHandler handler = createModelInformationHead();
		handler.startElement(null, SECTION_STATEFLOW, null, emptyAttributes);

		Attributes idAttributes = mock(Attributes.class);
		when(idAttributes.getValue(PARAM_ID)).thenReturn("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

		driveSaxHandler(emptyAttributes, handler, idAttributes);

		MutableMDLSection rootSection = handler.getRootModelSection();

		verify(idAttributes, times(8)).getValue(PARAM_ID);

		MutableMDLSection stateflow = rootSection.getFirstSubSection(SECTION_STATEFLOW);

		MutableMDLSection machine = stateflow.getFirstSubSection(SECTION_MACHINE);
		assertThat(machine, notNullValue());
		assertThat(machine.getSubSections().getValues(), not(empty()));

		MutableMDLSection machineChildren = machine.getFirstSubSection(SECTION_CHILDREN);
		assertThat(machineChildren, notNullValue());
		assertThat(machineChildren.getSubSections().getValues(), not(empty()));

		MutableMDLSection chart = machineChildren.getFirstSubSection(SECTION_CHART);
		assertThat(chart, notNullValue());
		assertThat(chart.getSubSections().getValues(), not(empty()));

		MutableMDLSection chartChildren = chart.getFirstSubSection(SECTION_CHILDREN);
		assertThat(chartChildren, notNullValue());
		assertThat(chartChildren.getSubSections().getValues(), not(empty()));

		assertThat(chartChildren.getSubSections(SECTION_STATE), hasSize(2));
		assertThat(chartChildren.getSubSections(SECTION_TRANSITION), hasSize(3));
		assertThat(chartChildren.getSubSections(SECTION_JUNCTION), hasSize(1));
	}

	/** Feeds the SAX handler with data for the test. */
	private static void driveSaxHandler(Attributes emptyAttributes, SLXModelHandler handler, Attributes idAttributes)
			throws SAXException {
		handler.startElement(null, SECTION_MACHINE, null, idAttributes);
		handler.startElement(null, SECTION_CHILDREN, null, emptyAttributes);

		handler.startElement(null, SECTION_CHART, null, idAttributes);
		handler.startElement(null, SECTION_CHILDREN, null, emptyAttributes);

		handler.startElement(null, SECTION_STATE, null, idAttributes);
		handler.endElement(null, SECTION_STATE, null);

		handler.startElement(null, SECTION_STATE, null, idAttributes);
		handler.endElement(null, SECTION_STATE, null);

		handler.startElement(null, SECTION_TRANSITION, null, idAttributes);
		handler.endElement(null, SECTION_TRANSITION, null);
		handler.startElement(null, SECTION_TRANSITION, null, idAttributes);
		handler.endElement(null, SECTION_TRANSITION, null);
		handler.startElement(null, SECTION_TRANSITION, null, idAttributes);
		handler.endElement(null, SECTION_TRANSITION, null);
		handler.startElement(null, SECTION_JUNCTION, null, idAttributes);
		handler.endElement(null, SECTION_JUNCTION, null);

		handler.endElement(null, SECTION_CHILDREN, null);
		handler.endElement(null, SECTION_CHART, null);

		handler.endElement(null, SECTION_CHILDREN, null);
		handler.endElement(null, SECTION_MACHINE, null);
		handler.endElement(null, SECTION_STATEFLOW, null);

		createModelInformationTail(handler);
	}

	/** Setup basic slx tags for an empty model. */
	private static SLXModelHandler createModelInformationHead() throws SAXException {
		SLXModelHandler handler = new SLXModelHandler();
		Locator locator = mock(Locator.class);
		when(locator.getLineNumber()).thenReturn(0);
		handler.setDocumentLocator(locator);

		Attributes emptyAttributes = mock(Attributes.class);

		handler.startElement(null, SECTION_MODEL_INFORMATION, null, emptyAttributes);
		return handler;
	}

	/** Finalize model information tag. */
	private static SLXModelHandler createModelInformationTail(SLXModelHandler handler) {
		handler.endElement(null, SECTION_MODEL_INFORMATION, null);
		return handler;
	}

}