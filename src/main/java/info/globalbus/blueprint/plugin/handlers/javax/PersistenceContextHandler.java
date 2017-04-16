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
package info.globalbus.blueprint.plugin.handlers.javax;

import org.apache.aries.blueprint.plugin.spi.BeanEnricher;
import org.apache.aries.blueprint.plugin.spi.ContextEnricher;
import org.apache.aries.blueprint.plugin.spi.FieldAnnotationHandler;

import javax.persistence.PersistenceContext;
import java.lang.reflect.Field;
import java.util.List;

public class PersistenceContextHandler implements FieldAnnotationHandler<PersistenceContext> {

    @Override
    public Class<PersistenceContext> getAnnotation() {
        return PersistenceContext.class;
    }

    @Override
    public void handleFieldAnnotation(Class<?> clazz, List<Field> fields, ContextEnricher contextEnricher, BeanEnricher beanEnricher) {
        final String nsJpa1 = Namespaces.getNamespaceByPattern(contextEnricher.getBlueprintConfiguration().getNamespaces(), Namespaces.PATTERN_NS_JPA1);
        if (nsJpa1 != null) {
            for (final Field field : fields) {
                final String name = field.getName();
                final PersistenceContext persistenceContext = field.getAnnotation(PersistenceContext.class);
                beanEnricher.addBeanContentWriter("javax.persistence.field.context/" + name, writer -> {
                    writer.writeEmptyElement("context");
                    writer.writeDefaultNamespace(nsJpa1);
                    writer.writeAttribute("unitname", persistenceContext.unitName());
                    writer.writeAttribute("property", name);
                });
            }
        }
        final String nsJpa2 = Namespaces.getNamespaceByPattern(contextEnricher.getBlueprintConfiguration().getNamespaces(), Namespaces.PATTERN_NS_JPA2);
        if (nsJpa2 != null) {
            contextEnricher.addBlueprintContentWriter("javax.persistence.enableJpa2", writer -> {
                writer.writeEmptyElement("enable");
                writer.writeDefaultNamespace(nsJpa2);
            });
        }
    }
}
