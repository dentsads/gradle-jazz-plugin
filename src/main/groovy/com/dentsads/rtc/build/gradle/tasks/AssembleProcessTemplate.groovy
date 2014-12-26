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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Zip

class AssembleProcessTemplate extends DefaultTask{
    @Input String assemblyMasterSourceSetPathString
    @Input String assemblySlaveSourceSetPathString
    @Input String templateName
    @Input String templateId
    @Input File buildDir
    @Input String buildTypeName
    
    @TaskAction
    void assembleProcessTemplate() {
        untransformProcessTemplate()
    }
    
    void untransformProcessTemplate() {
        logger.quiet("Packaging '$assemblyMasterSourceSetPathString' and '$assemblySlaveSourceSetPathString'.")

        untransformProcessTemplate(assemblySlaveSourceSetPathString)
        untransformProcessTemplate(assemblyMasterSourceSetPathString)
    }
    
    void untransformProcessTemplate(String assemblySourceSetPathString) {
        String suffixPath = assemblySourceSetPathString.substring(assemblySourceSetPathString.lastIndexOf(File.separator) + 1,assemblySourceSetPathString.length())
        String templateBuildBasePath = "${buildDir.absolutePath}${File.separator}${buildTypeName}${File.separator}${suffixPath}"
        String attachmentsPath = "${templateBuildBasePath}${File.separator}attachments"
        String processContentPath = "${attachmentsPath}${File.separator}processContent"

        logger.quiet("Deleting unpacked Process Template folder '${templateBuildBasePath}', if present")
        new File(templateBuildBasePath).deleteDir()
        
        // Make a fresh copy of build Type sources into the build directory
        project.copy {
            from assemblySourceSetPathString
            into templateBuildBasePath
        }

        // Zip the processContent folder in place and delete it
        Zip zip = project.tasks.maybeCreate("bundle${buildTypeName}", Zip)
        zip.from(processContentPath).into("processContent")
        zip.setArchiveName("processContent.zip")
        zip.destinationDir = project.file(attachmentsPath)
        zip.execute()
        
        new File(processContentPath).deleteDir()

        // get all files in the /attachments folder and put in array
        def attachmentFiles = []
        new File(attachmentsPath).traverse(type: groovy.io.FileType.FILES){ file ->
            attachmentFiles << file
        }

        // sort transformed attachment file array alphanumerically
        attachmentFiles.sort { 
          String fileRelativePath = it.absolutePath.replace(attachmentsPath,"")
          fileRelativePath.findAll("/").size() == 1 ? fileRelativePath.substring(1) : fileRelativePath
        }

        // Create attachment.txt file
        File attachmentsTxt = new File("${templateBuildBasePath}${File.separator}attachments.txt")
        attachmentsTxt.createNewFile()
        
        // copy transformed files into position and rename them according to their sorting index.
        // Write into attachments.txt accordingly
        attachmentFiles.eachWithIndex {File file, i ->
            project.copy {
                from file

                String fileRelativePath = file.absolutePath.replace(attachmentsPath,"")
                fileRelativePath = fileRelativePath.findAll("/").size() == 1 ? fileRelativePath.substring(1) : fileRelativePath
                attachmentsTxt.append("${fileRelativePath}\n")

                into "${attachmentsPath}-temp"

                rename { String fileName ->
                    String fileNameNoEnding = fileName.substring(0,fileName.lastIndexOf("."))
                    fileName.replaceFirst(fileNameNoEnding, String.valueOf(i))
                }
            }

        }

        // Delete old attachments folder with non-transformed content
        new File(attachmentsPath).deleteDir()

        // Rename temp attachments folder with transformed content
        new File("${attachmentsPath}-temp").renameTo(attachmentsPath)

        // Zip process Template in place
        TimeZone.setDefault(TimeZone.getTimeZone('UTC'))
        def now = new Date()
        String formattedDate = now.format("yyyyMMdd-HHmmss-SS")
        
        logger.quiet("Packaging zip container '${buildDir}${File.separator}${buildTypeName}-${suffixPath}-${formattedDate}.zip'")
        
        zip = project.tasks.maybeCreate("zip${buildTypeName}${suffixPath}", Zip)
        zip.from(templateBuildBasePath).into("template")
        zip.setArchiveName("${buildTypeName}-${suffixPath}-${formattedDate}.zip")
        zip.destinationDir = buildDir
        zip.execute()
    }

}
