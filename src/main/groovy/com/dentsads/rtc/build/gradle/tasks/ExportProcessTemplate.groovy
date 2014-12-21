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

import com.dentsads.rtc.build.gradle.internal.model.RepositoryAuthentication
import com.ibm.team.process.client.IProcessClientService
import com.ibm.team.process.client.IProcessItemService
import com.ibm.team.process.common.IProcessDefinition
import com.ibm.team.process.common.IProjectArea
import com.ibm.team.process.common.IProjectAreaHandle
import com.ibm.team.process.internal.client.IProcessInternalClientService
import com.ibm.team.process.internal.client.ProcessClientService
import com.ibm.team.repository.client.ITeamRepository
import com.ibm.team.repository.client.TeamPlatform
import com.ibm.team.repository.common.IContent
import com.ibm.team.repository.common.TeamRepositoryException
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.SubProgressMonitor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class ExportProcessTemplate extends DefaultTask{
    @Input String templateName
    @Input String templateId
    @Input String zipPath
    String templateTempSuffix = "-temp"
    @Input RepositoryAuthentication repository
    
    ITeamRepository teamRepo
    IProcessItemService service
    
    @TaskAction
    void exportProcessDefinition() {
        println "execute export"
        
        TeamPlatform.startup();
        
        IProgressMonitor monitor = new NullProgressMonitor();
        
        teamRepo = login(repository.repositoryUrl,
                repository.username, repository.password, monitor);
        
        service = (IProcessItemService) teamRepo.getClientLibrary(IProcessItemService.class);
        IProjectAreaHandle projectAreaHandle = getProjectArea(templateName);
        IProcessDefinition definition = ((ProcessClientService) service).createProcessDefinitionFromProjectArea(projectAreaHandle, templateName + templateTempSuffix, templateName + templateTempSuffix, "", monitor);
        
        exportProcessDefinition(zipPath, templateName +templateTempSuffix, definition, monitor);

        ((ProcessClientService) service).delete(definition, true, monitor);
        
        TeamPlatform.shutdown();
    }
    
    public ITeamRepository login(String repositoryAddress, final String user, final String pass, IProgressMonitor monitor) throws TeamRepositoryException {
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

    public IProjectArea getProjectArea(final String projectAreaName) throws TeamRepositoryException
    {
        List<IProjectArea> projectAreas = (List<IProjectArea>) service.findAllProjectAreas(IProcessClientService.ALL_PROPERTIES, new NullProgressMonitor());

        IProjectArea targetProjectArea;
        
        for (IProjectArea projectArea: projectAreas) {
            if (projectArea.getName().equals(projectAreaName))
                targetProjectArea = projectArea;
            
        }

        return targetProjectArea;

    }

    public String exportProcessDefinition(String archivePath, String archiveName, IProcessDefinition definition, IProgressMonitor monitor) throws TeamRepositoryException, IOException {
        monitor.beginTask("", 1000); //$NON-NLS-1$

        String exportPath = archivePath + System.getProperty("file.separator") + archiveName + ".zip";

        File selectedDir = new File(archivePath);
        if (!selectedDir.exists()) {
            throw new TeamRepositoryException("Directory does not exist");
        } else if (!selectedDir.isDirectory()) {
            throw new TeamRepositoryException("Not a directory");
        }

        // Export to a temporary zip file
        File tempZipFile = new File(exportPath); //$NON-NLS-1$
        tempZipFile.createNewFile();

        try {
            exportToArchive(definition, tempZipFile, new SubProgressMonitor(monitor, 700));
            return exportPath;
        } finally {
        }
    }

    /**
     * Exports the given process definition to the given archive file.
     *
     * @param definition the definition to export
     * @param archive the archive file to export into.
     */
    private void exportToArchive(IProcessDefinition definition, File archive, IProgressMonitor monitor) throws TeamRepositoryException {
        monitor.beginTask("", 1000); //$NON-NLS-1$
        // get exported file from server

        IContent content = ((IProcessInternalClientService) teamRepo.getClientLibrary(IProcessInternalClientService.class)).createProcessDefinitionExportZip(definition.getItemId().getUuidValue(), new SubProgressMonitor(monitor, 200));
        // write content to file
        try {
            FileOutputStream stream = new FileOutputStream(archive);
            this.repo.contentManager().retrieveContent(content, stream, new SubProgressMonitor(monitor, 800));
        } catch (FileNotFoundException e) {
            throw new TeamRepositoryException(e);
        }
    }
}
