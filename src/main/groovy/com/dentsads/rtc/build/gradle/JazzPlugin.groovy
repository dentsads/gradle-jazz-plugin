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
import com.dentsads.rtc.build.gradle.tasks.ExportProcessTemplate
import com.dentsads.rtc.build.gradle.tasks.InstantiateProcessTemplate
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
    static final String DEPLOY_ALL_TASK_NAME = 'deployAll'
    static final String JAZZ_GROUP_NAME = 'Jazz'
    
    final Map<String, BuildTypeData> buildTypes = [:]
    final Map<String, DeploymentConfig> deploymentConfigs = [:]

    Logger logger = Logging.getLogger(JazzPlugin.class);

    protected Project project

    JazzExtension jazzExtension

    private boolean hasCreatedTasks = false

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
            //createExportTask()
        }
    }

   private void addBuildType(BuildType buildType) {
        String name = buildType.name
        checkName(name, "BuildType")

       def sourceSet = jazzExtension.sourceSets.maybeCreate(name)
       
        BuildTypeData buildTypeData = new BuildTypeData(buildType, sourceSet, project)
        buildTypes[name] = buildTypeData
    }
    
    private static void checkName(String name, String displayName) {
         if ("main".equals(name)) {
            throw new RuntimeException("${displayName} names cannot be 'main'")
         
         if ("All".equals(name))
             throw new RuntimeException("${displayName} names cannot be 'All'")
        }
    }
    
    public void createTasks() {
        if (hasCreatedTasks) {
            logger.quiet("Tasks have already been created, aborting task creation!")
            return
        }
        hasCreatedTasks = true

        logger.quiet("Creating Tasks for Build Type declarations!")
        
        createDeploymentTaskAll()
        
        for (BuildTypeData buildTypeData : buildTypes.values()) {
            createBuildTypeTasks(buildTypeData)
        }
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
        extractionTask.zipPath = jazzExtension.extractionConfig.zipPath
        extractionTask.templateId = jazzExtension.extractionConfig.templateId
        extractionTask.repositoryUrl = jazzExtension.extractionConfig.repository.repositoryUrl
        extractionTask.username = jazzExtension.extractionConfig.repository.username
        extractionTask.password = jazzExtension.extractionConfig.repository.password
    } 
    
    private void createDeploymentTaskAll() {
        Task deploymentTask = project.tasks.create(DEPLOY_ALL_TASK_NAME)
        deploymentTask.description = "Assembles, imports into RTC and instantiates all Process Template declared by the build types."
        deploymentTask.group = JAZZ_GROUP_NAME
    }
    
    private void createDeploymentTask(BuildTypeData buildTypeData) {
        Task deploymentTask = project.tasks.create("deploy${buildTypeData.buildType.name.capitalize()}", InstantiateProcessTemplate)
        deploymentTask.description = "Assembles, imports into RTC and instantiates the Process Template declared by buildType ${buildTypeData.buildType.name}."
        deploymentTask.group = JAZZ_GROUP_NAME
        
        //logger.quiet("master: " + buildTypeData.sourceSet.getMaster().toString())
        //logger.quiet("slave: " + buildTypeData.sourceSet.getSlave().toString())

        deploymentTask.templateName = buildTypeData.buildType.templateName
        deploymentTask.templateId = buildTypeData.buildType.templateId
        deploymentTask.repositoryUrl = buildTypeData.buildType.deployment.repository.repositoryUrl
        deploymentTask.username = buildTypeData.buildType.deployment.repository.username
        deploymentTask.password = buildTypeData.buildType.deployment.repository.password
        
        // depends on assembly
        //deploymentTask.dependsOn project.tasks.findByName("")
        
        // deployAll depends on this deployment task
        project.tasks.findByName(DEPLOY_ALL_TASK_NAME).dependsOn deploymentTask
    }
    
    private void createAssemblyTask(BuildTypeData buildTypeData) {
        
        
    }
}