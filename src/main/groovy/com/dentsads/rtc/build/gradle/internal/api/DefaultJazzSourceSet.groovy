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
package com.dentsads.rtc.build.gradle.internal.api

import com.dentsads.rtc.build.gradle.api.JazzSourceDirectorySet
import com.dentsads.rtc.build.gradle.api.JazzSourceSet
import org.gradle.api.internal.file.FileResolver

class DefaultJazzSourceSet implements JazzSourceSet{
    
    private final String name;
    private final JazzSourceDirectorySet master;
    private final JazzSourceDirectorySet slave;
    
    public DefaultJazzSourceSet(String name, FileResolver fileResolver) {
        this.name = name;

        String masterDisplayName = String.format("%s master", this.name);
        master = new DefaultJazzSourceDirectorySet(masterDisplayName, fileResolver);

        String slaveDisplayName = String.format("%s slave", this.name);
        slave = new DefaultJazzSourceDirectorySet(slaveDisplayName, fileResolver);
    }
    
    @Override
    String getName() {
        return name
    }

    @Override
    JazzSourceDirectorySet getMaster() {
        return master
    }

    @Override
    JazzSourceDirectorySet getSlave() {
        return slave
    }

    @Override
    JazzSourceSet setRoot(String path) {
        master.setSrcDirs(Collections.singletonList(path + "/master"))
        slave.setSrcDirs(Collections.singletonList(path + "/slave"))
        
        return this
    }
}
