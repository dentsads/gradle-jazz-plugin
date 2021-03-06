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
* Supports the development process by automatically incorporating/calling a diff tool on the specification.txt file when exporting a fresh copy of an RTC process template. Currently 'meld' for Linux and 'Beyond Compare' for Windows are recommended and verified to work.

The Jazz plugin fully integrates into the Gradle build lifecycle by
extending the Java plugin.

Tasks and Lifecycle
===================

The Jazz plugin adds the following tasks and dependencies to the
build:

    :assembleExecution
      Assembles the Process Template declared by buildType execution.
      
    :assemblePlanning
      Assembles the Process Template declared by buildType planning.
      
    :deployAll
      Assembles, imports into RTC and instantiates all Process Templates declared by the build types.
      
    :deployExecution
      Assembles, imports into RTC and instantiates the Process Template declared by buildType execution.
      
    :deployPlanning
      Assembles, imports into RTC and instantiates the Process Template declared by buildType planning.
      
    :exportProcessTemplate
      Extracts and exports a Process Template .zip file for a given Project Area.
      
    :mergeProcessTemplates
      Prepares a two way merge of the exported Process Template with given target directory contents.
      
