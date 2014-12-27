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

import com.ibm.team.process.common.IProcessDefinition
import com.ibm.team.process.internal.client.IProcessInternalClientService
import com.ibm.team.process.internal.common.util.ApplicationVisibilityConstants
import com.ibm.team.repository.client.ITeamRepository
import com.ibm.team.repository.client.TeamPlatform
import com.ibm.team.repository.common.IContent
import com.ibm.team.repository.common.LineDelimiter
import com.ibm.team.repository.common.TeamRepositoryException
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.SubProgressMonitor
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

class InstantiateProcessTemplate extends BaseTask{

    @Input String templateName
    @Input String templateId
    @InputFiles FileCollection zipFiles
    
    ITeamRepository teamRepo
    IProgressMonitor monitor
    
    @TaskAction
    void instantiateProcessTemplate() {
        if (!TeamPlatform.isStarted()) TeamPlatform.startup();
        this.monitor = new NullProgressMonitor()
        this.teamRepo = login(repositoryUrl,
                username, password, monitor);

        zipFiles.files.each { File file ->

            FileTree zip = project.zipTree(file)
            String processId = zip.matching { include "template/processId.txt"}.getSingleFile().getText()
            String name = zip.matching { include "template/name.txt"}.getSingleFile().getText()
            
            logger.quiet("importing template '$name' to repository '$repositoryUrl'")
            importProcessDefinition(file, processId, name, name, monitor)
        }

        if (TeamPlatform.isStarted()) TeamPlatform.shutdown();
    }

    public IProcessDefinition importProcessDefinition(File archiveFile, String definitionId, String definitionName, String definitionToOverwrite, IProgressMonitor monitor) throws TeamRepositoryException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        monitor.beginTask("", 1000); //$NON-NLS-1$


        //File archive;
        boolean isTempArchive = false;
        //archive = new File(archivePath);

        monitor.worked(50); // Creating a zip file could take some time
        IContent content;
        try {
            content = createBinaryContentFromFile(archiveFile, new SubProgressMonitor(monitor, 350));
        } finally {
            if (isTempArchive) {
                archiveFile.delete();
            }
        }

        IProcessInternalClientService processClient = (IProcessInternalClientService) teamRepo.getClientLibrary(IProcessInternalClientService.class);
        return processClient.importProcessDefinitionZip(content, definitionId, definitionName, definitionToOverwrite != null, ApplicationVisibilityConstants.SENTINEL_APPLICATION_ID, new SubProgressMonitor(monitor, 600));
    }

    private IContent createBinaryContentFromFile(File file, IProgressMonitor monitor) throws TeamRepositoryException {
        if (!file.exists()) {
            return null;
        }
        try {
            FileInputStream stream = new FileInputStream(file);
            return teamRepo.contentManager().storeContent(IContent.CONTENT_TYPE_UNKNOWN, null, LineDelimiter.LINE_DELIMITER_NONE, stream, null, monitor);
        } catch (IOException e) {
            throw new TeamRepositoryException(e);
        }
    }
}
