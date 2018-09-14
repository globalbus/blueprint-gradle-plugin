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

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

class BeanRefStore {
    private final SortedSet<BeanRef> reg = new TreeSet<>();

    void addBean(BeanRef beanRef) {
        rejectOnConflict(beanRef);
        reg.add(beanRef);
    }

    private void rejectOnConflict(BeanRef beanRef) {
        for (BeanRef bean : reg) {
            if (beanRef.conflictsWith(bean)) {
                throw new ConflictDetected(beanRef, bean);
            }
        }
    }

    BeanRef getMatching(BeanTemplate template) {
        for (BeanRef bean : reg) {
            if (bean.matches(template)) {
                return bean;
            }
        }
        return null;
    }

    List<BeanRef> getAllMatching(BeanTemplate template) {
        return reg.stream().filter(beanRef -> beanRef.matches(template)).collect(Collectors.toList());
    }
}
