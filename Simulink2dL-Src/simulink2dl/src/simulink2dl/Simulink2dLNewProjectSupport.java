/*******************************************************************************
 * Copyright (c) 2020
 * AG Embedded Systems, University of Münster
 * SESE Software and Embedded Systems Engineering, TU Berlin
 * 
 * Authors:
 * 	Paula Herber
 * 	Sabine Glesner
 * 	Timm Liebrenz
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package simulink2dl;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import simulink2dl.util.Constants;
import simulink2dl.util.PluginLogger;

/**
 * This is a helper class to create a new Simulink2dL Project.
 * 
 * @author Timm Liebrenz
 *
 */
public class Simulink2dLNewProjectSupport {

	/**
	 * Creates a new Simulink2dl project.
	 * 
	 * @param projectName
	 * @param location
	 * @param slModel
	 * @return
	 */
	public static IProject createProject(String projectName, URI location, File slModel) {
		if (projectName != null) {
			IProject project = createBaseProject(projectName, location);
			try {
				// Create the default file structure
				addNature(project);
				String[] paths = { Constants.OUTPUT_DIR + Constants.DL_DIR,
						Constants.INPUT_DIR + Constants.SIMULINK_DIR };
				addToProjectStructure(project, paths);
			} catch (CoreException e) {
				e.printStackTrace();
				project = null;
			}

			try {
				// Create a link to the original model
				String fullPathString = slModel.getCanonicalPath();
				String modelName = slModel.getName();
				IPath fullPath = new Path(fullPathString);
				IFile modelLink = project.getFile(Constants.INPUT_DIR + Constants.SIMULINK_DIR + modelName);
				if (project.getWorkspace().validateLinkLocation(modelLink, fullPath).isOK()) {
					modelLink.createLink(fullPath, IResource.NONE, null);
				} else {
					PluginLogger.error("Could not create link to Simulink model");
				}
			} catch (IOException e) {
				Simulink2dLPlugin.logException(e.toString(), e);
			} catch (CoreException e) {
				Simulink2dLPlugin.logException(e.toString(), e);
			}

			return project;
		}
		return null;
	}

	/**
	 * Creates new base project.
	 * 
	 * @param projectName
	 * @param location
	 * @return
	 */
	private static IProject createBaseProject(String projectName, URI location) {
		IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		if (!newProject.exists()) {
			URI projectLocation = location;
			IProjectDescription desc = newProject.getWorkspace().newProjectDescription(newProject.getName());
			if ((location != null) && ResourcesPlugin.getWorkspace().getRoot().getLocationURI().equals(location)) {
				projectLocation = null;
			}

			desc.setLocationURI(projectLocation);
			try {
				newProject.create(desc, null);
				if (!newProject.isOpen()) {
					newProject.open(null);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		return newProject;
	}

	/**
	 * Creates the given folders in the project file structure.
	 * 
	 * @param newProject
	 * @param paths
	 * @throws CoreException
	 */
	private static void addToProjectStructure(IProject newProject, String[] paths) throws CoreException {
		for (String path : paths) {
			IFolder newFolder = newProject.getFolder(path);
			createFolder(newFolder);
		}
	}

	/**
	 * Recursively creates all folders in a given path to a folder.
	 * 
	 * @param folder
	 * @throws CoreException
	 */
	private static void createFolder(IFolder folder) throws CoreException {
		IContainer parent = folder.getParent();
		if (parent instanceof IFolder) {
			createFolder((IFolder) parent);
		}
		if (!folder.exists()) {
			folder.create(false, true, null);
		}
	}

	/**
	 * Sets the project nature and description of the given project if it has no
	 * nature.
	 * 
	 * @param project
	 * @throws CoreException
	 */
	private static void addNature(IProject project) throws CoreException {
		if (!project.hasNature(ProjectNature.NATURE_ID)) {
			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = ProjectNature.NATURE_ID;
			description.setNatureIds(newNatures);

			IProgressMonitor monitor = null;
			project.setDescription(description, monitor);
		}
	}

}
