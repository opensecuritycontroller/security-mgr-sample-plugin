package org.osc.manager.ism.model;

import org.osc.sdk.manager.element.ManagerSecurityGroupInterfaceElement;

public class SecurityGroupInterfaceListElement extends BaseIdNameObject implements ManagerSecurityGroupInterfaceElement {

    public SecurityGroupInterfaceListElement(Long id, String name, String policyId, String tag) {
        super(id, name);
        this.policyId = policyId;
        this.tag = tag;
    }

    private String policyId;
    private String tag;

    @Override
    public String getPolicyId() {
        return this.policyId;
    }

    @Override
    public String getTag() {
        return this.tag;
    }

    @Override
    public String getSecurityGroupInterfaceId() {
        return getId();
    }

}
