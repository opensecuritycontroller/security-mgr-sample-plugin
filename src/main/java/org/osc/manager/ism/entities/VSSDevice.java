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

import static javax.persistence.FetchType.LAZY;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class VSSDevice {

    private Long id;
    
    private String name;
    
    private List<DeviceMember> deviceMembers;
    
    private List<SecurityGroup> securityGroups;

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, fetch=LAZY, mappedBy="parent")
    public List<DeviceMember> getDeviceMembers() {
        return deviceMembers;
    }

    public void setDeviceMembers(List<DeviceMember> deviceMembers) {
        this.deviceMembers = deviceMembers;
    }

    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, fetch=LAZY, mappedBy="parent")
    public List<SecurityGroup> getSecurityGroups() {
        return securityGroups;
    }
    
    public void setSecurityGroups(List<SecurityGroup> securityGroups) {
        this.securityGroups = securityGroups;
    }
}
