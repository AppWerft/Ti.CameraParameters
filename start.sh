#!/bin/bash

APPID=de.appwerft.cameraparameters
VERSION=1.1.5

rm -rf build/;cd android; ant clean;ant ;  unzip -uo  dist/$APPID-android-$VERSION.zip  -d  ~/Library/Application\ Support/Titanium/;cd ..;

