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

public class RepositoryAuthentication {
    private String fRepositoryUrl = null;
    private String fUsername = null;
    private String fPassword = null;

    public String getRepositoryUrl() {
        return fRepositoryUrl;
    }

    public void setRepositoryUrl(String fRepositoryUrl) {
        this.fRepositoryUrl = fRepositoryUrl;
    }

    public String getUsername() {
        return fUsername;
    }

    public void setUsername(String fUsername) {
        this.fUsername = fUsername;
    }

    public String getPassword() {
        return fPassword;
    }

    public void setPassword(String fPassword) {
        this.fPassword = fPassword;
    }
}
