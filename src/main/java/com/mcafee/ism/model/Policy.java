package com.mcafee.ism.model;

import com.intelsecurity.isc.plugin.manager.element.ManagerPolicyElement;

public class Policy extends BaseIdNameObject implements ManagerPolicyElement {

    public Policy(Long id, String name) {
        super(id, name);
    }

}
