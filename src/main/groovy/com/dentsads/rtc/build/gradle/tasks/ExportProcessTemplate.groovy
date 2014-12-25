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
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class ExportProcessTemplate extends BaseTask{
    @Input String projectAreaName
    @Input @Optional String templateTempSuffix = "-temp"
    @OutputDirectory File zipPath
    
    static final String PROCESS_TEMPLATE_EXPORT_DIR_NAME = 'templateExports'
    
    IProcessItemService service
    ITeamRepository teamRepo
    IProgressMonitor monitor
    
    @TaskAction
    void exportProcessTemplate() {
        /*
        if (!getZipPath()) {
            setZipPath(createBuildExportPath())
            logger.quiet("zip export folder default is '$zipPath'")
        }
        */
        
        TeamPlatform.startup();
        this.monitor = new NullProgressMonitor()
        this.teamRepo = login(repositoryUrl,
                username, password, monitor);

        service = (IProcessItemService) teamRepo.getClientLibrary(IProcessItemService.class);
        IProjectAreaHandle projectAreaHandle = getProjectArea(projectAreaName);
        IProcessDefinition definition = ((ProcessClientService) service).createProcessDefinitionFromProjectArea(projectAreaHandle, projectAreaName + templateTempSuffix, projectAreaName + templateTempSuffix, "", monitor);
        
        exportProcessDefinition(zipPath.absolutePath, projectAreaName, definition, monitor);

        logger.quiet("deleting temporarily created template '$definition.name' from '$repositoryUrl'")
        ((ProcessClientService) service).delete(definition, true, monitor);
        
        TeamPlatform.shutdown();
    }

    private IProjectArea getProjectArea(final String projectAreaName) throws TeamRepositoryException
    {
        List<IProjectArea> projectAreas = (List<IProjectArea>) service.findAllProjectAreas(IProcessClientService.ALL_PROPERTIES, new NullProgressMonitor());

        IProjectArea targetProjectArea;
        
        for (IProjectArea projectArea: projectAreas) {
            if (projectArea.getName().equals(projectAreaName))
                targetProjectArea = projectArea;
            
        }

        return targetProjectArea;

    }

    private String exportProcessDefinition(String archivePath, String archiveName, IProcessDefinition definition, IProgressMonitor monitor) throws TeamRepositoryException, IOException {
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
            logger.quiet("exporting to $tempZipFile")
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
            teamRepo.contentManager().retrieveContent(content, stream, new SubProgressMonitor(monitor, 800));
        } catch (FileNotFoundException e) {
            throw new TeamRepositoryException(e);
        }
    }

    // Deletes and creates Build Export Path Folder for every execution
    private File createBuildExportPath() {
        File exportPath = new File(project.buildDir,  File.separator + PROCESS_TEMPLATE_EXPORT_DIR_NAME)
        
        return exportPath.exists() ? {exportPath.deleteDir() ; exportPath.mkdir() ; return exportPath}() : {exportPath.mkdir() ; return exportPath}()
    }
}
