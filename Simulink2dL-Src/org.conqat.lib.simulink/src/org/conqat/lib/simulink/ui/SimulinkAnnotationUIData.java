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
package org.conqat.lib.simulink.ui;

import org.conqat.lib.simulink.model.SimulinkAnnotation;
import org.conqat.lib.simulink.model.datahandler.AnnotationLayoutData;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;

/**
 * Contains all data necessary to render a simulink annotation.
 */
public class SimulinkAnnotationUIData {

	/**
	 * The annotation layout data contains the layout data like position,
	 * colors, border visibility etc.
	 */
	private final AnnotationLayoutData annotationLayoutData;

	/**
	 * The text. This may be <code>null</code> if the annotation contains an
	 * image
	 */
	private String text;

	/** Instantiates a new simulink annotation ui data. */
	public SimulinkAnnotationUIData(SimulinkAnnotation annotation) {
		annotationLayoutData = annotation.obtainLayoutData();
		LabelLayoutData labelLayoutData = annotation.obtainLabelData();
		if (labelLayoutData != null) {
			text = annotation.obtainLabelData().getText();
		}
	}

	/**
	 * @see #annotationLayoutData
	 */
	public AnnotationLayoutData getAnnotationLayoutData() {
		return annotationLayoutData;
	}

	/**
	 * @see #text
	 */
	public String getText() {
		return text;
	}
}
