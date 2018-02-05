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

1.If have no "dropins" folder that make a folder and name "dropins" in your Eclipse(or MAT) home path

2.Download /bin/finder_xxxx.jar of this repository

3.Put the jar into the "dropins" folder

4.restart your eclipse or mat

How to use it:

There are two ways to use it.

1.Menu use

Open a hprof file with eclipse or mat,then you will see a toolbar item like

2.CMD console use

If you install matfinder to eclipse,then you must jump to eclipse's plugin folder and run follow cmd.
...\plugins>java -Dosgi.bundles=org.eclipse.mat.dtfj@4:start,org.eclipse.equinox.common@2:start,org.eclipse.update.configurator@3:start,org.eclipse.core.runtime@start -jar org.eclipse.equinox.launcher_1.3.0.v20140415-2008.jar -consoleLog -application org.eclipse.mat.api.parse "...\xxx.hprof" "...\FinderTask.xml"

If you install matfinder to MAT,then you can run follow cmd in the MAT-Home path directly.
MemoryAnalyzer -consolelog -application org.eclipse.mat.api.parse "...\xxx.hprof" "...\FinderTask.xml"

...\xxx.hprof:A memory dump file from Android OS for a app.


...\FinderTask.xml:A file like follow contents in it(you can use local xml to do it.In project "additional" folder).

<?xml version="1.0" encoding="UTF-8"?>
<section name="FinderSS">
	<query name="Overview">
		<param key="html.show_table_header" value="false"/>
		<command>
			heap_dump_overview
		</command>
	</query>
	<query name="Find Cloud Analyze">
		<command>
			FindCloudDebug -p .../MemoryRulesConfig.xml  -o .../outputfolder
		</command>
	</query>
</section>

.../MemoryRulesConfig.xml:A memory rule config file is created by MatFinder(see section:How to create memory rule).

.../outputfolder:Output file created by the cmd runing after(see section:What's mean of the outputfiles).

