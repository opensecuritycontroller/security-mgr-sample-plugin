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

import org.apache.log4j.Logger;
import org.osc.manager.ism.model.PolicyListElement;
import org.osc.sdk.manager.api.ManagerPolicyApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;

public class IsmPolicyApi implements ManagerPolicyApi {

    Logger log = Logger.getLogger(IsmPolicyApi.class);

    private static ArrayList<PolicyListElement> policyList = new ArrayList<PolicyListElement>();
    static {
        policyList.add(new PolicyListElement(0L, "Platinum"));
        policyList.add(new PolicyListElement(1L, "Gold"));
        policyList.add(new PolicyListElement(2L, "Silver"));
        policyList.add(new PolicyListElement(3L, "Bronze"));
    }

    public IsmPolicyApi(ApplianceManagerConnectorElement mc) throws Exception {

    }

    public static IsmPolicyApi create(ApplianceManagerConnectorElement mc) throws Exception {
        return new IsmPolicyApi(mc);
    }

    @Override
    public PolicyListElement getPolicy(String policyId, String domainId) throws Exception {
        return policyList.get(Integer.parseInt(policyId));
    }

    @Override
    public List<PolicyListElement> getPolicyList(String domainId) throws Exception {
        return policyList;
    }
}
