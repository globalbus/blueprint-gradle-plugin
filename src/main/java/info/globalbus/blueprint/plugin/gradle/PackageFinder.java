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

import java.io.File;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Value;
import lombok.experimental.UtilityClass;

@UtilityClass
class PackageFinder {
    Set<String> findPackagesInSources(Collection<File> compileSourceRoots) {
        Set<String> packages = new HashSet<>();
        for (File src : compileSourceRoots) {
            if (src.exists()) {
                packages.addAll(findPackageRoots(src));
            }
        }
        return packages;
    }

    private Set<String> findPackageRoots(File file) {
        Set<String> packages = new HashSet<>();
        Deque<SearchFile> stack = new ArrayDeque<>();
        stack.add(new SearchFile(null, file));
        while (!stack.isEmpty()) {
            SearchFile cur = stack.pop();
            File[] files = cur.f.listFiles();
            boolean foundFile = false;
            assert files != null;
            for (File child : files) {
                if (child.isFile()) {
                    packages.add(cur.prefix);
                    foundFile = true;
                }
            }
            if (foundFile) {
                continue;
            }
            Stream.of(files).filter(File::isDirectory).forEach(child ->
                stack.add(new SearchFile(Optional.ofNullable(cur.prefix).map(v -> v + "." + child.getName())
                    .orElse(child.getName()), child)));
        }
        return packages;
    }

    @Value
    private static class SearchFile {
        String prefix;
        File f;
    }
}
