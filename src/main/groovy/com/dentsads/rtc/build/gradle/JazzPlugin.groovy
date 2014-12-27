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
package com.dentsads.rtc.build.gradle

import com.dentsads.rtc.build.gradle.api.JazzSourceSet
import com.dentsads.rtc.build.gradle.internal.api.DefaultJazzSourceSet
import com.dentsads.rtc.build.gradle.internal.dsl.BuildTypeFactory
import com.dentsads.rtc.build.gradle.internal.dsl.DeploymentConfigDsl
import com.dentsads.rtc.build.gradle.internal.dsl.DeploymentConfigFactory
import com.dentsads.rtc.build.gradle.internal.dsl.JazzSourceSetFactory
import com.dentsads.rtc.build.gradle.internal.model.DeploymentConfig
import com.dentsads.rtc.build.gradle.internal.BuildTypeData
import com.dentsads.rtc.build.gradle.internal.model.BuildType
import com.dentsads.rtc.build.gradle.internal.model.MergeConfig
import com.dentsads.rtc.build.gradle.tasks.AssembleProcessTemplate
import com.dentsads.rtc.build.gradle.tasks.ExportProcessTemplate
import com.dentsads.rtc.build.gradle.tasks.InstantiateProcessTemplate
import com.dentsads.rtc.build.gradle.tasks.MergeProcessTemplates
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.internal.reflect.Instantiator

import javax.inject.Inject

class JazzPlugin implements Plugin<Project> {
    static final String EXPORT_PROCESS_TEMPLATE_TASK_NAME = 'exportProcessTemplate'
    static final String MERGE_PROCESS_TEMPLATE_TASK_NAME = 'mergeProcessTemplates'
    static final String DEPLOY_ALL_TASK_NAME = 'deployAll'
    static final String JAZZ_GROUP_NAME = 'Jazz'
    
    final Map<String, BuildTypeData> buildTypes = [:]
    final Map<String, DeploymentConfig> deploymentConfigs = [:]

    Logger logger = Logging.getLogger(JazzPlugin.class);

    protected Project project

    JazzExtension jazzExtension

    protected Instantiator instantiator

    @Inject
    public JazzPlugin(Instantiator instantiator) {
        this.instantiator = instantiator
    }

    @Override
    void apply(Project project) {
        this.project = project

        project.apply plugin: JavaBasePlugin

        def buildTypeContainer = project.container(BuildType,
                new BuildTypeFactory(instantiator,  project.fileResolver))

        def sourceSetsContainer = project.container(JazzSourceSet,
                new JazzSourceSetFactory(instantiator,  project.fileResolver))
        
        def deploymentConfigContainer = project.container(DeploymentConfig,
                new DeploymentConfigFactory(instantiator, (ProjectInternal)project))

        jazzExtension = project.extensions.create('jazz', JazzExtension, this, (ProjectInternal)project, instantiator,
        buildTypeContainer, deploymentConfigContainer, sourceSetsContainer)

        sourceSetsContainer.whenObjectAdded { JazzSourceSet sourceSet ->
            sourceSet.setRoot(String.format("src/%s", sourceSet.getName()))
        }
        
        buildTypeContainer.whenObjectAdded { BuildType buildType ->
            addBuildType(buildType)
        }

        deploymentConfigContainer.whenObjectAdded { DeploymentConfig deploymentConfig ->
            DeploymentConfigDsl deploymentConfigDsl = (DeploymentConfigDsl) deploymentConfig
            deploymentConfigs[deploymentConfigDsl.name] = deploymentConfig
        }

        project.afterEvaluate{
            createTasks()
        }
    }

   private void addBuildType(BuildType buildType) {
        String name = buildType.name
        checkName(name, "BuildType")

       def sourceSet = jazzExtension.sourceSets.maybeCreate(name)
       
        BuildTypeData buildTypeData = new BuildTypeData(buildType, sourceSet, project)
        buildTypes[name] = buildTypeData
    }
    
    private void checkName(String name, String displayName) {
         if ("main".equals(name)) 
            throw new RuntimeException("${displayName} names cannot be 'main'")
         
         if ("All".equals(name))
             throw new RuntimeException("${displayName} names cannot be 'All'")
    }
    
    public void createTasks() {
        logger.quiet("Creating Tasks for Build Type declarations!")
        
        createDeploymentTaskAll()
        
        for (BuildTypeData buildTypeData : buildTypes.values()) {
            createBuildTypeTasks(buildTypeData)
        }

        createExportTask()
    }
    
    public void createBuildTypeTasks(BuildTypeData buildTypeData) {
        createAssemblyTask(buildTypeData)
        createDeploymentTask(buildTypeData)
    }
    
    public void createExportTask() {
        Task extractionTask = project.tasks.create(EXPORT_PROCESS_TEMPLATE_TASK_NAME, ExportProcessTemplate)
        extractionTask.description = 'Extracts and exports a Process Template .zip file for a given Project Area.'
        extractionTask.group = JAZZ_GROUP_NAME
        
        extractionTask.projectAreaName = jazzExtension.extractionConfig.projectAreaName
        extractionTask.zipPath = project.file("${project.buildDir}/templateExports")
        extractionTask.repositoryUrl = jazzExtension.extractionConfig.repository.repositoryUrl
        extractionTask.username = jazzExtension.extractionConfig.repository.username
        extractionTask.password = jazzExtension.extractionConfig.repository.password

        File processTemplateZipFile = new File(extractionTask.zipPath, File.separator + extractionTask.projectAreaName + ".zip")
        createMergeProcessTemplateTask(processTemplateZipFile)

        // Task is never up to date and must always be executed
        //extractionTask.outputs.upToDateWhen {false}
        // Delete any previously created output files and directories
        //extractionTask.outputs.files.each {File file -> file.deleteDir()}
    } 
    
    private void createDeploymentTaskAll() {
        Task deploymentTask = project.tasks.create(DEPLOY_ALL_TASK_NAME)
        deploymentTask.description = "Assembles, imports into RTC and instantiates all Process Templates declared by the build types."
        deploymentTask.group = JAZZ_GROUP_NAME
    }
    
    private void createDeploymentTask(BuildTypeData buildTypeData) {
        Task deploymentTask = project.tasks.create(getDeployTaskNameForBuildTypeData(buildTypeData), InstantiateProcessTemplate)
        deploymentTask.description = "Assembles, imports into RTC and instantiates the Process Template declared by buildType ${buildTypeData.buildType.name}."
        deploymentTask.group = JAZZ_GROUP_NAME
        
        //logger.quiet("master: " + buildTypeData.sourceSet.getMaster().toString())
        //logger.quiet("slave: " + buildTypeData.sourceSet.getSlave().toString())

        deploymentTask.templateName = buildTypeData.buildType.templateName
        deploymentTask.templateId = buildTypeData.buildType.templateId
        deploymentTask.repositoryUrl = buildTypeData.buildType.deployment.repository.repositoryUrl
        deploymentTask.username = buildTypeData.buildType.deployment.repository.username
        deploymentTask.password = buildTypeData.buildType.deployment.repository.password
        deploymentTask.zipFiles = project.tasks.findByName(getAssemblyTaskNameForBuildTypeData(buildTypeData)).outputs.files
        
        // deployAll depends on this deployment task
        project.tasks.findByName(DEPLOY_ALL_TASK_NAME).dependsOn deploymentTask
    }
    
    public void createMergeProcessTemplateTask(File processTemplateZipFile) {
        Task mergeTask = project.tasks.create(MERGE_PROCESS_TEMPLATE_TASK_NAME, MergeProcessTemplates)
        mergeTask.description = 'Prepares a two way merge of the exported Process Template with given target directory contents.'
        mergeTask.group = JAZZ_GROUP_NAME

        MergeConfig mergeConfig = jazzExtension.extractionConfig.mergeConfig
        mergeTask.processTemplateZipFile = processTemplateZipFile
        mergeTask.mergeTargetDirectory = mergeConfig.mergeTargetDirectory
        mergeTask.mergeToolExecutable = mergeConfig.mergeToolExecutable

        // Task is never up to date and must always be executed
        //mergeTask.outputs.upToDateWhen {false}
        
        mergeTask.dependsOn project.tasks.findByName(EXPORT_PROCESS_TEMPLATE_TASK_NAME)
    }
    
    private void createAssemblyTask(BuildTypeData buildTypeData) {
        Task assembleTask = project.tasks.create(getAssemblyTaskNameForBuildTypeData(buildTypeData), AssembleProcessTemplate)
        assembleTask.description = "Assembles the Process Template declared by buildType ${buildTypeData.buildType.name}."
        assembleTask.group = JAZZ_GROUP_NAME

        BuildType type = buildTypeData.buildType
        DefaultJazzSourceSet defaultSourceSet = buildTypeData.sourceSet

        assembleTask.buildTypeName = type.name
        assembleTask.templateName = type.templateName
        assembleTask.templateId = type.templateId
        assembleTask.buildDir = project.file("${project.buildDir}/templateAssemblies")
        assembleTask.assemblyMasterSourceSetPathString = defaultSourceSet.master.getSrcDirs().iterator()[0]
        assembleTask.assemblySlaveSourceSetPathString = defaultSourceSet.slave.getSrcDirs().iterator()[0]
        assembleTask.assemblyResSourceSetPathString = defaultSourceSet.res.getSrcDirs().iterator()[0]


        // Setting up a formatted Timestamp
        TimeZone.setDefault(TimeZone.getTimeZone('UTC'))
        def now = new Date()
        String formattedTimestampString = now.format("yyyyMMdd-HHmmss-SS")

        assembleTask.formattedTimestampString = formattedTimestampString
        
        String suffixPathMaster = assembleTask.assemblyMasterSourceSetPathString
                .substring(assembleTask.assemblyMasterSourceSetPathString
                .lastIndexOf(File.separator) + 1,assembleTask.assemblyMasterSourceSetPathString.length())
        String suffixPathSlave =  assembleTask.assemblySlaveSourceSetPathString
                .substring( assembleTask.assemblySlaveSourceSetPathString
                .lastIndexOf(File.separator) + 1, assembleTask.assemblySlaveSourceSetPathString.length())

        File outputMaster = project.file("$assembleTask.buildDir/${type.name}-${suffixPathMaster}-${formattedTimestampString}.zip")
        File outputSlave = project.file("$assembleTask.buildDir/${type.name}-${suffixPathSlave}-${formattedTimestampString}.zip")
        
        assembleTask.outputs.files(outputMaster, outputSlave)
        
        // assemble depends on this assembly task
        project.tasks.assemble.dependsOn assembleTask
    }
    
    private String getAssemblyTaskNameForBuildTypeData(BuildTypeData buildTypeData) {
        "assemble${buildTypeData.buildType.name.capitalize()}"     
    }

    private String getDeployTaskNameForBuildTypeData(BuildTypeData buildTypeData) {
        "deploy${buildTypeData.buildType.name.capitalize()}"
    }
}