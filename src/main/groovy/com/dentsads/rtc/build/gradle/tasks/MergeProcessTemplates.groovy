/*
 * Copyright 2014 Dimitrios Dentsas
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dentsads.rtc.build.gradle.tasks

import groovy.io.FileType
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

class MergeProcessTemplates extends DefaultTask{
    @InputFile File processTemplateZipFile
    @InputDirectory File mergeTargetDirectory
    @InputFile File mergeToolExecutable

    @TaskAction
    void mergeProcessTemplates() {
        transformProcessTemplateZipFile()
        callMergeTool()
    }
    
    void transformProcessTemplateZipFile() {
        logger.quiet("Deleting unpacked Process Template folder, if present")
        new File(processTemplateZipFile.getAbsolutePath().replaceFirst("[.][^.]+\$", "")).deleteDir()

        logger.quiet("Unpacking Process Template Zip File '$processTemplateZipFile' and merging into target directory '$mergeTargetDirectory'")

        String zipFileName = processTemplateZipFile.getName().replaceFirst("[.][^.]+\$", "")
        String copyDirectoryPath = processTemplateZipFile.getAbsolutePath().substring(0,processTemplateZipFile.getAbsolutePath().lastIndexOf(File.separator))
        String templateBasePath = "${copyDirectoryPath}${File.separator}" + zipFileName + "${File.separator}template"
        String attachmentsPath = "${templateBasePath}${File.separator}attachments"
        String attachmentFilePath = "${templateBasePath}${File.separator}attachments.txt"

        FileTree zip = project.zipTree(processTemplateZipFile)

        // Unzip process Template container into folder with same name
        project.copy {
            from (zip) {
                into  zipFileName
            }
            into copyDirectoryPath
        }

        // get array of all lines in attachments.txt
        def attachmentFileLines = []
        new File( attachmentFilePath ).eachLine { line ->
            attachmentFileLines << line
        }

        // get all non-transformed files in the /attachments folder and put in array
        def attachmentFiles = []
        def dir = new File(attachmentsPath)
        dir.eachFileRecurse (FileType.FILES) { file ->
            attachmentFiles << file
        }

        // sort non-transformed attachment array alphanumerically
        attachmentFiles.sort(true, new Comparator<File>() {
            @Override
            int compare(File left, File right) {
                short leftInt = Short.valueOf(left.getName().replaceFirst("[.][^.]+\$", ""))
                short rightInt = Short.valueOf(right.getName().replaceFirst("[.][^.]+\$", ""))
                return leftInt > rightInt ? 1 : -1
            }
        })

        // copy non-transformed files into position and rename them according to the attachments.txt instructions
        attachmentFileLines.eachWithIndex { line, i ->
            project.copy {
                from "${attachmentsPath}${File.separator}${attachmentFiles[i].getName()}"

                String stringLine = line
                String stringLinePathName = stringLine.startsWith("/") ? stringLine.substring(0,stringLine.lastIndexOf("/")) : ""
                String stringLineFilename = stringLine.substring(stringLine.lastIndexOf("/") + 1, stringLine.length())

                //logger.quiet("original: " + attachmentFiles[i].getName() + " path name:" + stringLinePathName + " file name: " + stringLineFilename)

                into "${attachmentsPath}-temp${stringLinePathName}"

                rename { fileName ->
                    stringLineFilename
                }
            }

        }
        
        // Delete old attachments folder with non-transformed content
        new File(attachmentsPath).deleteDir()

        // Rename temp attachments folder with transformed content
        new File("${attachmentsPath}-temp").renameTo(attachmentsPath)

        // Unzip the processContent.zip file in place and delete it
        project.copy {
            from project.zipTree("${attachmentsPath}${File.separator}processContent.zip")

            into attachmentsPath
        }
        new File("${attachmentsPath}${File.separator}processContent.zip").delete()
        
        /* Delete attachments.txt after transformation,
         * since it could contain outdated information when new attachments are added 
         */
        new File(attachmentFilePath).delete()
    }
    
    void callMergeTool() {
        String zipFileName = processTemplateZipFile.getName().replaceFirst("[.][^.]+\$", "")
        String copyDirectoryPath = processTemplateZipFile.getAbsolutePath().substring(0,processTemplateZipFile.getAbsolutePath().lastIndexOf(File.separator))
        String templateBasePath = "${copyDirectoryPath}${File.separator}" + zipFileName + "${File.separator}template"

        project.exec {
            commandLine mergeToolExecutable.absolutePath,  templateBasePath, mergeTargetDirectory.absolutePath
        }
        
    }
}
