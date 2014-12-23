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

import com.dentsads.rtc.build.gradle.internal.BaseTest
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class PluginDslTest extends BaseTest {
    public void testDeploymentConfigs() {
        //Project project = ProjectBuilder.builder().withProjectDir(
        //        new File(testDir, "deploymentConfigs")).build()

        Project project = ProjectBuilder.builder().build()
        
        project.apply plugin: 'jazz'

        project.jazz {
            deploymentConfigs {
                config_localhost{
                    activeFeatureIds = ['com.ibm.foo.feature',
                                        'com.bosch.foo.feature']
                    repository {
                        username "foo"
                        
                    }
                }
            }
        }

        assertEquals("foo", project.jazz.deploymentConfigs.config_localhost.repository.username)
    }

    public void testBuildTypes() {
        Project project = ProjectBuilder.builder().build()
        
        project.apply plugin: 'jazz'

        project.jazz {
            buildTypes {
                test{
                    templateName "foo"
                }
                test2 {}
                test3 {}
            }
        }

        JazzPlugin plugin = project.jazz.plugin
        plugin.createTasks()
        
        assertNull(project.jazz.buildTypes.test.templateId)
        
        String[] buildTypeNames = ["test",
                                   "test2",
                                   "test3"];

        for (String buildTypeName : buildTypeNames) {
            findNamedItem(project.jazz.plugin.buildTypes.values(), buildTypeName, "buildType Names");
        }

        String[] buildTypeTaskNames = ["testingAssembleTest",
                                       "testingAssembleTest2",
                                       "testingAssembleTest3"];

        for (String buildTypeTaskName : buildTypeTaskNames) {
            findNamedItem(project.tasks.asMap.values(), buildTypeTaskName, "buildType Task Names");
        }

    }
    
    public void testProjectAreaExtraction() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'jazz'

        project.jazz {
            extractionConfig {
                projectAreaName "TestArea1"
                templateId "TestArea1"
                //zipPath "Path1"

                repository {
                    username "TestJazzAdmin1"
                    password "TestJazzAdmin1"
                    repositoryUrl "https://localhost:9443/ccm"
                }
            }
        }

        JazzPlugin plugin = project.jazz.plugin
        plugin.createExportTask()
        //project.tasks.exportProcessTemplate.execute()

        findNamedItem(project.tasks.asMap.values(), "exportProcessTemplate", "buildType Task Names");

    }
}
