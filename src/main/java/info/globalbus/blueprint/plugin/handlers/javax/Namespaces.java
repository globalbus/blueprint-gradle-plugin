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
package info.globalbus.blueprint.plugin.handlers.javax;

import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Namespaces {
    public static final String PATTERN_NS_JPA1 = "http\\:\\/\\/aries\\.apache\\.org\\/xmlns\\/jpa\\/v1\\.(.)\\.(.)";
    public static final String PATTERN_NS_JPA2 = "http\\:\\/\\/aries\\.apache\\.org\\/xmlns\\/jpa\\/v2\\.(.)\\.(.)";
    public static final String PATTERN_NS_TX1 = "http\\:\\/\\/aries\\.apache\\.org\\/xmlns\\/transactions\\/v1\\.(.)"
        + "\\.(.)";
    public static final String PATTERN_NS_TX2 = "http\\:\\/\\/aries\\.apache\\.org\\/xmlns\\/transactions\\/v2\\.(.)"
        + "\\.(.)";
    public static final String NS_TX_1_2_0 = "http://aries.apache.org/xmlns/transactions/v1.2.0";

    public static String getNamespaceByPattern(Set<String> namespaces, String pattern) {
        for (String namespace : namespaces) {
            if (namespace.matches(pattern)) {
                return namespace;
            }
        }
        return null;
    }
}
