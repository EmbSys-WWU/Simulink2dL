
KeepScoreModel usage guide

1 Instantiation

The following three parameters are necessary (all of which require a parsed project in turn):

-oldModel a Model obtained e.g. by calling
	(MeMoPersistenceManager.getInstance()).getModel()
(note further technicalities found in KeepScoreTesterPage.makeModel())

-sourceModelFilePath a string containing the file system path to the source *.slx, obtained e.g. by calling
	project.getFolder("input").getFolder("simulink").getFile(model.getName() + ".slx").getLocation().toFile().getAbsolutePath();
You can find this path by starting the Eclipse plugin environment (runtime-EclipseApplication), opening a project you have created, navigating to "/input/simulink", right-clicking the *.slx file inside, clicking properties and opening the "Resource" view. The "Location" string holds the correct path.

-outputLocationPath a string containing the file system path to the folder to store auxiliary files for the project, obtained e.g. by calling
	(project.getFolder("output"+File.separator+"soamed")).getLocation().toOSString() + File.separatorChar;
(again note further technicalities)
You can find this path by starting the Eclipse plugin environment (runtime-EclipseApplication), opening a project you have created, right-clicking on the "output" folder inside it, clicking properties and opening the "Resource" view. The "Location" string holds the correct path.


2 Usage

After instantiating the KeepScoreModel object you can add Blocks and SignalLines by passing instances of these classes to the function "add". Similarly, you can delete Blocks and SignalLines by passing the "delete" function objects you want to delete. Take care to delete SignalLines before Blocks, as their deletion depends on Ports on Blocks.
For convenience of use, these methods return this.
Once you're done modifying the model, call "getMatlabCommandsString", which returns the Matlab commands reflecting the changes you have made to the model. Calling this method also prevents further modifications of this model as you most likely want to change the resulting model.

The MeMo project can apply your changes to the model. Call these functions in sequence:
	MeMoMatlabManager.connect();
	MeMoMatlabManager.eval((new KeepScoreModel(...)).getMatlabCommandsString());
	MeMoMatlabManager.diconnect(false);
To see your new model in the "MeMo Project Navigator" call:
	ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
(See KeepScoreWizard.performFinish() for details)
and open "[you_project_name]/output/soamed/".