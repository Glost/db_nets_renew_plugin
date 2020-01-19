# Changelog

## 14 October 2015 (1kmoelle)

* The lazy loading of Navigator's GUI is now done via a *Proxy* pattern.
* SwingGuiController is gone. It's logic is now directly in the NavigatorGuiImpl.
* *NavigatorVC* provides interfaces for Git and SVN implementations.
* Refactored Navigator*Diff plugins towards Dependency Injection.
* FileTreeCellRenderer now supports additional renderers to make rendering extendable.
* All in all, the refactoring removed many indirect accesses.

## August-September 2015 (1kmoelle)

- Refactored the complete plugin, as also its dependent plugins
- Applied some architecture to the plugin, e.g. 
	+ **Observer pattern** for the model to be watched by the GUI and Autosave
	+ **Composite pattern** for the hierarchy of the directory tree
	+ **Command pattern** for the menu buttons to encapsulate logic
- Autosave now gets written to `$preferencesLocation/navigator.xml`
- Controllers are used to handle logic with their related tasks
- Changed file and directory icons
- Support for more file types like XML, Java, Markdown etc.

## January, 22 2015 (3emuelle@inf...)

- Be aware that some plugins override the property *de.renew.navigator.filesAtStartup*, e.g. CAP6 plugins, within its start.sh script

## 26 March 2009 (3emuelle@inf...)

added the following features:

* show path of treenodes as tooltip
* "remove" button to menu bar
* load directories on startup:
	1. add the following lines to your .renew.properties file:
		de.renew.navigator.filesAtStartup=<dir1>;<dir2>
		de.renew.navigator.workspace=<path to your workspace>
	2. specify directories as a SEMICOLON separated list!!!
	3. subdirectories of your workspace can be added as follows, e.g.: 
			Mulan/Fipa;Renew/Navigator/src/de/renew/navigator
	4. directories beyond your workspace can be added as
	  fully qualified path, i.e.: `/<path>/<to>/<your>/<desired>/<dir>`
