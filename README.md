Cordova / Phonegap unzip plugin working on BlackBerry
=====================================================

This plugin for BlackBerry devices allows:

* Download a zip file from external URL
* Unzip a gzip file from SDCard
* Get info of the zip file

Usage:

cd <dev_path>  && ant update -Dproject.path="<dev_path>\<project_name>" && cd <project_name> && copy <dev_path>\build\ext\cordova.1.7.0.jar <dev_path>\<project_path>\www\ext\ /Y && ant blackberry load-simulator

Example of usage:

cd \dev\unzipBlackBerry && ant update -Dproject.path="C:\dev\unzipBlackBerry\example" && cd example && copy C:\dev\cordova-phonegap-unzip-plugin-blackberry\build\ext\cordova.1.7.0.jar C:\dev\cordova-phonegap-unzip-plugin-blackberry\example\www\ext\ /Y && ant blackberry load-simulator
