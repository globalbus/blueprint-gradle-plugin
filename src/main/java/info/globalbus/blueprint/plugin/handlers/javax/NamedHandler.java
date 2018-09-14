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

import java.lang.reflect.AnnotatedElement;
import javax.inject.Named;
import org.apache.aries.blueprint.plugin.spi.NamedLikeHandler;
import org.apache.commons.lang3.StringUtils;

public class NamedHandler implements NamedLikeHandler {
    @Override
    public Class getAnnotation() {
        return Named.class;
    }

    @Override
    public String getName(Class clazz, AnnotatedElement annotatedElement) {
        Named annotation = annotatedElement.getAnnotation(Named.class);
        if (StringUtils.isEmpty(annotation.value())) {
            return null;
        }
        return annotation.value();
    }

    @Override
    public String getName(Object annotation) {
        Named named = (Named) annotation;
        if (StringUtils.isEmpty(named.value())) {
            return null;
        }
        return named.value();
    }
}
