/*******************************************************************************
 * Copyright (c) Intel Corporation
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.osc.manager.ism.entities;

import java.util.HashSet;
import java.util.Set;

import org.osc.sdk.manager.element.ManagerPolicyElement;
import org.osc.sdk.manager.element.SecurityGroupInterfaceElement;

public class SecurityGroupInterfaceElementImpl implements SecurityGroupInterfaceElement {

    private Long id;

    private String name;

    private Set<PolicyEntity> policies = new HashSet<>();

    private String tag;

    private SecurityGroupEntity securityGroup;

    SecurityGroupInterfaceElementImpl() {
    }

    public SecurityGroupInterfaceElementImpl(SecurityGroupInterfaceEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.policies = entity.getPolicies();
        this.tag = entity.getTag();
        this.securityGroup = entity.getSecurityGroup();
    }

    public Long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Set<PolicyEntity> getPolicies() {
        return this.policies;
    }

    @Override
    public String getTag() {
        return this.tag;
    }

    public SecurityGroupEntity getSecurityGroup() {
        return this.securityGroup;
    }

    @Override
    public Set<ManagerPolicyElement> getManagerPolicyElements() {
        Set<ManagerPolicyElement> managerPolicyElements = new HashSet<>();
        for (PolicyEntity policy : this.policies) {
            managerPolicyElements.add(policy);
        }
        return managerPolicyElements;
    }

    @Override
    public String getManagerSecurityGroupInterfaceId() {
        return Long.toString(getId());
    }

    @Override
    public String getManagerSecurityGroupId() {
        return Long.toString(this.securityGroup.getId());
    }
}
