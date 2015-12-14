package com.mcafee.ism.model;

import com.intelsecurity.isc.plugin.manager.element.ManagerDeviceElement;

public class Device extends BaseIdNameObject implements ManagerDeviceElement {

    public Device(Long id, String name) {
        super(id, name);
    }

}
