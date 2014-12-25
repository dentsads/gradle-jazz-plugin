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
import com.dentsads.rtc.build.gradle.internal.model.BuildType
import com.dentsads.rtc.build.gradle.internal.model.DeploymentConfig
import com.dentsads.rtc.build.gradle.internal.model.ExtractionConfig
import com.dentsads.rtc.build.gradle.internal.model.MergeConfig
import com.dentsads.rtc.build.gradle.internal.model.RepositoryAuthentication
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.reflect.Instantiator
import org.gradle.util.ConfigureUtil

class JazzExtension {
    final NamedDomainObjectContainer<BuildType> buildTypes
    final NamedDomainObjectContainer<DeploymentConfig> deploymentConfigs
    final NamedDomainObjectContainer<JazzSourceSet> sourceSets
    final ExtractionConfig extractionConfig

    protected final JazzPlugin plugin

    public JazzExtension(JazzPlugin plugin, ProjectInternal project, Instantiator instantiator,
                        NamedDomainObjectContainer<BuildType> buildTypes,
                        NamedDomainObjectContainer<DeploymentConfig> deploymentConfigs,
                        NamedDomainObjectContainer<JazzSourceSet> sourceSets) {
        this.plugin = plugin
        this.buildTypes = buildTypes
        this.deploymentConfigs = deploymentConfigs
        this.sourceSets = sourceSets
        this.extractionConfig = instantiator.newInstance(ExtractionConfig.class)
        this.extractionConfig.mergeConfig = instantiator.newInstance(MergeConfig.class)
        this.extractionConfig.repository = instantiator.newInstance(RepositoryAuthentication.class)
    }

    void deploymentConfigs(Action<? super NamedDomainObjectContainer<DeploymentConfig>> action) {
        //plugin.checkTasksAlreadyCreated();
        action.execute(deploymentConfigs)
    }

    void buildTypes(Action<? super NamedDomainObjectContainer<BuildType>> action) {
        //plugin.checkTasksAlreadyCreated();
        action.execute(buildTypes)
    }
    
    void sourceSets(Action<NamedDomainObjectContainer<JazzSourceSet>> action) {
        //plugin.checkTasksAlreadyCreated();
        action.execute(sourceSets)
    }
    
    void extractionConfig(Closure closure) {
        ConfigureUtil.configure(closure, extractionConfig)
        //plugin.createExportTask()
    }

}
