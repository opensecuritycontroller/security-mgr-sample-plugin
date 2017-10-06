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
package org.osc.manager.ism.api;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.osc.manager.ism.entities.DeviceMemberEntity;
import org.osc.manager.ism.utils.LogProvider;
import org.osc.sdk.manager.api.ManagerDeviceMemberApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.DistributedApplianceInstanceElement;
import org.osc.sdk.manager.element.ManagerDeviceMemberStatusElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.osgi.service.transaction.control.TransactionControl;
import org.slf4j.Logger;

public final class IsmAgentApi implements ManagerDeviceMemberApi {

    private static final Logger LOG = LogProvider.getLogger(IsmAgentApi.class);
    private IsmDeviceApi api;
    private TransactionControl txControl;
    private EntityManager em;
    private VirtualSystemElement vs;

    public IsmAgentApi(ApplianceManagerConnectorElement mc, VirtualSystemElement vs, TransactionControl txControl,
            EntityManager em) throws Exception {
        this.txControl = txControl;
        this.em = em;
        this.vs = vs;
        this.api = new IsmDeviceApi(this.vs, this.txControl, this.em);
    }

    public static IsmAgentApi create(ApplianceManagerConnectorElement mc, VirtualSystemElement vs,
            TransactionControl txControl, EntityManager em) throws Exception {
        return new IsmAgentApi(mc, vs, txControl, em);
    }

    @Override
    public List<ManagerDeviceMemberStatusElement> getFullStatus(List<DistributedApplianceInstanceElement> daiList) {
        List<ManagerDeviceMemberStatusElement> response = new ArrayList<>();
        if (daiList == null) {
            throw new IllegalArgumentException("The provided DAIs should not be null");
        }

        for (DistributedApplianceInstanceElement dai : daiList) {
            DeviceMemberEntity member = null;

            try {
                member = (DeviceMemberEntity) this.api.findDeviceMemberByName(dai.getName());
            } catch (Exception e) {
                LOG.error(String.format("Cannot find device by name %s", dai.getName()), e);
                DeviceMemberEntity memberStatus = new DeviceMemberEntity();
                memberStatus.setName(dai.getName());
                response.add(memberStatus);
            }

            if (member != null) {
                member.setDistributedApplianceInstanceElement(dai);
                response.add(member);
            }
        }

        return response;
    }

    @Override
    public void reAuthenticateAppliance() {
    }

    @Override
    public void syncAgent() {
    }
}

