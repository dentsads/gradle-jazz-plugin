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
package com.dentsads.rtc.build.gradle.internal.model;

public class BuildType {

    private String fName = null;
    private String fTemplateName = null;
    private String fTemplateId = null;
    private String fZipFileName = null;
    private DeploymentConfig fDeployment = null;

    public BuildType(String name) {
        fName = name;
    }

    public String getName() { return fName; }

    public void setName(String name) {
        fName = name;
    }

    public String getTemplateName() { return fTemplateName; }

    public void setTemplateName(String templateName) {
        fTemplateName = templateName;
    }

    public String getTemplateId() {
        return fTemplateId;
    }

    public void setTemplateId(String templateId) {
        fTemplateId = templateId;
    }

    public String getZipFileName() {
        return fZipFileName;
    }

    public void setZipFileName(String zipFileName) {
        fZipFileName = zipFileName;
    }

    public DeploymentConfig getDeployment() {
        return fDeployment;
    }

    public void setDeployment(DeploymentConfig fDeployment) {
        this.fDeployment = fDeployment;
    }
}
