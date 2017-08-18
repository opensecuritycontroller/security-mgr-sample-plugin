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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.osc.sdk.manager.element.ManagerDomainElement;

@Entity
public class DomainEntity implements ManagerDomainElement {
    private String name;

    @Id
    @GeneratedValue
    private Long Id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "appliance_manager_connector_fk", nullable = false, foreignKey = @ForeignKey(name = "FK_DO_APPLIANCE_MANAGER_CONNECTOR"))
    private ApplianceManagerConnectorEntity applianceManagerConnector;

    @OneToMany(mappedBy = "domain", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PolicyEntity> policies = new ArrayList<PolicyEntity>();

    @Override
    public String getId() {
        if (this.Id == null) {
            return null;
        }
        return this.Id.toString();
    }

    public void setId(Long Id) {
        this.Id = Id;
    }

    public List<PolicyEntity> getPolicies() {
        return this.policies;
    }

    public void setPolicies(PolicyEntity policies) {
        this.policies.add(policies);
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApplianceManagerConnectorEntity getapplianceManagerConnector() {
        return this.applianceManagerConnector;
    }

    public void setapplianceManagerConnector(ApplianceManagerConnectorEntity applianceManagerConnector) {
        this.applianceManagerConnector = applianceManagerConnector;
    }
}
