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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import org.conqat.lib.commons.logging.ILogger;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkEncodedDataUtil;
import org.conqat.lib.simulink.model.SimulinkModel;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLStructure;

/**
 * Adds MatData and Resources (such as images) to a {@link SimulinkModel}.
 */
public class SimulinkModelDataExtractor {

	/** Path to the data file of models (having a DocBlock) */
	private static final String SLX_DATA_FILE = "simulink/bdmxdata.mat";

	/** Field of the MLArray from which the contained text can be read. */
	private static final String SLX_DATA_CONTENT_FIELD = "content";

	/**
	 * Flag to indicate whether the model is in the new .slx or the old .mdl file
	 * format.
	 */
	private final boolean isSlxFormat;

	/** Logger. */
	private final ILogger logger;

	/** The file that will be parsed. May be <code>null</code>. */
	private final File file;

	/**
	 * Instantiates a new Simulink model data extractor.
	 *
	 * @param isSlxFormat
	 *            indicates whether the current model is in slx format
	 * @param logger
	 *            the logger
	 * @param file
	 *            the file from which the model will be built (may be
	 *            <code>null</code>).
	 */
	public SimulinkModelDataExtractor(boolean isSlxFormat, ILogger logger, File file) {
		this.isSlxFormat = isSlxFormat;
		this.logger = logger;
		this.file = file;
	}

	/**
	 * Adds the data to the model. Data are resources like images but also text for
	 * doc blocks.
	 */
	public void addDataToModel(MDLSection simulinkFile, SimulinkModel model) {
		addResourcesToModel(simulinkFile, model);
		if (model.hasDocBlock()) {
			addMatDataToModel(simulinkFile, model);
		}
	}

	/**
	 * Adds resources (especially images) to the model such that they can be
	 * rendered.
	 * 
	 * @param simulinkFile
	 *            the simulink file. For mdl files the image data is contained in a
	 *            MatResources section.
	 */
	private void addResourcesToModel(MDLSection simulinkFile, SimulinkModel model) {
		if (isSlxFormat) {
			addResourcesToModelFromSlxFile(model);
			return;
		}

		MDLSection matResourceSection = simulinkFile.getFirstSubSection(SimulinkConstants.SECTION_MAT_RESOURCES);
		if (matResourceSection == null) {
			return;
		}

		for (MDLSection section : matResourceSection.getSubSections()) {
			String path = section.getParameter("Path");
			try {
				BufferedImage image = SimulinkEncodedDataUtil.getImage(section.getParameter("Data"));
				model.addResource(path, image);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/** Adds resources obtained from a slx file to the given model. */
	private void addResourcesToModelFromSlxFile(SimulinkModel model) {
		if (file == null) {
			// should only be called if there is a model file set.
			return;
		}

		try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file))) {
			ZipEntry entry = zipInputStream.getNextEntry();
			while (entry != null) {
				String fileName = "/" + entry.getName();
				if (fileName.endsWith(".png") || fileName.endsWith(".jpg")) {
					BufferedImage image = ImageIO.read(zipInputStream);
					model.addResource(fileName, image);
				}
				entry = zipInputStream.getNextEntry();
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Adds text data that is saved in a MatData section or an extra file (
	 * {@link #SLX_DATA_FILE}) to the model.
	 * 
	 * @param simulinkFile
	 *            the simulink file. For mdl files the text data is contained in a
	 *            MatData section, for slx files
	 *            {@link #addMatDataToModelFromSlxFile(SimulinkModel)} will be
	 *            called.
	 */
	private void addMatDataToModel(MDLSection simulinkFile, SimulinkModel model) {

		if (isSlxFormat) {
			addMatDataToModelFromSlxFile(model);
			return;
		}

		MDLSection matDataSection = simulinkFile.getFirstSubSection(SimulinkConstants.SECTION_MAT_DATA);
		if (matDataSection == null) {
			return;
		}

		for (MDLSection section : matDataSection.getSubSections()) {
			String tag = section.getParameter("Tag");
			String text = SimulinkEncodedDataUtil.getText(section.getParameter("Data"));
			model.addText(tag, text);
		}
	}

	/** Adds text obtained from a slx file to the given model. */
	private void addMatDataToModelFromSlxFile(SimulinkModel model) {
		if (file == null) {
			return;
		}

		try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file))) {
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				if (zipEntry.getName().equals(SLX_DATA_FILE)) {
					processDataFile(model, zipInputStream);
				}
				zipEntry = zipInputStream.getNextEntry();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Processes the SLX_DATA_FILE of a slx model. To do so, the file contained in
	 * the zip is read and its content is added to the model.
	 */
	private static void processDataFile(SimulinkModel model, ZipInputStream zipInputStream) throws IOException {
		MatFileReader reader = new MatFileReader(zipInputStream);
		Map<String, MLArray> content = reader.getContent();
		for (Entry<String, MLArray> contentEntry : content.entrySet()) {
			String tag = contentEntry.getKey();
			MLArray array = reader.getMLArray(tag);

			MLChar field = null;
			if (array instanceof MLStructure) {
				field = (MLChar) ((MLStructure) array).getField(SLX_DATA_CONTENT_FIELD);
			} else if (array instanceof MLChar) {
				field = (MLChar) array;
			}

			if (field != null) {
				String text = field.getString(0);
				model.addText(tag, text);
			}
		}
	}
}
