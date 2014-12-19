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
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.reflect.Instantiator
import org.gradle.util.ConfigureUtil

class DeploymentConfigDsl extends DeploymentConfig implements Serializable  {
    private static final long serialVersionUID = 1L
    private ProjectInternal project;

    public DeploymentConfigDsl(String name, ProjectInternal project) {
        super(name)
        this.project = project
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DeploymentConfigDsl that = (DeploymentConfigDsl) o;
        if (!fName.equals(that.fName)) return false;
        return true;
    }
}
