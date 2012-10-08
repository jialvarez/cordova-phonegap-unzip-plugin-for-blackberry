Cordova / Phonegap unzip plugin working on BlackBerry
=====================================================

This plugin for BlackBerry devices allows:

* Download a zip file from external URL
* Unzip a gzip file from SDCard
* Get info of the zip file

Usage:

<pre>cd <dev_path>  && ant update -Dproject.path="<dev_path>\<project_name>" && cd <project_name> && copy <dev_path>\build\ext\cordova.1.7.0.jar <dev_path>\<project_path>\www\ext\ /Y && ant blackberry load-simulator</pre>

Example of usage:

<pre>cd \dev\cordova-phonegap-unzip-plugin-for-blackberry && ant update -Dproject.path="C:\dev\cordova-phonegap-unzip-plugin-for-blackberry\example" && cd example && copy C:\dev\cordova-phonegap-unzip-plugin-for-blackberry\build\ext\cordova.1.7.0.jar C:\dev\cordova-phonegap-unzip-plugin-for-blackberry\example\www\ext\ /Y && ant blackberry load-simulator</pre>
