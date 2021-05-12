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
package org.conqat.lib.commons.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.filesystem.FileSystemUtils;
import org.conqat.lib.commons.string.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Collection of utility methods for XML.
 */
public class XMLUtils {

	/** Identifier for schema source. */
	private static final String ATTRIBUTE_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

	/** Schema URL */
	private static final String SCHEMA_URL = "http://www.w3.org/2001/XMLSchema";

	/** Identifier for schema language. */
	private static final String ATTRIBUTE_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

	/** Creates a new {@link XMLWriter} that writes to the given output file. */
	public static <ElementsEnum extends Enum<ElementsEnum>, AttributesEnum extends Enum<AttributesEnum>> XMLWriter<ElementsEnum, AttributesEnum> createUtf8Writer(
			File outputFile, Class<AttributesEnum> attributesClass)
			throws FileNotFoundException, UnsupportedEncodingException {
		return new XMLWriter<ElementsEnum, AttributesEnum>(new PrintStream(outputFile, FileSystemUtils.UTF8_ENCODING),
				new XMLResolver<ElementsEnum, AttributesEnum>(attributesClass));
	}

	/**
	 * Parse a file without validation.
	 *
	 * @param file
	 *            the file to parse.
	 * @return the DOM document.
	 *
	 * @throws SAXException
	 *             if a parsing exception occurs, i.e. if the file is not
	 *             well-formed.
	 * @throws IOException
	 *             if an IO exception occurs.
	 */
	public static Document parse(File file) throws SAXException, IOException {
		return createSchemaUnawareParser().parse(file);
	}

	/**
	 * Parse an input source without validation.
	 *
	 * @param input
	 *            the input source to parse
	 * @return the DOM document.
	 *
	 * @throws SAXException
	 *             if a parsing exception occurs, i.e. if the file is not
	 *             well-formed.
	 * @throws IOException
	 *             if an IO exception occurs.
	 */
	public static Document parse(InputSource input) throws SAXException, IOException {

		return createSchemaUnawareParser().parse(input);
	}

	/**
	 * Parse an input source using SAX without validation.
	 *
	 * @throws SAXException
	 *             if a parsing exception occurs, i.e. if the file is not
	 *             well-formed.
	 * @throws IOException
	 *             if an IO exception occurs.
	 */
	public static void parseSAX(File file, DefaultHandler handler) throws SAXException, IOException {
		createSchemaUnawareSAXParser().parse(file, handler);
	}

	/**
	 * Parse an input source using SAX without validation.
	 *
	 * @throws SAXException
	 *             if a parsing exception occurs, i.e. if the file is not
	 *             well-formed.
	 * @throws IOException
	 *             if an IO exception occurs.
	 */
	public static void parseSAX(InputSource input, DefaultHandler handler) throws SAXException, IOException {
		createSchemaUnawareSAXParser().parse(input, handler);
	}

	/**
	 * Parse a string that contains XML without validation.
	 *
	 * @throws SAXException
	 *             if a parsing exception occurs, i.e. if the file is not
	 *             well-formed.
	 * @throws IOException
	 *             if an IO exception occurs.
	 */
	public static void parseSAX(String content, DefaultHandler handler) throws SAXException, IOException {
		parseSAX(new InputSource(new ByteArrayInputStream(StringUtils.stringToBytes(content))), handler);
	}

	/**
	 * Parse and validate file using schema. This implements a custom error
	 * handler to avoid different behaviour between the JAXP implementations
	 * shipping with Java 1.5 and Java 1.6.
	 *
	 * @param file
	 *            the file to parse.
	 * @param schemaURL
	 *            URL point to schema, may not be null
	 * @return the DOM document.
	 *
	 * @throws SAXException
	 *             if a parsing exception occurs, i.e. if the file is not
	 *             well-formed or not valid
	 * @throws IOException
	 *             if an IO exception occurs.
	 */
	public static Document parse(File file, URL schemaURL) throws SAXException, IOException {

		FileInputStream stream = new FileInputStream(file);
		try {
			return parse(new InputSource(stream), schemaURL);
		} finally {
			stream.close();
		}
	}

	/**
	 * Parse and validate file using schema. This implements a custom error
	 * handler to avoid different behaviour between the JAXP implementations
	 * shipping with Java 1.5 and Java 1.6.
	 *
	 * @param input
	 *            the input to parse.
	 * @param schemaURL
	 *            URL point to schema, may not be null
	 * @return the DOM document.
	 *
	 * @throws SAXException
	 *             if a parsing exception occurs, i.e. if the file is not
	 *             well-formed or not valid
	 * @throws IOException
	 *             if an IO exception occurs.
	 */
	public static Document parse(InputSource input, URL schemaURL) throws SAXException, IOException {

		CCSMAssert.isTrue(schemaURL != null, "Schema URL may not be null!");

		DocumentBuilder parser = createSchemaAwareParser(schemaURL);

		XMLErrorHandler errorHandler = new XMLErrorHandler();
		parser.setErrorHandler(errorHandler);
		Document document = parser.parse(input);

		if (errorHandler.exception != null) {
			throw errorHandler.exception;
		}

		return document;
	}

	/**
	 * Parse and validate file using SAX and schema.
	 *
	 * @param file
	 *            the file to parse.
	 * @param schemaURL
	 *            URL point to schema, may not be null
	 *
	 * @throws SAXException
	 *             if a parsing exception occurs, i.e. if the file is not
	 *             well-formed or not valid
	 * @throws IOException
	 *             if an IO exception occurs.
	 */
	public static void parseSAX(File file, URL schemaURL, DefaultHandler handler) throws SAXException, IOException {

		FileInputStream stream = new FileInputStream(file);
		try {
			parseSAX(new InputSource(stream), schemaURL, handler);
		} finally {
			stream.close();
		}
	}

	/**
	 * Parse and validate file using SAX and schema.
	 *
	 * @param input
	 *            the input to parse.
	 * @param schemaURL
	 *            URL point to schema, may not be null
	 *
	 * @throws SAXException
	 *             if a parsing exception occurs, i.e. if the file is not
	 *             well-formed or not valid
	 * @throws IOException
	 *             if an IO exception occurs.
	 */
	public static void parseSAX(InputSource input, URL schemaURL, DefaultHandler handler)
			throws SAXException, IOException {

		CCSMAssert.isTrue(schemaURL != null, "Schema URL may not be null!");
		createSchemaAwareSAXParser(schemaURL).parse(input, handler);
	}

	/**
	 * Creates a StAX parser. The parser can be queried for new parsing events
	 * via hasNext()/next(). It continues parsing only on next() calls.
	 * 
	 * This parser should be used if performance is relevant and only part of
	 * the XML document is needed.
	 */
	public static XMLEventReader createStAXParser(InputSource input) throws XMLStreamException {
		XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
		XMLEventReader reader = xmlInputFactory.createXMLEventReader(input.getByteStream(), input.getEncoding());
		return reader;
	}

	/** Creates a schema-unaware XML parser */
	private static DocumentBuilder createSchemaUnawareParser() {

		try {
			return createNamespaceAwareDocumentBuilderFactory().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException("No document builder found, probably Java is misconfigured!", e);
		}
	}

	/** Creates a schema-unaware SAX parser */
	private static SAXParser createSchemaUnawareSAXParser() throws SAXException {
		try {
			return createNamespaceAwareSAXParserFactory().newSAXParser();
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException("No SAX parser found, probably Java is misconfigured!", e);
		}
	}

	/** Creates a schema-aware XML parser */
	private static DocumentBuilder createSchemaAwareParser(URL schemaURL) {
		DocumentBuilderFactory dbf = createNamespaceAwareDocumentBuilderFactory();
		dbf.setValidating(true);
		dbf.setAttribute(ATTRIBUTE_SCHEMA_LANGUAGE, SCHEMA_URL);
		dbf.setAttribute(ATTRIBUTE_SCHEMA_SOURCE, schemaURL.toString());

		try {
			return dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException("No document builder found, probably Java is misconfigured!", e);
		}
	}

	/** Creates a schema-aware SAX parser */
	private static SAXParser createSchemaAwareSAXParser(URL schemaURL) throws SAXException {
		SAXParserFactory spf = createNamespaceAwareSAXParserFactory();
		spf.setValidating(true);
		try {
			SAXParser parser = spf.newSAXParser();
			parser.setProperty(ATTRIBUTE_SCHEMA_LANGUAGE, SCHEMA_URL);
			parser.setProperty(ATTRIBUTE_SCHEMA_SOURCE, schemaURL.toString());
			return parser;
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException("No SAX parser found, probably Java is misconfigured!", e);
		}
	}

	/** Creates a namespace-aware {@link DocumentBuilderFactory} */
	private static DocumentBuilderFactory createNamespaceAwareDocumentBuilderFactory() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);

		return dbf;
	}

	/** Creates a namespace-aware {@link SAXParserFactory} */
	private static SAXParserFactory createNamespaceAwareSAXParserFactory() {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		return spf;
	}


	/**
	 * Same as {@link #parse(File, URL)} but with schema file.
	 *
	 * @throws IllegalArgumentException
	 *             if the schema file could not be converted to an URL
	 */
	public static Document parse(File file, File schema) throws SAXException, IOException {
		try {
			return parse(file, schema.toURI().toURL());
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Schema file could not be converted to URL: " + e);
		}
	}

	/**
	 * Returns a string representation of the given XML document, which is
	 * "pretty printed", i.e. the tags are indented.
	 */
	public static String prettyPrint(Document doc) throws TransformerException {
		URL url = XMLUtils.class.getResource("pretty.xsl");
		StreamSource xslSource = new StreamSource(url.toExternalForm());
		Transformer transformer = TransformerFactory.newInstance().newTransformer(xslSource);
		return StringUtils.normalizeLineSeparatorsPlatformSpecific(transformDocumentToString(doc, transformer));
	}

	/**
	 * Transforms the document to an xml string (flat, with no line breaks). Use
	 * {@link #prettyPrint(Document)} for readable xml output.
	 */
	public static String print(Document document) throws TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		return transformDocumentToString(document, transformer);
	}

	/** Transform an XML document to a string using the given transformer */
	private static String transformDocumentToString(Document document, Transformer transformer)
			throws TransformerException {
		DOMSource source = new DOMSource(document);
		StringWriter stringWriter = new StringWriter();
		StreamResult resultStream = new StreamResult(stringWriter);
		transformer.transform(source, resultStream);
		return stringWriter.toString();
	}

	/**
	 * Determines the index (starting at 0) of the given element relative to
	 * other element nodes for the same parent.
	 */
	public static int getElementPosition(Element element) {
		int num = -1;
		Node node = element;
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				++num;
			}
			node = node.getPreviousSibling();
		}
		return num;
	}

	/**
	 * Returns all children of the given element which are element named as
	 * specified.
	 */
	public static List<Element> getNamedChildren(Element element, String elementNames) {
		List<Element> result = new ArrayList<Element>();
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(elementNames)) {
				result.add((Element) node);
			}
		}
		return result;
	}

	/**
	 * Returns the first child of the given element which is an element named as
	 * specified. Returns null if none are found.
	 */
	public static Element getNamedChild(Element element, String name) {
		List<Element> children = XMLUtils.getNamedChildren(element, name);
		if (children.size() > 0) {
			return children.get(0);
		}
		return null;
	}

	/**
	 * Returns the last child of the given element which is an element named as
	 * specified. Returns null if none are found.
	 */
	public static Element getLastNamedChild(Element element, String name) {
		List<Element> children = XMLUtils.getNamedChildren(element, name);
		if (children.size() > 0) {
			return CollectionUtils.getLast(children);
		}
		return null;
	}

	/**
	 * Get the text content of the given element's first child that is an
	 * element named as specified. If none is found, the empty string is
	 * returned.
	 */
	public static String getNamedChildContent(Element parent, String name) {
		Element element = XMLUtils.getNamedChild(parent, name);
		if (element == null) {
			return StringUtils.EMPTY_STRING;
		}
		return element.getTextContent();
	}

	/**
	 * Returns the first element whose child with the given name has the given
	 * text content.
	 */
	public static Element getElementByChildContent(List<Element> elements, String childName, String childContent) {
		for (Element element : elements) {
			Element child = XMLUtils.getNamedChild(element, childName);
			if (child != null && child.getTextContent().equals(childContent)) {
				return element;
			}
		}
		return null;
	}

	/**
	 * Returns the ancestor of the given element with the given distance.
	 * Distance zero means the element itself, one the parent and so on.
	 */
	public static Element getAncestor(Element element, int distance) {
		for (int i = 0; i < distance; i++) {
			if (element == null) {
				return null;
			}
			element = CCSMAssert.checkedCast(element.getParentNode(), Element.class);
		}
		return element;
	}

	/**
	 * Extracts all ElementNodes from a NodeList and returns the result as a
	 * list.
	 *
	 * @param nodeList
	 *            the NodeList to be searched for ElementNodes.
	 * @return an array containing all ElementNodes stored in the given node
	 *         list or null if the input has been null.
	 */
	public static List<Element> elementNodes(NodeList nodeList) {
		if (nodeList == null) {
			return null;
		}
		List<Element> result = new ArrayList<Element>();
		int len = nodeList.getLength();
		for (int i = 0; i < len; ++i) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				result.add((Element) node);
			}
		}
		return result;
	}

	/** Removes the given element from its parent. */
	public static void removeElement(Element element) {
		Node parent = element.getParentNode();
		if (parent != null) {
			parent.removeChild(element);
		}
	}

	/**
	 * Appends a child element with the given tag name to the given element and
	 * returns the new element.
	 */
	public static Element appendChild(Element element, String tagName) {
		Element newElement = CCSMAssert.checkedCast(element.getOwnerDocument().createElement(tagName), Element.class);
		element.appendChild(newElement);
		return newElement;
	}

	/**
	 * Appends a child element with the given tag name and the given text
	 * content to the given element and returns the new element.
	 */
	public static Element appendChild(Element parent, String tagName, String textContent) {
		Element newElement = appendChild(parent, tagName);
		newElement.setTextContent(textContent);
		return newElement;
	}

	/**
	 * Get all leaf elements of an XML tree rooted at an element
	 *
	 * @param root
	 *            The root element
	 * @return List of all leaf elements
	 */
	public static List<Element> leafElementNodes(Element root) {
		List<Element> leafElementNodes = new ArrayList<Element>();
		leafElementNodes(root, leafElementNodes);
		return leafElementNodes;
	}

	/**
	 * Add all leaf element nodes of an XML tree rooted at an element to a list
	 */
	private static void leafElementNodes(Element root, List<Element> leafElementNodes) {
		List<Element> children = XMLUtils.elementNodes(root.getChildNodes());
		if (children.isEmpty()) {
			leafElementNodes.add(root);
		} else {
			for (Element child : children) {
				leafElementNodes(child, leafElementNodes);
			}
		}
	}

	/** Creates an empty XML document. */
	public static Document createEmptyDocument() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException("No document builder found, probably Java is misconfigured!", e);
		}
		return builder.newDocument();
	}

	/** Converts the given {@link String} to a SAX {@link InputSource}. */
	public static InputSource toInputSource(String string) {
		return new InputSource(new StringReader(string));
	}

	/**
	 * Simple error handler for handling validation errors. This handler stores
	 * the first problem raised during parsing.
	 */
	private static class XMLErrorHandler implements ErrorHandler {

		/**
		 * The stored exception. Value unequal <code>null</code> signals a
		 * validation problem.
		 */
		private SAXParseException exception;

		/** {@inheritDoc} */
		@Override
		public void error(SAXParseException exception) {
			if (this.exception == null) {
				this.exception = exception;
			}
		}

		/** {@inheritDoc} */
		@Override
		public void fatalError(SAXParseException exception) {
			error(exception);
		}

		/** {@inheritDoc} */
		@Override
		public void warning(SAXParseException exception) {
			System.out.println(exception);
			// ignore
		}
	}

	/**
	 * Fixes chars which are not allowed in XML content. The following
	 * replacements are allowed:
	 * <ul>
	 * <li>All '&' which are not part of an XML escape char sequence are
	 * replaced by '&amp;'.
	 * <li>All low ASCII control chars are removed, besides TAB, LF, CR
	 * <li>Escaped ASCII control chars are removed (e.g. &#x0; or &#0;) with
	 * variable zero padding in hex and decimal format.
	 * </ul>
	 */
	public static String fixIllegalXmlChars(String content) {
		String replacedContent = content.replaceAll("(?i)&(?!(lt|gt|amp|apos|quot|#x[0-9a-f]+|#\\d+);)", "&amp;");
		replacedContent = replacedContent.replaceAll("([\\x00-\\x08\\x0b\\x0c\\x0e-\\x1f\\x7f])",
				StringUtils.EMPTY_STRING);
		replacedContent = replacedContent.replaceAll("(?i)&#x0*([0-8bcef]|1[0-9a-f]|7f);", StringUtils.EMPTY_STRING);
		replacedContent = replacedContent.replaceAll("(?i)&#0*([0-8]|1[124-9]|2[0-9]|3[01]|127);",
				StringUtils.EMPTY_STRING);
		return replacedContent;
	}
}