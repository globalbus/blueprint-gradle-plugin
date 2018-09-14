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
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.github.classgraph.classloaderhandler.URLClassLoaderHandler;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
class FilteredClassFinder {

    @SuppressWarnings("unchecked")
    static Set<Class<?>> findClasses(ClassLoader finder, Collection<String> packageNames) {
        return findClasses(finder, packageNames, Handlers.BEAN_MARKING_ANNOTATION_CLASSES
            .toArray(new Class[Handlers.BEAN_MARKING_ANNOTATION_CLASSES.size()]));
    }

    private static Set<Class<?>> findClasses(ClassLoader finder, Collection<String> packageNames, Class<? extends
        Annotation>[] annotations) {
        ClassGraph classGraph = new ClassGraph();
        classGraph.whitelistPackages(packageNames.toArray(new String[0]));
        classGraph.enableAnnotationInfo();
        classGraph.registerClassLoaderHandler(URLClassLoaderHandler.class);
        classGraph.addClassLoader(finder);
        try (ScanResult result = classGraph.scan()) {
            Set<ClassInfo> classInfos = Stream.of(annotations)
                .map(v -> result.getClassesWithAnnotation(v.getName()))
                .flatMap(Collection::stream).collect(Collectors.toSet());
            return classInfos.stream().map(ClassInfo::loadClass).collect(Collectors.toSet());
        }
    }

}
