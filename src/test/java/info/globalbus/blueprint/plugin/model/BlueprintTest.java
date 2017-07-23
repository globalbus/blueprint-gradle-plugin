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
package info.globalbus.blueprint.plugin.model;

import info.globalbus.blueprint.plugin.BlueprintConfigurationImpl;
import info.globalbus.blueprint.plugin.gradle.PluginSettings;
import info.globalbus.blueprint.plugin.test.MyBean3;
import info.globalbus.blueprint.plugin.test.ServiceReferences;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.container.Converter;

import static org.junit.Assert.assertEquals;

public class BlueprintTest {
    private static final String NS_JPA1 = "http://aries.apache.org/xmlns/jpa/v1.0.0";
    private static final String NS_TX1 = "http://aries.apache.org/xmlns/transactions/v1.0.0";

    private final BlueprintConfigurationImpl blueprintConfiguration;

    public BlueprintTest() {
        Set<String> namespaces = new HashSet<>(Arrays.asList(NS_JPA1, NS_TX1));
        PluginSettings extension = new PluginSettings();
        extension.setNamespaces(namespaces);
        blueprintConfiguration = new BlueprintConfigurationImpl(extension);
    }

    @Test
    public void testLists() {
        Blueprint blueprint = new Blueprint(blueprintConfiguration, MyBean3.class);
        Assert.assertEquals(1, blueprint.getBeans().size());
        Assert.assertEquals(0, getOsgiServices(blueprint).size());
    }

    @Test
    public void testLists2() {
        Blueprint blueprint = new Blueprint(blueprintConfiguration, ServiceReferences.class);
        Assert.assertEquals(1, blueprint.getBeans().size());
        Assert.assertEquals(3, getOsgiServices(blueprint).size());
    }

    private Set<String> getOsgiServices(Blueprint blueprint) {
        Set<String> blueprintWritersKeys = blueprint.getCustomWriters().keySet();
        Set<String> osgiServices = new HashSet<>();
        for (String blueprintWritersKey : blueprintWritersKeys) {
            if (blueprintWritersKey.startsWith("osgiService/")) {
                osgiServices.add(blueprintWritersKey);
            }
        }
        return osgiServices;
    }

    private void assertSpecialRef(String expectedId, Class<?> clazz) {
        Blueprint blueprint = new Blueprint(blueprintConfiguration);
        BeanRef ref = blueprint.getMatching(new BeanTemplate(clazz, new Annotation[] {}));
        assertEquals(expectedId, ref.id);
    }

    @Test
    public void testSpecialRefs() {
        assertSpecialRef("blueprintBundleContext", BundleContext.class);
        assertSpecialRef("blueprintBundle", Bundle.class);
        assertSpecialRef("blueprintContainer", BlueprintContainer.class);
        assertSpecialRef("blueprintConverter", Converter.class);
    }

}
