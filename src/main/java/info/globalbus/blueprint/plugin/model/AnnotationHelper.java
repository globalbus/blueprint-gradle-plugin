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
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.apache.aries.blueprint.plugin.spi.InjectLikeHandler;
import org.apache.aries.blueprint.plugin.spi.NamedLikeHandler;
import org.apache.aries.blueprint.plugin.spi.ValueInjectionHandler;

@UtilityClass
class AnnotationHelper {
    static final Class<? extends Annotation>[] injectDependencyAnnotations = findInjectDependencyAnnotations();

    private static Class<? extends Annotation>[] findInjectDependencyAnnotations() {
        List<Class<? extends Annotation>> classes = new ArrayList<>();
        for (InjectLikeHandler<? extends Annotation> injectLikeHandler : Handlers.BEAN_INJECT_LIKE_HANDLERS) {
            classes.add(injectLikeHandler.getAnnotation());
        }
        for (ValueInjectionHandler<? extends Annotation> valueInjectionHandler : Handlers.VALUE_INJECTION_HANDLERS) {
            classes.add(valueInjectionHandler.getAnnotation());
        }
        return classes.toArray(new Class[classes.size()]);
    }

    static String findValue(Annotation[] annotations) {
        for (ValueInjectionHandler<? extends Annotation> valueInjectionHandler : Handlers.VALUE_INJECTION_HANDLERS) {
            Object annotation = findAnnotation(annotations, valueInjectionHandler.getAnnotation());
            if (annotation != null) {
                String value = valueInjectionHandler.getValue(annotation);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    static String findName(Annotation[] annotations) {
        for (NamedLikeHandler<? extends Annotation> namedLikeHandler : Handlers.NAMED_LIKE_HANDLERS) {
            Object annotation = findAnnotation(annotations, namedLikeHandler.getAnnotation());
            if (annotation != null) {
                String value = namedLikeHandler.getName(annotation);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    static <T> T findAnnotation(Annotation[] annotations, Class<T> annotation) {
        for (Annotation a : annotations) {
            if (a.annotationType() == annotation) {
                return annotation.cast(a);
            }
        }
        return null;
    }

    static boolean findSingletons(Annotation[] annotations) {
        for (Class<? extends Annotation> singletonAnnotation : Handlers.SINGLETONS) {
            Object annotation = findAnnotation(annotations, singletonAnnotation);
            if (annotation != null) {
                return true;
            }
        }
        return false;
    }

    static boolean findSingleton(Class clazz) {
        for (Class<?> singletonAnnotation : Handlers.SINGLETONS) {
            if (clazz.getAnnotation(singletonAnnotation) != null) {
                return true;
            }
        }
        return false;
    }
}
