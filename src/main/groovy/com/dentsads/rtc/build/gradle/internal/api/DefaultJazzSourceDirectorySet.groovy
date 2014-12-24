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
import com.google.common.collect.Lists
import org.gradle.api.internal.file.FileResolver

class DefaultJazzSourceDirectorySet implements JazzSourceDirectorySet{

    private final String name;
    private final FileResolver fileResolver;
    private List<Object> source = Lists.newArrayList();

    public DefaultJazzSourceDirectorySet(String name, FileResolver fileResolver) {
        this.name = name;
        this.fileResolver = fileResolver;
    }
    
    @Override
    public String getName() {
        return name
    }

    @Override
    public JazzSourceDirectorySet srcDir(Object srcDir) {
        source.add(srcDir);
        return this;
    }

    @Override
    public JazzSourceDirectorySet srcDirs(Object... srcDirs) {
        Collections.addAll(source, srcDirs);
        return this;
    }

    @Override
    public JazzSourceDirectorySet setSrcDirs(Iterable<?> srcDirs) {
        source.clear();
        for (Object srcDir : srcDirs) {
            source.add(srcDir);
        }
        return this;
    }

    @Override
    public Set<File> getSrcDirs() {
        return fileResolver.resolveFiles(source.toArray()).getFiles();
    }

    @Override
    public String toString() {
        return source.toString();
    }
}
