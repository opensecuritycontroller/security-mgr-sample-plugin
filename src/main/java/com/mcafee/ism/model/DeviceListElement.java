package com.mcafee.ism.model;

import com.intelsecurity.isc.plugin.manager.element.ManagerDeviceElement;

public class DeviceListElement extends BaseIdNameObject implements ManagerDeviceElement {

    public DeviceListElement(Long id, String name) {
        super(id, name);
    }

}
