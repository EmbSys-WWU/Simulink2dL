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
package org.conqat.lib.simulink.model;

import java.awt.image.BufferedImage;
import java.util.Set;

import org.conqat.lib.commons.test.ADeepCloneTestExclude;
import org.conqat.lib.simulink.model.datahandler.AnnotationLayoutData;
import org.conqat.lib.simulink.model.datahandler.EInterpreter;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;

/**
 * Class for Simulink annotations, which are basically comments in the Simulink
 * model.
 */
public class SimulinkAnnotation extends SimulinkElementBase {

	/** Parameter where the path of images is stored */
	private static final String IMAGE_PATH = "ImagePath";

	/**
	 * Name used for anonymous annotations (i.e. those without name parameter
	 * set).
	 */
	public static final String ANONYMOUS_ANNOTATION_NAME = "<unnamed annotation>";

	/** Create annotation. */
	public SimulinkAnnotation() {
		// only required to support default construction
	}

	/** Create annotation from other annotation (for deep cloning). */
	private SimulinkAnnotation(SimulinkAnnotation other) {
		super(other);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This may return {@link #ANONYMOUS_ANNOTATION_NAME} if the annotation has
	 * no name defined.
	 */
	@Override
	public String getName() {
		String name = super.getName();
		if (name == null) {
			return ANONYMOUS_ANNOTATION_NAME;
		}
		return name;
	}

	/**
	 * Get annotation default parameter.
	 */
	@Override
	/* package */String getDefaultParameter(String name) {
		return getModel().getAnnotationDefaultParameter(name);
	}

	/**
	 * Get annotation default parameter names.
	 */
	@Override
	/* package */Set<String> getDefaultParameterNames() {
		return getModel().getAnnotationDefaultParameterNames();
	}

	/** Deep clone annotation. */
	@Override
	public SimulinkAnnotation deepClone() {
		return new SimulinkAnnotation(this);
	}

	/**
	 * Returns the layout data for this annotation. This data is parsed from the
	 * model with each call, so repeated access should be avoided by storing the
	 * result in a local variable.
	 */
	@ADeepCloneTestExclude
	public AnnotationLayoutData obtainLayoutData() {
		return getModel().getModelDataHandler().getSimulinkLayoutHandler().obtainAnnotationLayoutData(this);
	}

	/**
	 * Returns the label data for this annotation. This data is parsed from the
	 * model with each call, so repeated access should be avoided by storing the
	 * result in a local variable.
	 */
	@ADeepCloneTestExclude
	public LabelLayoutData obtainLabelData() {
		return getModel().getModelDataHandler().getSimulinkLayoutHandler().obtainAnnotationLabelData(this);
	}

	/**
	 * Checks whether the annotation contains any image.
	 *
	 * @return <code>true</code> if there is an image in this annotation
	 */
	public boolean containsImage() {
		return getParameter(IMAGE_PATH) != null;
	}

	/**
	 * Gets the image contained in this annotation. Can be <code>null</code> if
	 * there is no image in the annotation or the path does not match any
	 * resource.
	 */
	public BufferedImage getImage() {
		String path = getParameter(IMAGE_PATH);
		// slx files reference images with [$unpackedFolder] in their path, but
		// the path parameter of the image does not contain that string.
		path = path.replace("[$unpackedFolder]", "");
		return getModel().getImage(path);
	}

	/** Returns the interpreter to use */
	@ADeepCloneTestExclude
	public EInterpreter getInterpreter() {
		return EInterpreter.getFromValue(getParameter(SimulinkConstants.PARAM_INTERPRETER));
	}
}
