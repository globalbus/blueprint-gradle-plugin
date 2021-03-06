/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package info.globalbus.blueprint.plugin;

import info.globalbus.blueprint.plugin.gradle.PluginSettings;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.aries.blueprint.plugin.spi.Activation;
import org.apache.aries.blueprint.plugin.spi.Availability;
import org.apache.aries.blueprint.plugin.spi.BlueprintConfiguration;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class BlueprintConfigurationImpl implements BlueprintConfiguration {
    private final PluginSettings extension;
    private ClassLoader finder;

    @Override
    public Set<String> getNamespaces() {
        return extension.getNamespaces();
    }

    @Override
    public Activation getDefaultActivation() {
        return extension.getDefaultActivation();
    }

    @Override
    public Availability getDefaultAvailability() {
        return extension.getDefaultAvailability();
    }

    @Override
    public Long getDefaultTimeout() {
        return extension.getDefaultTimeout();
    }

    @Override
    public Map<String, String> getCustomParameters() {
        return extension.getCustomParameters();
    }
}
