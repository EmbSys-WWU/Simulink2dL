/*-----------------------------------------------------------------------+
 | com.teamscale.index
 |                                                                       |
   $Id: codetemplates.xml 18709 2009-03-06 13:31:16Z hummelb $            
 |                                                                       |
 | Copyright (c)  2009-2016 CQSE GmbH                                 |
 +-----------------------------------------------------------------------*/
package org.conqat.lib.simulink.builder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.commons.xml.XMLUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The Simulink registry is used to parse the XML files loaded at startup and
 * store them in the format of lookup maps. The Simulink XML files determine
 * which text labels and icons are rendered on each block depending on its type.
 */
public class SimulinkRegistry {

	/** Singleton instance. */
	private static final SimulinkRegistry INSTANCE = new SimulinkRegistry();

	/** The default Simulink blocks label lookup file */
	public static final String DEFAULT_SIMULINK_BLOCK_LOOKUP_FILE = "simulink-blocks/default_blockLabelLookupMap.xml";

	/** The Simulink blocks label lookup file provided by the user */
	public static final String USER_SIMULINK_BLOCK_LOOKUP_FILE = "simulink-blocks/user_blockLabelLookupMap.xml";

	/** The XML block tag <block> */
	private static final String BLOCK_TAG = "block";

	/** The XML type tag <type> */
	private static final String TYPE_TAG = "type";

	/** The XML parameter tag <parameter> */
	private static final String PARAMETER_TAG = "parameter";

	/** The XML parameter tag <text> */
	private static final String TEXT_TAG = "text";

	/** The XML icon tag <icon> */
	private static final String ICON_TAG = "icon";

	/** Mapping block types to text labels. */
	private final Map<String, String> labelTextLookup = new HashMap<>();

	/**
	 * Mapping block types to the parameters displayed as text on block label.
	 */
	private final Map<String, String> labelParameterLookup = new HashMap<>();

	/** Mapping block types to the icons displayed on block label. */
	private final Map<String, String> labelIconLookup = new HashMap<>();

	/** Hidden constructor. */
	private SimulinkRegistry() {
		// hidden
	}

	/** Returns the singleton instance. */
	public static SimulinkRegistry getInstance() {
		return INSTANCE;
	}

	/** Parses the XML file and populates the Maps */
	public void loadSimulinkFile(File textLookupFile) {
		NodeList lookupNodes;
		try {
			lookupNodes = XMLUtils.parse(textLookupFile).getElementsByTagName(BLOCK_TAG);

			for (Element blockElement : XMLUtils.elementNodes(lookupNodes)) {
				String blockType = XMLUtils.getNamedChildContent(blockElement, TYPE_TAG);

				populateLookupMap(blockType, blockElement, PARAMETER_TAG, labelParameterLookup);
				populateLookupMap(blockType, blockElement, TEXT_TAG, labelTextLookup);
				populateLookupMap(blockType, blockElement, ICON_TAG, labelIconLookup);
			}
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Extracts the value of the given XML tag name (if it exists) and adds the
	 * value to the lookup map using the block type as key.
	 * 
	 * @param blockType
	 *            the type of the block
	 * @param blockElement
	 *            the block element node
	 * @param xmlTagName
	 *            the tag name
	 * @param lookupMap
	 *            the map to save the retrieved values
	 */
	private static void populateLookupMap(String blockType, Element blockElement, String xmlTagName,
			Map<String, String> lookupMap) {
		String lookupValue = XMLUtils.getNamedChildContent(blockElement, xmlTagName);
		if (!StringUtils.isEmpty(lookupValue)) {
			lookupMap.put(blockType, lookupValue);
		}
	}

	/**
	 * Returns the text to be displayed on a Simulink block label corresponding
	 * to the given block type. If none is available, null is returned.
	 */
	public String getLabelText(String type) {
		return this.labelTextLookup.get(type);
	}

	/**
	 * Returns the parameter to be displayed on a Simulink block label
	 * corresponding to the given block type. If none is available, null is
	 * returned.
	 */
	public String getLabelParameter(String typeString) {
		return this.labelParameterLookup.get(typeString);
	}

	/**
	 * Returns the icon to be displayed on a Simulink block label corresponding
	 * to the given block type. If none is available, null is returned.
	 */
	public String getLabelIcon(String typeString) {
		return this.labelIconLookup.get(typeString);
	}

}
