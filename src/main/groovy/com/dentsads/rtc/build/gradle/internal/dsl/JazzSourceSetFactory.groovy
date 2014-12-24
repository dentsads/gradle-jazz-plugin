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
package com.dentsads.rtc.build.gradle.internal.dsl

import com.dentsads.rtc.build.gradle.api.JazzSourceSet
import com.dentsads.rtc.build.gradle.internal.api.DefaultJazzSourceSet
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.reflect.Instantiator

class JazzSourceSetFactory implements NamedDomainObjectFactory<JazzSourceSet>{

    private final Instantiator instantiator;
    private final FileResolver fileResolver;
    
    public JazzSourceSetFactory(Instantiator instantiator,
                                FileResolver fileResolver) {
        this.instantiator = instantiator;
        this.fileResolver = fileResolver;
    }
    
    @Override
    JazzSourceSet create(String name) {
        return instantiator.newInstance(DefaultJazzSourceSet.class, name, fileResolver);
    }
}
