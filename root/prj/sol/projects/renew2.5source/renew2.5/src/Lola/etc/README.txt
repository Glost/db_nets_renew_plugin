Lola Plugin for Renew
---------------------
Version: 0.7.4
Date: 19.09.2012
---------------------
Install:
- You will need a working build environment (configure, make, gcc)
  => install the package 'build-essential' 
     (sudo apt-get install build-essential)
- to draw reachability graphs you will need the package 'graphviz'
- run the compile-all-lolas.sh shell script, it will download the lola sources,
  compile the different required lola binaries to the lib folder
- run ant, this will compile the plugin and copy it to ../dist/plugins

Usage:
- commands are integrated in the menu of Renew (Plugins->Lola Integration)
- the Lola plugin registers export and import functionality for Lola net files. Accessible under 
  File->Export resp. File->Import.
  
Commands:
- "Show Lola GUI" - starts up the graphical user interface, from which all functionality of the 
  plugin can be accessed.
- "Check all verification tasks" - checks all Lola tasks found in the current drawing i.e. all 
  TextFigures that start with "ANALYSE" or "ASSERT" (upper case required). Depending on the result
  it creates a colored frame around the task text.
- "Check selected request" - checks a single selected verification task and colors the frame.
- "Check selected Transitions" - checks if selected transitions are dead. In this case, they are
  colored red, otherwise (non-dead) they are colored green.
- "Check selected Places" - checks if selected places are bounded. In this case, they are
  colored green, otherwise (unbounded) they are colored red.
- "Check all places and transitions" - checks and colors all places and transitions.

Classes:
- LolaAnalyzer, this class is responsible for calling Lola and producing a LolaResult.
  It offers public methods for checking transitions, places, tasks etc. which wrap the
  call to lola binaries and return a LolaResult object.
  On instantiation it creates a LolaFileCreator object.

- LolaFileCreator, is responsible for creating the Lola net file, creating task files and
  naming of places and transitions. 
  It offers the static method 'writeTemporaryLolaFile(CPNDrawing)' which creates a Lola net file
  in the default temp location and returns this File. Also it offers methods 'writeLolaFile' and 
  'writeTaskFile' which can control where the files are created.

- LolaTask, represents a verification request. Can determine it's type, check itself and color
  it's corresponding figure.

- LolaResult, represents the result of a lola call.

- LolaGUI, the graphical user interface.

- LolaHelper, provides mainly constants, like keys of tasks (e.g. PlaceKey -> "ANALYSE PLACE) and
  the map from task types to lola commands.

