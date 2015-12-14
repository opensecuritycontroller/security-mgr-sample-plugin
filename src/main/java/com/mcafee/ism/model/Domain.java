package com.mcafee.ism.model;

import com.intelsecurity.isc.plugin.manager.element.ManagerDomainElement;

public class Domain extends BaseIdNameObject implements ManagerDomainElement {

    public Domain(Long id, String name) {
        super(id, name);
    }

}
