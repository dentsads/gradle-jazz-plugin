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
package com.dentsads.rtc.build.gradle.internal.dsl

import com.dentsads.rtc.build.gradle.internal.model.DeploymentConfig
import com.dentsads.rtc.build.gradle.internal.model.RepositoryAuthentication
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.reflect.Instantiator

class DeploymentConfigFactory implements NamedDomainObjectFactory<DeploymentConfig> {

    final Instantiator instantiator
    final ProjectInternal project

    public DeploymentConfigFactory(Instantiator instantiator, ProjectInternal project) {
        this.instantiator = instantiator
        this.project = project
    }

    @Override
    DeploymentConfig create(String name) {
        DeploymentConfig depl = instantiator.newInstance(DeploymentConfigDsl.class, name, project)
        depl.repository = instantiator.newInstance(RepositoryAuthentication.class)
        
        return depl
    }
}
