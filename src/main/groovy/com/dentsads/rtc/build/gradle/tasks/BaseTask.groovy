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

import com.ibm.team.repository.client.ITeamRepository
import com.ibm.team.repository.client.TeamPlatform
import com.ibm.team.repository.common.TeamRepositoryException
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.NullProgressMonitor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input

class BaseTask extends DefaultTask{

    @Input String repositoryUrl;
    @Input String username;
    @Input String password;

    protected ITeamRepository login(String repositoryAddress, final String user, final String pass, IProgressMonitor monitor) throws TeamRepositoryException {
        ITeamRepository repository = TeamPlatform.getTeamRepositoryService().getTeamRepository(repositoryAddress);
        repository.registerLoginHandler(new ITeamRepository.ILoginHandler() {
            public ITeamRepository.ILoginHandler.ILoginInfo challenge(ITeamRepository repo) {
                return new ITeamRepository.ILoginHandler.ILoginInfo() {
                    public String getUserId() {
                        return user;
                    }
                    public String getPassword() {
                        return pass;
                    }
                };
            }
        });
        monitor.subTask("Contacting " + repository.getRepositoryURI() + "...");
        repository.login(monitor);
        monitor.subTask("Connected");
        return repository;
    }

}
