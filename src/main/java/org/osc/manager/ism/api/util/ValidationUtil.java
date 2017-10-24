/*******************************************************************************
 * Copyright (c) Intel Corporation
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
            return this.em.find(DeviceEntity.class, Long.valueOf(deviceId));
        });

        if (device == null) {
            throw new IllegalArgumentException(String.format("Cannot find device with id: %s", deviceId));
        }
        return device;
    }

    public void validateIdMatches(DeviceEntity entity, Long id, String objName)
            throws Exception {
        if (!id.equals(Long.parseLong(entity.getId()))) {
            throw new IllegalArgumentException(
                    String.format("The ID %s specified in the '%s' data does not match the id specified in the URL",
                            entity.getId(), objName));
        }
    }
}
