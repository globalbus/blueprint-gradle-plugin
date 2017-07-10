/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package info.globalbus.blueprint.plugin.gradle;

import info.globalbus.blueprint.plugin.handlers.Handlers;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.classloaderhandler.URLClassLoaderHandler;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
class FilteredClassFinder {

    @SuppressWarnings("unchecked")
    static Set<Class<?>> findClasses(ClassLoader finder, Collection<String> packageNames) {
        return findClasses(finder, packageNames, Handlers.BEAN_MARKING_ANNOTATION_CLASSES.toArray(new Class[Handlers
            .BEAN_MARKING_ANNOTATION_CLASSES.size()]));
    }

    private static Set<Class<?>> findClasses(ClassLoader finder, Collection<String> packageNames, Class<? extends
        Annotation>[] annotations) {
        FastClasspathScanner fastClasspathScanner = new FastClasspathScanner(packageNames.toArray(new String[0]));
        fastClasspathScanner.registerClassLoaderHandler(URLClassLoaderHandler.class);
        fastClasspathScanner.addClassLoader(finder);
        Set<Class<?>> rawClasses = new HashSet<>();
        for (Class<? extends Annotation> annotation : annotations) {
            fastClasspathScanner.matchClassesWithAnnotation(annotation, rawClasses::add);
        }
        fastClasspathScanner.scan();
        return rawClasses;
    }

}
