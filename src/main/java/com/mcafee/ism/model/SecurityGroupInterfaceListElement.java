package com.mcafee.ism.model;

import com.intelsecurity.isc.plugin.manager.element.ManagerSecurityGroupInterfaceElement;

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
