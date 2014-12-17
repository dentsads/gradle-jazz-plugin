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

import com.dentsads.rtc.build.gradle.internal.model.BuildType
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.reflect.Instantiator

class BuildTypeDsl extends BuildType implements Serializable  {
    private static final long serialVersionUID = 1L

    private final FileResolver fileResolver

    BuildTypeDsl(String name,
                 FileResolver fileResolver,
                 Instantiator instantiator) {
        super(name)
        this.fileResolver = fileResolver
    }

    @Override
    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (!super.equals(o)) return false
        return true
    }
}
