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

import com.google.common.base.CaseFormat;
import info.globalbus.blueprint.plugin.handlers.TransactionBase;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class SpringTransactionalFactory extends TransactionBase<Transactional> {

    public String getTransactionTypeName(Transactional transactional) {
        Propagation propagation = transactional.propagation();
        if (propagation == Propagation.NESTED) {
            throw new UnsupportedOperationException("Nested transactions not supported");
        }
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, propagation.name());
    }

    @Override
    public Class<Transactional> getAnnotation() {
        return Transactional.class;
    }
} 
