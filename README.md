MatFinder

Author

Yunleifu(234557307@qq.com) from Tencent SNG production Test-team.

Introduction

This is a plugin for mat or eclipse which already have installed mat plugin.It will be effective to finder the Android memory leak as soon as possible.

How to build it
1.You should install Mat-plugin for your eclipse（you can use local package to do it.In project "additional" folder,and named "org.eclipse.mat.updatesite-1.4.0-SNAPSHOT-site.zip".By doing this can help you use matfinder better,because it is not a standard Mat,I change some interface for public that is helpful to show the bitmap object in a view.）And you can do this like this tutorials(https://docs.oracle.com/javame/dev-tools/jme-sdk-3.4/ecl/html/setup_eclipseenv.htm)
2.Import the plugin project to your eclipse.Right click Project Explorer view, select import menu item,expand General topic,select Existing Projects into Workspace.Then local the project folder,and finish.
3.Build it


How to install
1.If have no "dropins" folder that make a folder and name "dropins" in your Eclipse home path
2.Download /bin/finder_xxxx.jar of this repository
3.Put the jar into the "dropins" folder
4.restart your eclipse or mat

How to use it:
There are two ways to use it.

1.Menu use

Open a hprof file with eclipse or mat,then you will see a toolbar item like

2.CMD console use

