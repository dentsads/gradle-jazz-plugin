gradle-jazz-plugin
==================

This is the Gradle plugin for the IBM Rational Team Concert Process Template build system,
the so called Jazz Plugin. It supports the development lifecycle (configuration, development, build and deployment)
of RTC Process Templates.

For issue tracking see the GitHub issues page: 
https://github.com/dentsads/gradle-jazz-plugin/issues

Features
========

Features of the Jazz plugin include:

* Configure, package, and build RTC Process Templates.
* Support automated extraction and deployment of packaged Process Template zips onto/from a live Jazz Team Server repository .
* Suppports the development process by automatically incorporating/calling a diff tool on the specification.txt file when exporting a fresh copy of an RTC process template.

The Jazz plugin fully integrates into the Gradle build lifecycle by
extending the Java plugin.
