Navigator Plugin
================

This plugin displays a window with a list of files which can be opened by
Renew.

* [Features](#features)
* [Dependencies](#dependencies)
* [Provisions](#provisions)
* [Required by](#required-by)
* [How to Extend](#how-to-extend)

## Features

- **FilesystemController** loads current File situation from the disc
- **AutosaveController** persists the current navigator state to preferences

## Dependencies

The Navigator plugin depends on

- `CH.ifa.draw.application.MenuManager`
- `CH.ifa.draw.io.ImportHolder`
- `CH.ifa.draw.IOHelper`

## Provisions

The Navigator plugin provisions

- `de.renew.navigator.NavigatorGui`
- `de.renew.navigator.models.NavigatorFileTree`

## Required by

The Navigator plugin is required by

- Navigator VersionControl Extension `de.renew.navigator.vc`
- Navigator Git Extension `de.renew.navigator.vc.git`
- Navigator SVN Extension `de.renew.navigator.vc.svn`
- Navigator Git Diff `de.renew.navigatordiff` (to be replaced)
- Navigator SVN Diff `de.renew.navigatorsvn` (to be replaced)
- Mulan Navigator Filters `de.renew.agent.navigator.filters`

## How to Extend

The easiest parts to extend are actions. They provide an easy method to add
a menu button with an action to perform. You can also add new file filters and
GUI extensions by writing a custom plugin and using the **NavigatorExtension**
interface.

You can also take a look at **CHANGELOG.md**

## Configuration

### Loading Files at Startup

Through the de.renew.navigator.filesAtStartup property it is possible to specify 
files that should be loaded at startup of the navigator. This property also 
defines which file are loaded in the navigator by clicking the home button of 
the navigator.

There exist several locations to define that property.
1. Inside the etc/plugin.cfg file of the Navigator plugin
1. Inside the .renew.properties file in the home directory of the user
1. Inside the testing/start.sh file of a Mulan plugin

The order of the list above is equal to the definition order on runtime. This 
means defining the property inside the .renew.properties file will overwrite the 
defined property inside the etc/plugin.cfg file.
