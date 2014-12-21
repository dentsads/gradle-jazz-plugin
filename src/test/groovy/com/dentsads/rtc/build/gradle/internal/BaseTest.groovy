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
package com.dentsads.rtc.build.gradle.internal

import junit.framework.TestCase

import java.security.CodeSource

public abstract class BaseTest extends TestCase{

    protected File getRootDir() {
        CodeSource source = getClass().getProtectionDomain().getCodeSource()
        if (source != null) {
            URL location = source.getLocation();
            try {
                File dir = new File(location.toURI())
                assertTrue(dir.getPath(), dir.exists())
                File f= dir.getParentFile().getParentFile().getParentFile();
                return  new File(f, "build")
            } catch (URISyntaxException e) {
                fail(e.getLocalizedMessage())
            }
        }
        fail("Failed to fetch the tools/build folder")
    }

    protected File getTestDir() {
        File rootDir = getRootDir()
        return new File(rootDir, "tests")
    }

    protected static <T> T findNamedItemMaybe(Collection<T> items,
                                              String name) {
        for (T item : items) {
            if (name.equals(item.name)) {
                return item
            }
        }
        return null
    }

    protected static <T> T findNamedItem(Collection<T> items,
                                         String name,
                                         String typeName) {
        T foundItem = findNamedItemMaybe(items, name);
        assertNotNull("$name $typeName null-check", foundItem)
        return foundItem
    }
}
