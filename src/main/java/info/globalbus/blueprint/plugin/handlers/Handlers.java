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
package info.globalbus.blueprint.plugin.handlers;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import lombok.experimental.UtilityClass;
import org.apache.aries.blueprint.plugin.spi.BeanAnnotationHandler;
import org.apache.aries.blueprint.plugin.spi.BeanFinder;
import org.apache.aries.blueprint.plugin.spi.CollectionDependencyAnnotationHandler;
import org.apache.aries.blueprint.plugin.spi.ContextInitializationHandler;
import org.apache.aries.blueprint.plugin.spi.CustomDependencyAnnotationHandler;
import org.apache.aries.blueprint.plugin.spi.FactoryMethodFinder;
import org.apache.aries.blueprint.plugin.spi.FieldAnnotationHandler;
import org.apache.aries.blueprint.plugin.spi.InjectLikeHandler;
import org.apache.aries.blueprint.plugin.spi.MethodAnnotationHandler;
import org.apache.aries.blueprint.plugin.spi.NamedLikeHandler;
import org.apache.aries.blueprint.plugin.spi.QualifingAnnotationFinder;
import org.apache.aries.blueprint.plugin.spi.ValueInjectionHandler;

@UtilityClass
public class Handlers {
    public static final List<Class<? extends Annotation>> BEAN_MARKING_ANNOTATION_CLASSES = new ArrayList<>();
    public static final List<Class<? extends Annotation>> SINGLETONS = new ArrayList<>();
    public static final List<InjectLikeHandler<? extends Annotation>> BEAN_INJECT_LIKE_HANDLERS = new ArrayList<>();
    public static final List<NamedLikeHandler> NAMED_LIKE_HANDLERS = new ArrayList<>();
    public static final List<ValueInjectionHandler<? extends Annotation>> VALUE_INJECTION_HANDLERS = new ArrayList<>();
    public static final List<BeanAnnotationHandler<? extends Annotation>> BEAN_ANNOTATION_HANDLERS = new ArrayList<>();
    public static final List<CustomDependencyAnnotationHandler<? extends Annotation>>
        CUSTOM_DEPENDENCY_ANNOTATION_HANDLERS = new ArrayList<>();
    public static final List<MethodAnnotationHandler<? extends Annotation>> METHOD_ANNOTATION_HANDLERS = new
        ArrayList<>();
    public static final List<FieldAnnotationHandler<? extends Annotation>> FIELD_ANNOTATION_HANDLERS = new
        ArrayList<>();
    public static final List<Class<? extends Annotation>> FACTORY_METHOD_ANNOTATION_CLASSES = new ArrayList<>();
    public static final List<Class<? extends Annotation>> QUALIFYING_ANNOTATION_CLASSES = new ArrayList<>();
    public static final List<ContextInitializationHandler> CONTEXT_INITIALIZATION_HANDLERS = new ArrayList<>();
    public static final List<CollectionDependencyAnnotationHandler<? extends Annotation>>
        COLLECTION_DEPENDENCY_ANNOTATION_HANDLERS = new ArrayList<>();

    static {
        for (BeanFinder<? extends Annotation> beanFinder : ServiceLoader.load(BeanFinder.class)) {
            BEAN_MARKING_ANNOTATION_CLASSES.add(beanFinder.getAnnotation());
            if (beanFinder.isSingleton()) {
                SINGLETONS.add(beanFinder.getAnnotation());
            }
        }

        for (InjectLikeHandler<? extends Annotation> injectLikeHandler : ServiceLoader.load(InjectLikeHandler.class)) {
            BEAN_INJECT_LIKE_HANDLERS.add(injectLikeHandler);
        }

        for (NamedLikeHandler namedLikeHandler : ServiceLoader.load(NamedLikeHandler.class)) {
            NAMED_LIKE_HANDLERS.add(namedLikeHandler);
        }

        for (ValueInjectionHandler<? extends Annotation> valueInjectionHandler : ServiceLoader.load
            (ValueInjectionHandler.class)) {
            VALUE_INJECTION_HANDLERS.add(valueInjectionHandler);
        }

        for (BeanAnnotationHandler<? extends Annotation> beanAnnotationHandler : ServiceLoader.load
            (BeanAnnotationHandler.class)) {
            BEAN_ANNOTATION_HANDLERS.add(beanAnnotationHandler);
        }

        for (CustomDependencyAnnotationHandler<? extends Annotation> customDependencyAnnotationHandler :
            ServiceLoader.load(CustomDependencyAnnotationHandler.class)) {
            CUSTOM_DEPENDENCY_ANNOTATION_HANDLERS.add(customDependencyAnnotationHandler);
        }

        for (MethodAnnotationHandler<? extends Annotation> methodAnnotationHandler : ServiceLoader.load
            (MethodAnnotationHandler.class)) {
            METHOD_ANNOTATION_HANDLERS.add(methodAnnotationHandler);
        }

        for (FieldAnnotationHandler<? extends Annotation> fieldAnnotationHandler : ServiceLoader.load
            (FieldAnnotationHandler.class)) {
            FIELD_ANNOTATION_HANDLERS.add(fieldAnnotationHandler);
        }

        for (FactoryMethodFinder<? extends Annotation> factoryMethodFinder : ServiceLoader.load(FactoryMethodFinder
            .class)) {
            FACTORY_METHOD_ANNOTATION_CLASSES.add(factoryMethodFinder.getAnnotation());
        }

        for (QualifingAnnotationFinder<? extends Annotation> qualifyingAnnotationFinder : ServiceLoader.load
            (QualifingAnnotationFinder.class)) {
            QUALIFYING_ANNOTATION_CLASSES.add(qualifyingAnnotationFinder.getAnnotation());
        }

        for (CollectionDependencyAnnotationHandler<? extends Annotation> collectionDependencyAnnotationHandler :
            ServiceLoader.load(CollectionDependencyAnnotationHandler.class)) {
            COLLECTION_DEPENDENCY_ANNOTATION_HANDLERS.add(collectionDependencyAnnotationHandler);
        }

        for (ContextInitializationHandler contextInitializationHandler :
            ServiceLoader.load(ContextInitializationHandler.class)) {
            CONTEXT_INITIALIZATION_HANDLERS.add(contextInitializationHandler);
        }
    }
}
