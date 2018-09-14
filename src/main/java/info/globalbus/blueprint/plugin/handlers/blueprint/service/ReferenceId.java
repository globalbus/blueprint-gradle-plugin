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
package info.globalbus.blueprint.plugin.handlers.blueprint.service;

import lombok.experimental.UtilityClass;
import org.apache.aries.blueprint.annotation.service.Availability;
import org.apache.aries.blueprint.annotation.service.MemberType;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.apache.aries.blueprint.annotation.service.ReferenceList;
import org.apache.aries.blueprint.plugin.spi.ContextEnricher;

@UtilityClass
class ReferenceId {
    static String generateReferenceId(Class clazz, Reference reference, ContextEnricher contextEnricher) {
        StringBuilder sb = new StringBuilder();
        writeBeanNameFromSimpleName(sb, clazz.getSimpleName());
        appendFilter(sb, reference.filter());
        appendComponentName(sb, reference.componentName());
        appendAvailability(sb, reference.availability(), contextEnricher);
        appendTimeout(sb, reference.timeout());
        return sb.toString().replaceAll("-+$", "");
    }

    private static void appendTimeout(StringBuilder sb, long timeout) {
        sb.append("-");
        if (ReferenceParameters.needTimeout(timeout)) {
            sb.append(timeout);
        }
    }

    private static void appendAvailability(StringBuilder sb, Availability availability,
        ContextEnricher contextEnricher) {
        sb.append("-");
        if (ReferenceParameters.needAvailability(contextEnricher, availability)) {
            sb.append(availability.name().toLowerCase());
        }
    }

    private static void appendComponentName(StringBuilder sb, String componentName) {
        sb.append("-");
        if (!"".equals(componentName)) {
            sb.append(componentName);
        }
    }

    private static void appendFilter(StringBuilder sb, String filter) {
        sb.append("-");
        if (!"".equals(filter)) {
            writeEscapedFilter(sb, filter);
        }
    }

    static String generateReferenceListId(ReferenceList referenceList, ContextEnricher contextEnricher) {
        StringBuilder sb = new StringBuilder("listOf-");
        writeBeanNameFromSimpleName(sb, referenceList.referenceInterface().getSimpleName());
        appendFilter(sb, referenceList.filter());
        appendComponentName(sb, referenceList.componentName());
        appendAvailability(sb, referenceList.availability(), contextEnricher);
        appendMemberType(sb, referenceList.memberType());
        return sb.toString().replaceAll("-+$", "");
    }

    private static void appendMemberType(StringBuilder sb, MemberType memberType) {
        sb.append("-");
        if (memberType == MemberType.SERVICE_REFERENCE) {
            sb.append("reference");
        }
    }

    private static void writeBeanNameFromSimpleName(StringBuilder sb, String name) {
        sb.append(name.substring(0, 1).toLowerCase());
        sb.append(name.substring(1));
    }

    private static void writeEscapedFilter(StringBuilder sb, String filter) {
        for (int c = 0; c < filter.length(); c++) {
            char ch = filter.charAt(c);
            if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9') {
                sb.append(ch);
            }
        }
    }
}
