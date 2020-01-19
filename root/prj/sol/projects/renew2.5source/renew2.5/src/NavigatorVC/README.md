Navigator VersionControl Extension
==================================

This plugin extends the Navigator with Version Control System functionality.

* [Dependencies](#dependencies)
* [Required by](#required-by)

## Dependencies

The NavigatorVC plugin depends on

- `de.renew.navigator.NavigatorPlugin`
- `CH.ifa.draw.application.DrawApplication`
- `de.renew.navigator.NavigatorGui`

## Provisions

The NavigatorVC plugin provisions

- `de.renew.navigator.vc.FileDiffer`
- `de.renew.navigator.vc.VCSHolder`

## Required by

The NavigatorVC plugin is required by

- Navigator Git Extension `de.renew.navigator.vc.git`
- Navigator SVN Extension `de.renew.navigator.vc.svn`

## How to Extend

The easiest parts to extend are actions. They provide an easy method to add
a menu button with an action to perform. You can also add new file filters and
GUI extensions by writing a custom plugin and using the **NavigatorExtension**
interface.

You can also take a look at **CHANGELOG.md**
