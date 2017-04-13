package org.osc.manager.ism.model;

import org.osc.sdk.manager.element.ManagerDeviceElement;

public class Device extends BaseIdNameObject implements ManagerDeviceElement {

    public Device(Long id, String name) {
        super(id, name);
    }

}
