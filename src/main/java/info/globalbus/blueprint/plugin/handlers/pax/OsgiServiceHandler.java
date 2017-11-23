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
package info.globalbus.blueprint.plugin.handlers.pax;

import info.globalbus.blueprint.plugin.model.Blueprint;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import org.apache.aries.blueprint.plugin.spi.ContextEnricher;
import org.apache.aries.blueprint.plugin.spi.CustomDependencyAnnotationHandler;
import org.apache.aries.blueprint.plugin.spi.XmlWriter;
import org.gradle.api.GradleException;
import org.ops4j.pax.cdi.api.OsgiService;

public class OsgiServiceHandler implements CustomDependencyAnnotationHandler<OsgiService> {
    @Override
    public Class<OsgiService> getAnnotation() {
        return OsgiService.class;
    }

    @Override
    public String handleDependencyAnnotation(AnnotatedElement annotatedElement, String name, ContextEnricher
        contextEnricher) {
        Class<?> clazz = getClass(annotatedElement);
        OsgiService annotation = annotatedElement.getAnnotation(OsgiService.class);
        final ServiceFilter serviceFilter = extractServiceFilter(annotatedElement);
        XmlWriter xmlWriter;
        String id = name != null ? name : generateReferenceId(clazz, serviceFilter);
        if (List.class.isAssignableFrom(clazz)) {
            clazz = getGenericType(annotatedElement);
            id = name != null ? name : generateReferenceId(clazz, serviceFilter);
            xmlWriter = getXmlListWriter(id, clazz, annotation, serviceFilter);
        } else {
            xmlWriter = getXmlElementWriter(id, clazz, annotation, serviceFilter);
        }
        contextEnricher.addBean(id, clazz);
        contextEnricher.addBlueprintContentWriter(getWriterId(id, clazz), xmlWriter);
        Blueprint blueprint = (Blueprint) contextEnricher;
        blueprint.getInterfaces().add(clazz.getPackage().getName());
        return id;
    }

    @Override
    public String handleDependencyAnnotation(final Class<?> clazz, OsgiService annotation, String name,
        ContextEnricher contextEnricher) {
        final ServiceFilter serviceFilter = extractServiceFilter(annotation);
        final String id = name != null ? name : generateReferenceId(clazz, serviceFilter);

        contextEnricher.addBean(id, clazz);
        contextEnricher.addBlueprintContentWriter(getWriterId(id, clazz), getXmlElementWriter(id, clazz,
            annotation, serviceFilter));
        return id;
    }

    private XmlWriter getXmlListWriter(final String id, final Class<?> clazz, OsgiService annotation, final
    ServiceFilter serviceFilter) {
        return writer -> {
            writer.writeEmptyElement("reference-list");
            writer.writeAttribute("id", id);
            writer.writeAttribute("interface", clazz.getName());
            if (annotation.required()) {
                writer.writeAttribute("availability", "mandatory");
            } else {
                writer.writeAttribute("availability", "optional");
            }
            if (serviceFilter.filter != null && !"".equals(serviceFilter.filter)) {
                writer.writeAttribute("filter", serviceFilter.filter);
            }
            if (serviceFilter.compName != null && !"".equals(serviceFilter.compName)) {
                writer.writeAttribute("component-name", serviceFilter.compName);
            }
        };
    }

    private XmlWriter getXmlElementWriter(final String id, final Class<?> clazz, OsgiService annotation, final
    ServiceFilter serviceFilter) {
        return writer -> {
            writer.writeEmptyElement("reference");
            writer.writeAttribute("id", id);
            writer.writeAttribute("interface", clazz.getName());
            if (serviceFilter.filter != null && !"".equals(serviceFilter.filter)) {
                writer.writeAttribute("filter", serviceFilter.filter);
            }
            if (serviceFilter.compName != null && !"".equals(serviceFilter.compName)) {
                writer.writeAttribute("component-name", serviceFilter.compName);
            }
        };
    }

    private String getWriterId(String id, Class<?> clazz) {
        return "osgiService/" + clazz.getName() + "/" + id;
    }

    private Class<?> getClass(AnnotatedElement annotatedElement) {
        if (annotatedElement instanceof Class<?>) {
            return (Class<?>) annotatedElement;
        }
        if (annotatedElement instanceof Method) {
            return ((Method) annotatedElement).getParameterTypes()[0];
        }
        if (annotatedElement instanceof Field) {
            return ((Field) annotatedElement).getType();
        }
        throw new GradleException("Unknown annotated element");
    }

    private Class<?> getGenericType(AnnotatedElement annotatedElement) {
        ParameterizedType genericType;
        if (annotatedElement instanceof Method) {
            genericType = (ParameterizedType) ((Method) annotatedElement).getGenericParameterTypes()[0];
        } else if (annotatedElement instanceof Field) {
            genericType = (ParameterizedType) ((Field) annotatedElement).getGenericType();
        } else {
            throw new GradleException("Unknown annotated element");
        }
        return (Class<?>) (genericType).getActualTypeArguments()[0];
    }

    private ServiceFilter extractServiceFilter(AnnotatedElement annotatedElement) {
        OsgiService osgiService = annotatedElement.getAnnotation(OsgiService.class);
        return extractServiceFilter(osgiService);
    }

    private ServiceFilter extractServiceFilter(OsgiService osgiService) {
        String filterValue = osgiService.filter();
        return new ServiceFilter(filterValue);
    }

    private String generateReferenceId(Class clazz, ServiceFilter serviceFilter) {
        String prefix = getBeanNameFromSimpleName(clazz.getSimpleName());
        String suffix = createIdSuffix(serviceFilter);
        return prefix + suffix;
    }

    private static String getBeanNameFromSimpleName(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1, name.length());
    }

    private String createIdSuffix(ServiceFilter serviceFilter) {
        if (serviceFilter.filter != null) {
            return "-" + getId(serviceFilter.filter);
        }
        if (serviceFilter.compName != null) {
            return "-" + serviceFilter.compName;
        }
        return "";
    }

    private String getId(String raw) {
        StringBuilder builder = new StringBuilder();
        for (int c = 0; c < raw.length(); c++) {
            char ch = raw.charAt(c);
            if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9') {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    private static class ServiceFilter {
        final String filter;
        final String compName;

        ServiceFilter(String filterValue) {
            if (filterValue == null || filterValue.isEmpty()) {
                filter = null;
                compName = null;
            } else if (filterValue.contains("(")) {
                filter = filterValue;
                compName = null;
            } else {
                filter = null;
                compName = filterValue;
            }
        }
    }
}
