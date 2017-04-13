package org.osc.manager.ism.model;

import org.osc.sdk.manager.element.ManagerSecurityGroupInterfaceElement;

public class SecurityGroupInterface extends BaseIdNameObject implements ManagerSecurityGroupInterfaceElement {

    private String policyId;
    private String tag;

    public SecurityGroupInterface(Long id, String name, String policyId, String tag) {
        super(id, name);
        this.policyId = policyId;
        this.tag = tag;
    }

    @Override
    public String getSecurityGroupInterfaceId() {
        return getId();
    }

    @Override
    public String getPolicyId() {
        return policyId;
    }

    @Override
    public String getTag() {
        return tag;
    }

}
