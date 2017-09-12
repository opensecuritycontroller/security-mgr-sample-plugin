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
package org.osc.manager.ism.model;

import java.util.Set;

import org.osc.sdk.manager.element.ManagerSecurityGroupInterfaceElement;

public class SecurityGroupInterfaceListElement extends BaseIdNameObject implements ManagerSecurityGroupInterfaceElement {

    public SecurityGroupInterfaceListElement(Long id, String name, String policyId, String tag) {
        super(id, name);
        this.policyIds.add(policyId);
        this.tag = tag;
    }

    private Set<String> policyIds;
    private String tag;

    @Override
    public String getTag() {
        return this.tag;
    }

    @Override
    public String getSecurityGroupInterfaceId() {
        return getId();
    }

	@Override
	public String getManagerSecurityGroupId() {
		return null;
	}

	@Override
	public Set<String> getManagerPolicyIds() {
		return this.policyIds;
	}

}
