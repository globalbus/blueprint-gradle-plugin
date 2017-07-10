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

import info.globalbus.blueprint.plugin.handlers.Handlers;
import java.lang.annotation.Annotation;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.aries.blueprint.plugin.spi.CustomDependencyAnnotationHandler;
import org.apache.aries.blueprint.plugin.spi.XmlWriter;

import static info.globalbus.blueprint.plugin.model.AnnotationHelper.findName;
import static info.globalbus.blueprint.plugin.model.AnnotationHelper.findValue;
import static info.globalbus.blueprint.plugin.model.NamingHelper.getBeanName;

class Argument implements XmlWriter {
    private final String ref;
    private final String value;

    Argument(BlueprintRegistry blueprintRegistry, Class<?> argumentClass, Annotation[] annotations) {
        this.value = findValue(annotations);
        if (value != null) {
            ref = null;
            return;
        }
        this.ref = findRef(blueprintRegistry, argumentClass, annotations);
    }

    private String findRef(BlueprintRegistry blueprintRegistry, Class<?> argumentClass, Annotation[] annotations) {
        String name = findName(annotations);

        for (CustomDependencyAnnotationHandler customDependencyAnnotationHandler : Handlers
            .CUSTOM_DEPENDENCY_ANNOTATION_HANDLERS) {
            Annotation annotation = (Annotation) AnnotationHelper.findAnnotation(annotations,
                customDependencyAnnotationHandler.getAnnotation());
            if (annotation != null) {
                String generatedRef = customDependencyAnnotationHandler.handleDependencyAnnotation(argumentClass,
                    annotation, name, blueprintRegistry);
                if (generatedRef != null) {
                    name = generatedRef;
                    break;
                }
            }
        }

        if (name == null) {
            BeanTemplate template = new BeanTemplate(argumentClass, annotations);
            BeanRef bean = blueprintRegistry.getMatching(template);
            if (bean != null) {
                name = bean.id;
            } else {
                name = getBeanName(argumentClass);
            }
        }
        return name;
    }

    String getRef() {
        return this.ref;
    }

    String getValue() {
        return this.value;
    }

    @Override
    public void write(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEmptyElement("argument");
        if (ref != null) {
            writer.writeAttribute("ref", ref);
        } else if (value != null) {
            writer.writeAttribute("value", value);
        }
    }
}
