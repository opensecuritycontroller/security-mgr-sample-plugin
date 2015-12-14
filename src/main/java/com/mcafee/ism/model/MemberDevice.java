package com.mcafee.ism.model;

import com.intelsecurity.isc.plugin.manager.element.ManagerDeviceMemberElement;

public class MemberDevice extends BaseIdNameObject implements ManagerDeviceMemberElement {

    public MemberDevice(Long id, String name) {
        super(id, name);
    }

}
