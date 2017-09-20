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
package org.osc.manager.ism.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.osc.sdk.manager.element.ManagerDeviceMemberElement;

@Entity
@Table(name = "DEVICE_MEMBER")
public class DeviceMemberEntity implements ManagerDeviceMemberElement {

    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "name", unique = true, nullable = false)
    private String name;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_fk", nullable = false, foreignKey = @ForeignKey(name = "FK_DEVICE"))
    private DeviceEntity parent;

    @Override
    public String getId() {
        if (this.id == null) {
            return null;
        }
        return this.id.toString();
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeviceEntity getParent() {
        return this.parent;
    }

    public void setParent(DeviceEntity parent) {
        this.parent = parent;
    }
}
