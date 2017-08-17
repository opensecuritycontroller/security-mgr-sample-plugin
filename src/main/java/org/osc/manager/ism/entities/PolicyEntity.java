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

import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.DomainElement;
import org.osc.sdk.manager.element.ManagerPolicyElement;

public class PolicyEntity implements ManagerPolicyElement {

    private Long id;
    private String name;
    private ApplianceManagerConnectorElement applianceManagerConnector;
    private DomainElement domain;

    @Override
    public String getId() {
        return this.id.toString();
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

    public ApplianceManagerConnectorElement getApplianceManagerConnector() {
        return this.applianceManagerConnector;
    }

    void setApplianceManagerConnector(ApplianceManagerConnectorElement applianceManagerConnector) {
        this.applianceManagerConnector = applianceManagerConnector;
    }

    public DomainElement getDomain() {
        return this.domain;
    }

    public void setDomain(DomainElement domain) {
        this.domain = domain;
    }
}
