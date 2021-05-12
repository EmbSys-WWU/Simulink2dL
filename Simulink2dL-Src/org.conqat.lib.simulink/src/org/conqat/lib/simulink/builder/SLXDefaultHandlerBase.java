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
package org.conqat.lib.simulink.builder;

import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_BLOCK_TYPE;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_CLASS_NAME;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_DIMENSION;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_NAME;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_OBJECT_ID;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_PROP_NAME;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_REF;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_SID;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_SSID;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_TYPE;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_ID;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_NAME_STATEFLOW;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_ANNOTATION;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_ARRAY;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_BLOCK;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_OBJECT;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_PARAMETER;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_CHART;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_DATA;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_EVENT;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_INSTANCE;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_JUNCTION;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_MACHINE;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_STATE;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_TARGET;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_TRANSITION;

import java.util.ArrayDeque;
import java.util.Deque;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Default handler for XML files from slx simulink files.
 */
public abstract class SLXDefaultHandlerBase extends DefaultHandler {

	/** Parsing stack. The top-most element is the currently parsed element. */
	private final Deque<MutableMDLSection> stack = new ArrayDeque<MutableMDLSection>();

	/** Locator to retrieve line number while parsing. */
	private Locator locator;

	/** Root model or state flow machine section. */
	protected MutableMDLSection rootSection = null;

	/**
	 * Identifier of the root section (e.g. SimulinkConstants.ModelInformation).
	 */
	protected String rootSectionName;

	/** {@inheritDoc} */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (rootSectionName.equals(localName)) {
			MutableMDLSection modelInformation = new MutableMDLSection(rootSectionName, locator.getLineNumber());
			stack.push(modelInformation);
		} else if (stack.isEmpty()) {
			throw new SAXException("Slx file does not start with " + rootSectionName);
		} else {
			startInnerElement(localName, attributes);
		}
	}

	/**
	 * Copies all of the given parameter from the attributes to the section if
	 * they exist (not null).
	 */
	private static void copyNonNullParameters(Attributes attributes, MutableMDLSection subSection,
			String... parameterNames) {
		for (String parameterName : parameterNames) {
			String value = attributes.getValue(parameterName);
			if (value != null) {
				subSection.setParameter(parameterName, value);
			}
		}
	}

	/**
	 * Starts an inner element, i.e. not the top-level model information
	 * element.
	 */
	protected void startInnerElement(String localName, Attributes attributes) {
		switch (localName) {
		case SECTION_PARAMETER:
			startParameterElement(attributes);
			break;
		case SECTION_BLOCK:
			createSubSectionWithAttributes(localName, attributes, PARAM_NAME, PARAM_BLOCK_TYPE, PARAM_SID);
			break;
		case SECTION_OBJECT:
			createSubSectionWithAttributes(localName, attributes, PARAM_PROP_NAME, PARAM_OBJECT_ID, PARAM_CLASS_NAME);
			break;
		case SECTION_ARRAY:
			createSubSectionWithAttributes(localName, attributes, PARAM_PROP_NAME, PARAM_TYPE, PARAM_DIMENSION);
			break;
		case SECTION_STATE:
		case SECTION_TRANSITION:
		case SECTION_JUNCTION:
		case SECTION_EVENT:
		case SECTION_TARGET:
		case SECTION_DATA:
		case SECTION_MACHINE:
		case SECTION_CHART:
		case SECTION_INSTANCE:
			createSubSectionWithAttributes(localName, attributes, PARAM_ID, PARAM_SSID, PARAM_NAME_STATEFLOW);
			break;
		case SECTION_ANNOTATION:
			createSubSectionWithAttributes(localName, attributes, PARAM_SID);
			break;
		default:
			createSubSectionWithAttributes(localName, attributes);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void endElement(String uri, String localName, String qName) {
		if (rootSectionName.equals(localName)) {
			rootSection = stack.pop();
		} else if (SECTION_PARAMETER.equals(localName) && !stack.isEmpty()) {
			MutableMDLSection section = stack.peek();
			String currentParameter = section.getCurrentParameter();
			section.appendParameter(currentParameter, "");
			section.resetCurrentParameter();
		} else if (stack.size() > 1) {
			MutableMDLSection section = stack.pop();
			section.resetCurrentParameter();
		}
	}

	/** Starts an XML element of type "parameter". */
	private void startParameterElement(Attributes attributes) {
		MutableMDLSection currentSection = stack.peek();
		currentSection.setCurrentParameter(attributes.getValue(PARAM_NAME));

		/* Some rare parameters use a ref attribute and have no text. */
		String ref = attributes.getValue(PARAM_REF);
		if (ref != null) {
			currentSection.setParameter(attributes.getValue(PARAM_NAME), ref);
		}
	}

	/**
	 * Creates a new subsection based on the given local name and pushes it to
	 * the {@link #stack}. All parameters given are copied from the attributes
	 * to the section (if they are not null).
	 */
	private void createSubSectionWithAttributes(String localName, Attributes attributes, String... parameterNames) {
		MutableMDLSection subSection = new MutableMDLSection(localName, locator.getLineNumber());
		copyNonNullParameters(attributes, subSection, parameterNames);
		pushSectionToStack(subSection);
	}

	/**
	 * Push a sub section to the stack. May only be invoked on a non-empty
	 * stack.
	 */
	private void pushSectionToStack(MutableMDLSection subSection) {
		MutableMDLSection parent = stack.peek();
		parent.addSubSection(subSection);
		stack.push(subSection);
	}

	/** {@inheritDoc} */
	@Override
	public void characters(char[] chars, int start, int length) {
		if (!stack.isEmpty()) {
			MutableMDLSection container = stack.peek();
			String currentParameter = container.getCurrentParameter();
			if (currentParameter != null) {
				container.appendParameter(currentParameter, new String(chars, start, length));
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

}