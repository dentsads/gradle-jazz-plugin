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

import java.util.List;

public class DeploymentConfig {
    private String fName = null;
    private List fActiveFeatureIds = null;
    private List fInactiveFeatureIds = null;
    private RepositoryAuthentication fRepository = null;

    public DeploymentConfig(String name) {
        fName = name;
    }

    public String getName() {
        return fName;
    }

    public void setName(String fName) {
        this.fName = fName;
    }

    public List getActiveFeatureIds() {
        return fActiveFeatureIds;
    }

    public void setActiveFeatureIds(List fActiveFeatureIds) {
        this.fActiveFeatureIds = fActiveFeatureIds;
    }

    public List getInactiveFeatureIds() {
        return fInactiveFeatureIds;
    }

    public void setInactiveFeatureIds(List fInactiveFeatureIds) {
        this.fInactiveFeatureIds = fInactiveFeatureIds;
    }

    public RepositoryAuthentication getRepository() {
        return fRepository;
    }

    public void setRepository(RepositoryAuthentication fRepository) {
        this.fRepository = fRepository;
    }

}
