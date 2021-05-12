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


/*NOTE: File content modified to support Simulink20+ models.
* Changes are marked as "Modified: Simulink20+:"*/
package org.conqat.lib.simulink.builder;

import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_LIBRARY;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_MODEL;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_STATEFLOW;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.conqat.lib.commons.collections.PairList;
import org.conqat.lib.commons.filesystem.FileSystemUtils;
import org.conqat.lib.commons.logging.ILogger;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.commons.xml.XMLUtils;
import org.conqat.lib.simulink.model.ParameterizedElement;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.datahandler.ModelDataHandler;
import org.conqat.lib.simulink.model.datahandler.ModelDataHandlerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Main Simulink/Stateflow model building class.
 * 
 */
public class SimulinkModelBuilder implements Closeable {

	/** Maximal number of lines to check for the encoding information. */
	private static final int ENCODING_GUESS_MAX_LINES = 50;

	/**
	 * The pattern used to extract encoding information for encoding guessing.
	 */
	private static final Pattern ENCODING_GUESS_PATTERN = Pattern.compile("SavedCharacterEncoding\\s+\"(.*?)\"");

	/** Regex for control characters. */
	private static final String CONTROL_CHARACTERS_REGEX = "[\\x00-\\x1F]";

	/** Path to model file within slx zip. */
	private static final String SLX_MODEL_FILE = "simulink/blockdiagram.xml";

	/** Path to defaults file within slx zip. */
	private static final String SLX_DEFAULTS_FILE = "simulink/bddefaults.xml";
	
	/** Modified: Simulink20+: Path to model contents in system_root.xml **/
	private static final String SLX_SYSTEMROOT_FILE = "simulink/systems/system_root.xml";

	/** Path to stateflow file within slx zip. */
	private static final String SLX_STATEFLOW_FILE = "simulink/stateflow.xml";

	/** Path to folder containing the mxArray data tags. */
	private static final String SLX_MXDATA_FOLDER = "simulink/bdmxdata/";

	/** Slx file extension. */
	public static final String SLX_FILE_EXTENSION = ".slx";

	/** Mdl file extension. */
	public static final String MDL_FILE_EXTENSION = ".mdl";

	/** InputStream to read model. */
	private final InputStream modelInputStream;

	/** InputStream to read model defaults. */
	private ZipInputStream modelDefaultsInputStream;

	/** ZipInputStream for stateflow information in slx files. */
	private ZipInputStream stateflowInputStream;
	
	/** Modified: Simulink20+: ZipInputStream for reading model contents in systems/system_root.xml */
	private ZipInputStream systemInputStream;

	/**
	 * MxData for the model as a list of pairs, where the first string is the file
	 * key and the second string is the variant control.
	 */
	private PairList<String, String> modelMxData;

	/**
	 * Old slx files store stateflow machine information in the model. Only if there
	 * is a stateflow xml file, special handling is needed
	 */
	private boolean slxContainsStateflowXml = false;

	/**
	 * In some cases, slx files store data in mxarray files in 'bdmxdata' folder
	 * which are referenced in the model by the blocks. If that's the case, then
	 * special handling is required.
	 */
	private boolean slxContainsMXData = false;

	/**
	 * Flag to indicate whether the model is in the new .slx or the old .mdl file
	 * format.
	 */
	private final boolean isSlxFormat;

	/** Newer slx files have model defaults in a separate file 'bddefaults.xml' */
	private boolean hasDefaultsXmlFile = false;
	
	/** Modified: Simulink20+:  Newer slx files have model contents in a seperate file 'systems/system_root.xml' */
	private boolean hasSystemRootXmlFile = false;

	/** Logger. */
	private final ILogger logger;

	/** Origin id. May be null. */
	private final String originId;

	/** The file that will be parsed. May be <code>null</code>. */
	private File file;

	/** The name of the file that will be parsed. */
	private String filename;

	/**
	 * Create new model builder.
	 * 
	 * @param inputStream
	 *            the stream to build the model from.
	 * @param logger
	 *            logger for reporting anomalies. You may use SimpleLogger here.
	 * @param filename
	 *            the name of the file. This is only used for determining the file
	 *            format to be used.
	 * @param originId
	 *            the origin id for the model. See
	 *            {@link SimulinkModel#getOriginId()}
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public SimulinkModelBuilder(InputStream inputStream, ILogger logger, String filename, String originId)
			throws IOException {
		this(null, inputStream, logger, filename, originId);
	}

	/**
	 * Create model builder.
	 * 
	 * @param file
	 *            file to parse
	 * @param logger
	 *            logger for reporting anomalies. You may use SimpleLogger here.
	 * @param originId
	 *            the origin id for the model. See
	 *            {@link SimulinkModel#getOriginId()}
	 * @throws ZipException
	 *             if a ZIP format error has occurred
	 * @throws IOException
	 *             if an I/O error has occurred
	 */
	public SimulinkModelBuilder(File file, ILogger logger, String originId) throws ZipException, IOException {
		this(file, new FileInputStream(file), logger, file.getName(), originId);
	}

	/**
	 * Create model builder. Origin id of the model is set to file.getName().
	 * 
	 * @param file
	 *            file to parse
	 * @param logger
	 *            logger for reporting anomalies. You may use SimpleLogger here.
	 * @throws ZipException
	 *             if a ZIP format error has occurred
	 * @throws IOException
	 *             if an I/O error has occurred
	 */
	public SimulinkModelBuilder(File file, ILogger logger) throws ZipException, IOException {
		this(file, logger, FileSystemUtils.getFilenameWithoutExtension(file));
	}

	/**
	 * Create new model builder.
	 *
	 * @param file
	 *            the file from which the model will be build (may be
	 *            <code>null</code>).
	 * @param inputStream
	 *            the stream to build the model from.
	 * @param logger
	 *            logger for reporting anomalies. You may use SimpleLogger here.
	 * @param filename
	 *            the name of the file. This is only used for determining the file
	 *            format to be used.
	 * @param originId
	 *            the origin id for the model. See
	 *            {@link SimulinkModel#getOriginId()}
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private SimulinkModelBuilder(File file, InputStream inputStream, ILogger logger, String filename, String originId)
			throws IOException {
		if (filename.toLowerCase().endsWith(SLX_FILE_EXTENSION)) {
			// copy stream
			if (!inputStream.markSupported()) {
				inputStream = new BufferedInputStream(inputStream);
			}
			inputStream.mark(inputStream.available() + 1);

			moveStreamsToSlxFileEntries(inputStream);

			// model stream
			ZipInputStream zipInputStream = new ZipInputStream(inputStream);
			moveStreamToModelFileEntry(zipInputStream);
			this.modelInputStream = zipInputStream;

			this.isSlxFormat = true;
		} else if (filename.toLowerCase().endsWith(MDL_FILE_EXTENSION)) {
			// ensure we have mark support in the stream
			if (inputStream.markSupported()) {
				this.modelInputStream = inputStream;
			} else {
				this.modelInputStream = new BufferedInputStream(inputStream);
			}
			this.isSlxFormat = false;
		} else {
			throw new IOException("Unknown Simulink file extension found for " + filename);
		}
		this.logger = logger;
		this.originId = originId;
		this.file = file;
		this.filename = filename;
	}

	/**
	 * Moves each of input streams to point to its corresponding file entry from the
	 * slx file.
	 */
	private void moveStreamsToSlxFileEntries(InputStream inputStream) throws IOException {
		ByteArrayOutputStream outputStream = copyInputStreamAndReset(inputStream);

		// Stateflow stream
		this.stateflowInputStream = new ZipInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
		this.slxContainsStateflowXml = moveStreamToFileEntry(this.stateflowInputStream, SLX_STATEFLOW_FILE);

		// defaults file stream
		outputStream = copyInputStreamAndReset(inputStream);
		this.modelDefaultsInputStream = new ZipInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
		this.hasDefaultsXmlFile = moveStreamToFileEntry(this.modelDefaultsInputStream, SLX_DEFAULTS_FILE);

		outputStream = copyInputStreamAndReset(inputStream);
		this.systemInputStream = new ZipInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
		this.hasSystemRootXmlFile = moveStreamToFileEntry(this.systemInputStream, SLX_SYSTEMROOT_FILE);
		
		// mxData stream
		ZipInputStream zipInputStream = new ZipInputStream(inputStream);
		extractMxDataForModel(zipInputStream);
		inputStream.reset();
	}

	/**
	 * Copies the given InputStream to a ByteArrayOutputStream (without closing the
	 * stream) and resets the InputStream.
	 */
	private static ByteArrayOutputStream copyInputStreamAndReset(InputStream inputStream) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		FileSystemUtils.copy(inputStream, outputStream);
		inputStream.reset();
		return outputStream;
	}

	/**
	 * Parses the stateflow file from the {@link #stateflowInputStream} and returns
	 * a sanitized {@link MDLSection} that represents the machine.
	 */
	private MDLSection getStateFlowMachine() throws SimulinkModelBuildingException {
		SLXStateflowHandler handler = new SLXStateflowHandler();
		try {
			if (!slxContainsStateflowXml) {
				// there is no stateflow xml
				return null;
			}
			XMLUtils.parseSAX(new InputSource(stateflowInputStream), handler);
			MutableMDLSection slxFile = handler.getRootMachineSection();
			SLXModelSanitizer.sanitize(slxFile);
			return slxFile.asImmutable();
		} catch (SAXException | IOException e) {
			throw new SimulinkModelBuildingException(e);
		}
	}

	/**
	 * Moves the internal position of the given ZIP stream to point to the entry
	 * corresponding to the given file name. Returns true if the ZipEntry is found
	 * and the stream moved to the correct position, false otherwise and the stream
	 * position will point to the end of the stream.
	 */
	private static boolean moveStreamToFileEntry(ZipInputStream zipInputStream, String fileEntryName)
			throws IOException {
		ZipEntry zipEntry = zipInputStream.getNextEntry();
		while (zipEntry != null) {
			if (fileEntryName.equals(zipEntry.getName())) {
				return true;
			}
			zipEntry = zipInputStream.getNextEntry();
		}
		return false;
	}

	/**
	 * Extracts the data from .mxarray files and adds them to the
	 * {@link #modelMxData} list.
	 */
	private void extractMxDataForModel(ZipInputStream zipInputStream) throws IOException {
		modelMxData = new PairList<>();
		ZipEntry zipEntry = zipInputStream.getNextEntry();
		while (zipEntry != null) {
			if (zipEntry.getName().startsWith(SLX_MXDATA_FOLDER)) {
				String mxArrayFileContent = FileSystemUtils.readStream(zipInputStream);
				mxArrayFileContent = processMxarrayFileContent(mxArrayFileContent);
				if (mxArrayFileContent != null) {
					modelMxData.add(createVariantControlsKeyFromMxData(zipEntry.getName()), mxArrayFileContent);
					this.slxContainsMXData = true;
				}
			}
			zipEntry = zipInputStream.getNextEntry();
		}
	}

	/**
	 * Processes the mxarray file content by removing all control characters and
	 * shifting the string after the first occurrence of the char '@'.
	 */
	private static String processMxarrayFileContent(String data) {
		data = data.replaceAll(CONTROL_CHARACTERS_REGEX, "");
		int index = data.indexOf('@');
		if (index < 0) {
			return null;
		}
		return data.substring(index + 1);
	}

	/** Creates the key for the variant control data from the .mxarray files. */
	private static String createVariantControlsKeyFromMxData(String fileName) {
		fileName = StringUtils.removeAll(fileName, ".mxarray", SLX_MXDATA_FOLDER);
		return "bdmxdata:" + fileName;
	}

	/**
	 * Moves the internal position of the given ZIP stream to point to the
	 * {@link #SLX_MODEL_FILE}. Throws an exception if the entry was not found.
	 */
	private static void moveStreamToModelFileEntry(ZipInputStream zipInputStream) throws IOException {
		ZipEntry entry = zipInputStream.getNextEntry();
		while (entry != null) {
			if (SLX_MODEL_FILE.equals(entry.getName())) {
				return;
			}
			entry = zipInputStream.getNextEntry();
		}
		throw new IOException("No entry named " + SLX_MODEL_FILE + " found.");
	}

	/** Build and return model with default parameters. */
	public SimulinkModel buildModel() throws SimulinkModelBuildingException {
		return buildModel(new ModelBuildingParameters());
	}

	/** Build and return model. */
	public SimulinkModel buildModel(ModelBuildingParameters parameters) throws SimulinkModelBuildingException {
		parameters.setLogger(logger);
		MDLSection simulinkFile = parseFile(parameters);

		MDLSection modelSection = getSimulinkModelSection(simulinkFile);
		ModelDataHandler modelDataHandler = ModelDataHandlerFactory.createModelHandler(
				modelSection.getParameter(SimulinkConstants.PARAM_VERSION), isSlxFormat, parameters);
		SimulinkModel model = new SimulinkModel(modelSection.getName().equals(SECTION_LIBRARY), originId,
				modelDataHandler);
		addParameters(model, modelSection);
		saveMxDataAsParameters(model);
		if (isSlxFormat && StringUtils.isEmpty(model.getParameter(SimulinkConstants.PARAM_NAME))) {
			model.setParameter(SimulinkConstants.PARAM_NAME,
					FileSystemUtils.getFilenameWithoutExtension(new File(filename)));
		}

		// build Stateflow machine first, as the state machines are referenced
		// from Simulink blocks
		buildStateFlowMachine(simulinkFile, model, modelDataHandler, parameters);

		new SimulinkBuilder(model, parameters, isSlxFormat).buildSimulink(modelSection);

		new SimulinkModelDataExtractor(isSlxFormat, logger, file).addDataToModel(simulinkFile, model);

		return model;
	}

	/** Saves the extracted MxData as parameters for the root model. */
	private void saveMxDataAsParameters(SimulinkModel model) {
		if (!isSlxFormat || !slxContainsMXData) {
			return;
		}
		for (int i = 0; i < modelMxData.size(); i++) {
			model.setParameter(modelMxData.getFirst(i), modelMxData.getSecond(i));
		}
	}

	/**
	 * Identifies the Stateflow section (if existent) and builds the corresponding
	 * machines.
	 */
	private void buildStateFlowMachine(MDLSection simulinkFile, SimulinkModel model, ModelDataHandler modelDataHandler,
			ModelBuildingParameters parameters) throws SimulinkModelBuildingException {
		MDLSection stateflowSection;
		if (isSlxFormat && slxContainsStateflowXml) {
			stateflowSection = getStateFlowMachine();
		} else {
			stateflowSection = simulinkFile.getFirstSubSection(SECTION_STATEFLOW);
		}

		if (stateflowSection != null) {
			new StateflowBuilder(parameters, model).buildStateflow(stateflowSection, modelDataHandler);
		}
	}

	/**
	 * Determine the section that holds the Simulink model. This may be
	 * {@link SimulinkConstants#SECTION_MODEL} or
	 * {@link SimulinkConstants#SECTION_LIBRARY}</code>.
	 * 
	 * @param simulinkFile
	 *            the Simulink file
	 * @throws SimulinkModelBuildingException
	 *             if no or multiple {@link SimulinkConstants#SECTION_MODEL}/
	 *             {@link SimulinkConstants#SECTION_LIBRARY}</code> were found
	 */
	private static MDLSection getSimulinkModelSection(MDLSection simulinkFile) throws SimulinkModelBuildingException {
		List<MDLSection> namedBlocks = simulinkFile.getSubSections(SECTION_MODEL);

		if (namedBlocks.isEmpty()) {
			namedBlocks = simulinkFile.getSubSections(SECTION_LIBRARY);
		}

		if (namedBlocks.size() != 1) {
			throw new SimulinkModelBuildingException("Model must have exactly one Model or Library block.");
		}

		return namedBlocks.get(0);
	}

	/**
	 * Parse Simulink file.
	 * 
	 * @throws SimulinkModelBuildingException
	 *             if an exception occurred during parsing.
	 */
	private MDLSection parseFile(ModelBuildingParameters parameters) throws SimulinkModelBuildingException {
		MDLSection section = null;
		if (isSlxFormat) {
			SLXModelHandler handler = new SLXModelHandler();
			try {
				XMLUtils.parseSAX(new InputSource(modelInputStream), handler);
				MutableMDLSection slxFile = handler.getRootModelSection();

				// Adding default settings to the model from bddefaults.xml
				if (this.hasDefaultsXmlFile) {
					SLXModelHandler defaultsHandler = new SLXModelHandler(
							SimulinkConstants.SECTION_BLOCK_DIAGRAM_DEFAULTS);
					XMLUtils.parseSAX(new InputSource(modelDefaultsInputStream), defaultsHandler);
					MutableMDLSection defaultsFile = defaultsHandler.getRootModelSection();
					slxFile.getFirstSubSection(SimulinkConstants.SECTION_MODEL)
							.addSubSections(defaultsFile.getSubSections().getValues());
				}
				// Modified: Simulink20+ Adding model contents from systems/system_root.xml
				if (this.hasSystemRootXmlFile) {
					SLXModelHandler systemHandler = new SLXModelHandler(
							SimulinkConstants.SECTION_SYSTEM);
					XMLUtils.parseSAX(new InputSource(systemInputStream), systemHandler);
					MutableMDLSection systemFile = systemHandler.getRootModelSection();
					MutableMDLSection systemSection = 
							slxFile.getFirstSubSection(SimulinkConstants.SECTION_MODEL)
								.getFirstSubSection(SimulinkConstants.SECTION_SYSTEM);
					systemSection.addSubSections(systemFile.getSubSections().getValues());
				}

				SLXModelSanitizer.sanitize(slxFile);
				section = slxFile.asImmutable();
			} catch (SAXException | IOException e) {
				throw new SimulinkModelBuildingException(e);
			}
		} else {
			try {
				MDLScanner scanner = new MDLScanner(
						new InputStreamReader(modelInputStream, determineMdlCharset(parameters)));
				MDLParser parser = new MDLParser(scanner, logger);
				section = (MDLSection) parser.parse().value;
			} catch (Exception e) {
				throw new SimulinkModelBuildingException(e);
			}
		}
		return section;
	}

	/**
	 * Determines the charset to be used. This should only be called for MDL files.
	 */
	private Charset determineMdlCharset(ModelBuildingParameters parameters) throws IOException {
		if (!parameters.isGuessMdlEncoding()) {
			return parameters.getCharset();
		}

		modelInputStream.mark(0);
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(modelInputStream));
			return guessMdlCharset(reader, parameters);
		} finally {
			modelInputStream.reset();
		}

	}

	/** Guesses the charset from the content of the given reader. */
	private Charset guessMdlCharset(BufferedReader reader, ModelBuildingParameters parameters) throws IOException {
		String line = reader.readLine();
		int lineCount = 0;
		while (line != null && (++lineCount <= ENCODING_GUESS_MAX_LINES)) {
			Matcher matcher = ENCODING_GUESS_PATTERN.matcher(line);
			if (!matcher.find()) {
				line = reader.readLine();
				continue;
			}

			try {
				return Charset.forName(matcher.group(1));
			} catch (UnsupportedCharsetException e) {
				logger.error("Charset found in MDL file (" + matcher.group(1)
						+ ") not supported by this installation. Falling back to " + parameters.getCharset().name());
				return parameters.getCharset();
			}
		}

		return parameters.getCharset();
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		FileSystemUtils.close(modelInputStream);
		FileSystemUtils.close(stateflowInputStream);
	}

	/**
	 * Add all parameters defined in a section to a Simulink block. The
	 * {@link SimulinkConstants#PARAM_POINTS} parameter is treated specially here.
	 * This parameter stores layout information and this is merged instead of
	 * overwritten. This behavior is required to deal with the hierarchy in lines
	 * caused by branches.
	 */
	/* package */static void addParameters(ParameterizedElement element, MDLSection section) {
		for (String name : section.getParameterNames()) {

			// we handle the points specially, as they must be joined to allow
			// proper layouting
			if (SimulinkConstants.PARAM_POINTS.equals(name)) {
				String value = element.getParameter(SimulinkConstants.PARAM_POINTS);
				String newValue = section.getParameter(SimulinkConstants.PARAM_POINTS);
				if (value == null) {
					value = newValue;
				} else if (newValue != null) {
					// prepend value by stripping the opening/closing bracket
					// from the arrays, i.e. [1; 2] and [3; 4] should become
					// [1; 2; 3; 4]
					value = newValue.substring(0, newValue.length() - 1) + "; " + value.substring(1);
				}
				element.setParameter(SimulinkConstants.PARAM_POINTS, value);
			} else {
				element.setParameter(name, section.getParameter(name));
			}
		}
	}

}