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
package info.globalbus.blueprint.plugin.handlers.spring;

import org.apache.aries.blueprint.plugin.spi.BeanAnnotationHandler;
import org.apache.aries.blueprint.plugin.spi.BeanEnricher;
import org.apache.aries.blueprint.plugin.spi.ContextEnricher;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.DependsOn;

import java.lang.reflect.AnnotatedElement;

public class DependsOnAttributeResolver implements BeanAnnotationHandler<DependsOn> {
    @Override
    public Class<DependsOn> getAnnotation() {
        return DependsOn.class;
    }

    @Override
    public void handleBeanAnnotation(AnnotatedElement annotatedElement, String id, ContextEnricher contextEnricher, BeanEnricher beanEnricher) {
        DependsOn annotation = annotatedElement.getAnnotation(DependsOn.class);
        String[] value = annotation.value();
        if (value.length == 0) {
            return;
        }
        String dependsOnValue = StringUtils.join(value, " ");
        beanEnricher.addAttribute("depends-on", dependsOnValue);
    }

}
