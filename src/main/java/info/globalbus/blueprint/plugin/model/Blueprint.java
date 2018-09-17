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
package info.globalbus.blueprint.plugin.model;

import info.globalbus.blueprint.plugin.BlueprintConfigurationImpl;
import info.globalbus.blueprint.plugin.handlers.Handlers;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import lombok.Getter;
import org.apache.aries.blueprint.plugin.spi.ContextEnricher;
import org.apache.aries.blueprint.plugin.spi.ContextInitializationHandler;
import org.apache.aries.blueprint.plugin.spi.XmlWriter;

public class Blueprint implements BlueprintRegistry, ContextEnricher, XmlWriter {
    private static final String NS_BLUEPRINT = "http://www.osgi.org/xmlns/blueprint/v1.0.0";

    private final BeanRefStore beanRefStore = new BeanRefStore();
    private final Map<String, XmlWriter> customWriters = new HashMap<>();
    private final BlueprintConfigurationImpl blueprintConfiguration;
    private final List<Bean> generatedBeans = new ArrayList<>();
    @Getter
    private final Set<String> interfaces = new HashSet<>();

    Blueprint(BlueprintConfigurationImpl blueprintConfiguration, Class<?>... beanClasses) {
        this(blueprintConfiguration, Arrays.asList(beanClasses));
    }

    public Blueprint(BlueprintConfigurationImpl blueprintConfiguration, Collection<Class<?>> beanClasses) {
        this.blueprintConfiguration = blueprintConfiguration;
        initContext();
        parseBeans(beanClasses);
        resolveDependency();
    }

    private void initContext() {
        for (ContextInitializationHandler contextInitializationHandler : Handlers.CONTEXT_INITIALIZATION_HANDLERS) {
            contextInitializationHandler.initContext(this);
        }
    }

    private void parseBeans(Collection<Class<?>> beanClasses) {
        for (Class<?> clazz : beanClasses) {
            parseBean(clazz);
        }
    }

    private void parseBean(Class<?> clazz) {
        Bean bean = new Bean(clazz, this);
        beanRefStore.addBean(bean.toBeanRef());
        generatedBeans.add(bean);
        addBeansFromFactories(bean);
    }

    private void addBeansFromFactories(Bean factoryBean) {
        for (Method method : factoryBean.clazz.getMethods()) {
            if (!isFactoryMethod(method)) {
                continue;
            }
            BeanFromFactory beanFromFactory = new BeanFromFactory(factoryBean, method, this);
            beanRefStore.addBean(beanFromFactory.toBeanRef());
            generatedBeans.add(beanFromFactory);
        }
    }

    private boolean isFactoryMethod(Method method) {
        boolean isFactoryMethod = false;
        for (Class<? extends Annotation> factoryMethodAnnotationClass : Handlers.FACTORY_METHOD_ANNOTATION_CLASSES) {
            Annotation annotation = AnnotationHelper.findAnnotation(method.getAnnotations(),
                factoryMethodAnnotationClass);
            if (annotation != null) {
                isFactoryMethod = true;
                break;
            }
        }
        return isFactoryMethod;
    }

    private void resolveDependency() {
        for (Bean bean : generatedBeans) {
            bean.resolveDependency(this);
        }
    }

    public BeanRef getMatching(BeanTemplate template) {
        return beanRefStore.getMatching(template);
    }

    public List<BeanRef> getAllMatching(BeanTemplate template) {
        return beanRefStore.getAllMatching(template);
    }

    Collection<Bean> getBeans() {
        return generatedBeans;
    }

    Map<String, XmlWriter> getCustomWriters() {
        return customWriters;
    }

    @Override
    public void addBean(String id, Class<?> clazz) {
        beanRefStore.addBean(new BeanRef(clazz, id, new Annotation[] {}));
    }

    @Override
    public void addBlueprintContentWriter(String id, XmlWriter blueprintWriter) {
        customWriters.put(id, blueprintWriter);
    }

    @Override
    public BlueprintConfigurationImpl getBlueprintConfiguration() {
        return blueprintConfiguration;
    }

    public void write(XMLStreamWriter writer) throws XMLStreamException {
        writeBlueprint(writer);
        writeTypeConverters(writer);
        writeBeans(writer);
        writeCustomWriters(writer);
        writer.writeEndElement();
    }

    private void writeTypeConverters(XMLStreamWriter writer) throws XMLStreamException {
        new CustomTypeConverterWriter(beanRefStore).write(writer);
    }

    private void writeCustomWriters(XMLStreamWriter writer) throws XMLStreamException {
        List<String> customWriterKeys = new ArrayList<>(customWriters.keySet());
        Collections.sort(customWriterKeys);
        for (String customWriterKey : customWriterKeys) {
            customWriters.get(customWriterKey).write(writer);
        }
    }

    private void writeBeans(XMLStreamWriter writer) throws XMLStreamException {
        Collections.sort(generatedBeans);
        for (Bean beanWriter : generatedBeans) {
            beanWriter.write(writer);
        }
    }

    private void writeBlueprint(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("blueprint");
        writer.writeDefaultNamespace(NS_BLUEPRINT);
        if (blueprintConfiguration.getDefaultActivation() != null) {
            writer.writeAttribute("default-activation", blueprintConfiguration.getDefaultActivation().toString());
        }
        if (blueprintConfiguration.getDefaultAvailability() != null) {
            writer.writeAttribute("default-availability", blueprintConfiguration.getDefaultAvailability().toString());
        }
        if (blueprintConfiguration.getDefaultTimeout() != null) {
            writer.writeAttribute("default-timeout", blueprintConfiguration.getDefaultTimeout().toString());
        }
    }

    public boolean shouldBeGenerated() {
        return !getBeans().isEmpty() || !customWriters.isEmpty();
    }

    public Set<String> getGeneratedPackages() {
        List<String> beans =
            generatedBeans.stream().map(b -> b.clazz.getPackage().getName()).collect(Collectors.toList());
        beans.addAll(interfaces);
        return beans.stream().filter(v -> !v.startsWith("java.")).collect(Collectors.toSet());
    }
}
