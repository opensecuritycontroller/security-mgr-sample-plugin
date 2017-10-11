package org.osc.manager.ism.api.util;

import javax.persistence.EntityManager;

import org.osc.manager.ism.entities.DeviceEntity;
import org.osgi.service.transaction.control.TransactionControl;

public class ValidationUtil {

    private final TransactionControl txControl;
    private final EntityManager em;

    public ValidationUtil(TransactionControl txControl, EntityManager em) {
        this.txControl = txControl;
        this.em = em;
    }

    public DeviceEntity getDeviceOrThrow(String deviceId) {

        DeviceEntity device = this.txControl.required(() -> {
            return this.em.find(DeviceEntity.class, deviceId);
        });

        if (device == null) {
            throw new IllegalArgumentException(String.format("Cannot find device with id: %s", deviceId));
        }
        return device;
    }
}
