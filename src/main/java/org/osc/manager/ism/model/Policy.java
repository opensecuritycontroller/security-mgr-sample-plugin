package org.osc.manager.ism.model;

import org.osc.sdk.manager.element.ManagerPolicyElement;

public class Policy extends BaseIdNameObject implements ManagerPolicyElement {

    public Policy(Long id, String name) {
        super(id, name);
    }

}
