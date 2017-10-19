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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.osc.sdk.manager.element.ManagerPolicyElement;
import org.osc.sdk.manager.element.ManagerSecurityGroupInterfaceElement;

@Entity
@Table(name = "SECURITY_GROUP_INTERFACE")
public class SecurityGroupInterfaceEntity implements ManagerSecurityGroupInterfaceElement {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "SECURITY_GROUP_INTERFACE_POLICY",
            joinColumns = @JoinColumn(name = "sgi_fk", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "policy_fk", referencedColumnName = "id"))
    private Set<PolicyEntity> policies = new HashSet<>();

    @Column(name = "tag", nullable = true)
    private String tag;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "security_group_fk", nullable = true, foreignKey = @ForeignKey(name = "FK_SECURITY_GROUP"))
    private SecurityGroupEntity securityGroup;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_fk", nullable = false, foreignKey = @ForeignKey(name = "FK_SGI_DEVICE"))
    private DeviceEntity device;

    SecurityGroupInterfaceEntity() {
    }

    public SecurityGroupInterfaceEntity(String name, Set<PolicyEntity> policies, String tag,
            SecurityGroupEntity mgrSecurityGroup, DeviceEntity device) {
        this.name = name;
        this.policies = policies;
        this.tag = tag;
        this.securityGroup = mgrSecurityGroup;
        this.device = device;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<PolicyEntity> getPolicies() {
        return this.policies;
    }

    public void setPolicies(Set<PolicyEntity> policies) {
        this.policies = policies;
    }

    @Override
    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public SecurityGroupEntity getSecurityGroup() {
        return this.securityGroup;
    }

    public void setSecurityGroup(SecurityGroupEntity securityGroup) {
        this.securityGroup = securityGroup;
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
    public String getSecurityGroupInterfaceId() {
        return getId() == null ? null : getId().toString();
    }

    @Override
    public String getSecurityGroupId() {
        if (getSecurityGroup() != null) {
            return getSecurityGroup().getId() == null ? null : getSecurityGroup().getId().toString();
        }
        return null;
    }

    public DeviceEntity getDevice() {
        return this.device;
    }
}
