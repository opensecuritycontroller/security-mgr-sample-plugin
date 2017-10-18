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

import org.osc.sdk.manager.element.ApplianceSoftwareVersionElement;
import org.osc.sdk.manager.element.DistributedApplianceElement;
import org.osc.sdk.manager.element.DomainElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.osc.sdk.manager.element.VirtualizationConnectorElement;

public class VSElement implements VirtualSystemElement {

    private Long id;

    private String name;

    private Long mgrId;

    public VSElement(Long mgrId, String name) {
        this.mgrId = mgrId;
        this.name = name;
    }

    @Override
    public VirtualizationConnectorElement getVirtualizationConnector() {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getMgrId() {
        return Long.toString(this.mgrId);
    }

    @Override
    public byte[] getKeyStore() {
        return null;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public DomainElement getDomain() {
        return null;
    }

    @Override
    public DistributedApplianceElement getDistributedAppliance() {
        return null;
    }

    @Override
    public ApplianceSoftwareVersionElement getApplianceSoftwareVersion() {
        return null;
    }

    public void setMgrId(Long mgrId) {
        this.mgrId = mgrId;
    }
}