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

import com.dentsads.rtc.build.gradle.internal.model.BuildType
import com.dentsads.rtc.build.gradle.internal.model.DeploymentConfig
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.reflect.Instantiator

class JazzExtension {
    final NamedDomainObjectContainer<BuildType> buildTypes
    final NamedDomainObjectContainer<DeploymentConfig> deploymentConfigs

    protected final JazzPlugin plugin

    public JazzExtension(JazzPlugin plugin, ProjectInternal project, Instantiator instantiator,
                        NamedDomainObjectContainer<BuildType> buildTypes,
                        NamedDomainObjectContainer<DeploymentConfig> deploymentConfigs) {
        this.plugin = plugin
        this.buildTypes = buildTypes
        this.deploymentConfigs = deploymentConfigs
    }

    void deploymentConfigs(Action<? super NamedDomainObjectContainer<DeploymentConfig>> action) {
        //plugin.checkTasksAlreadyCreated();
        action.execute(deploymentConfigs)
    }

    void buildTypes(Action<? super NamedDomainObjectContainer<BuildType>> action) {
        //plugin.checkTasksAlreadyCreated();
        action.execute(buildTypes)
    }

}
