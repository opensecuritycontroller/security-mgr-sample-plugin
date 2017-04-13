package org.osc.manager.ism.model;

import org.osc.sdk.manager.element.ManagerDeviceMemberElement;

public class MemberDevice extends BaseIdNameObject implements ManagerDeviceMemberElement {

    public MemberDevice(Long id, String name) {
        super(id, name);
    }

}
