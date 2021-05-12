# Getting Started
Tested under Ubuntu 20.04 and Windows 10
### Prerequisites: 

- Java JDK 12.0 or later
- Eclipse Modeling Tools
- Z3 Solver:
    - On Ubuntu, Z3 can be installed via "*sudo apt-get install z3 libz3-java*"
    - On Windows or for building from source, follow the instructions on <https://github.com/Z3Prover/z3>
    - In the latter case, the path to the */"Z3"/bin* directory has to be added to *PATH* (on Windows) or *LD_LIBRARY_PATH* (on Ubuntu)

### Starting Simulink2dL

Simulink2dL is a plugin for Eclipse Modeling Tools.
To get started, open Eclipse Modeling Tools and import Simulink2dL, e.g., via *File->import->Git* and choosing the "Simulink2dL" project.
Make sure the base project, as well as all 10 nested projects are selected.
To run Simulink2dL, go to *run configurations* and create a new eclipse application for the plugin project.
Once started, click on *Open Perspective* and choose *Simulink2dL Perspective*.
If everything is working correctly, there should be a *Transform* button visible in the tool bar.

# Transforming a Model

To transform a model from Simulink to dL, create a new project *File->New->Project->Simulink2dL Project*, enter a name for your project and choose the Simulink model to transform.
Then click on the newly created project *->input->simulink->"name of your model"* and start the transformation by clicking on the *Transform* button in the toolbar.

### Examples
The "*Examples*" folder contains the temperature control model from the ICFEM/FACS Papers.

- "*TemperatureControl19.slx*" is the default model.
- "*TemperatureControlService19.slx*" contains the temperature control model as a service, which is replaced by its contract during transformation.

##### Verification results can be found under:
https://www.uni-muenster.de/EmbSys/research/projects/SoVer-HySiM.html

- note that due to changes in the implementation the tactics may not be directly applicable to the newly transformed models.

# Notes
- Contracts currently have to be hard coded in java until a suitable parser and replacement method is implemented.
    - For an example see the [temperature control contract](Simulink2dL/simulink2dl.dlmodel.contracts/src/simulink2dl/dlmodel/contracts/helper/TemperatureControlContract.java)
- Vector or bus signals are currently not supported.
- Multirate features are considered experimental.
