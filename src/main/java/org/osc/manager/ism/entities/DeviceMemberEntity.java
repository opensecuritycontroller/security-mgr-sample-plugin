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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.osc.sdk.manager.element.DistributedApplianceInstanceElement;
import org.osc.sdk.manager.element.ManagerDeviceMemberElement;
import org.osc.sdk.manager.element.ManagerDeviceMemberStatusElement;

@Entity
@Table(name = "DEVICE_MEMBER")
public class DeviceMemberEntity implements ManagerDeviceMemberElement, ManagerDeviceMemberStatusElement {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "name", unique = true, nullable = false)
    private String name;
    @Column(name = "version")
    private String version;
    @Column(name = "rx")
    public Long rx;
    @Column(name = "txSva")
    public Long txSva;
    @Column(name = "dropSva")
    public Long dropSva;
    @Column(name = "applianceIp")
    private String applianceIp;
    @Column(name = "managerIp")
    private String managerIp;
    @Column(name = "brokerIp")
    private String brokerIp;
    @Column(name = "applianceGateway")
    private String applianceGateway;
    @Column(name = "applianceSubnetMask")
    private String applianceSubnetMask;
    @Column(name = "discovered")
    private Boolean discovered;
    @Column(name = "inspectionReady")
    private Boolean inspectionReady;
    @Column(name = "publicIp")
    private String publicIp;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_fk", nullable = false, foreignKey = @ForeignKey(name = "FK_DEVICE"))
    private DeviceEntity deviceId;
    @Transient
    private DistributedApplianceInstanceElement dai;

    public void updateDeviceMember(DeviceMemberEntity element) {
        this.name = element.name;
        this.applianceGateway = element.applianceGateway;
        this.applianceIp = element.applianceIp;
        this.applianceSubnetMask = element.applianceSubnetMask;
        this.brokerIp = element.brokerIp;
        this.managerIp = element.managerIp;
        this.publicIp = element.publicIp;
        this.rx = element.rx;
        this.txSva = element.txSva;
        this.dropSva = element.dropSva;
        this.version = element.version;
        this.discovered = element.discovered;
        this.inspectionReady = element.inspectionReady;
        this.dai = element.dai;
    }

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

    public DeviceEntity getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(DeviceEntity deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public Long getRx() {
        return this.rx;
    }

    @Override
    public Long getTxSva() {
        return this.txSva;
    }

    @Override
    public Long getDropSva() {
        return this.dropSva;
    }

    @Override
    public String getApplianceIp() {
        return this.applianceIp;
    }

    @Override
    public String getManagerIp() {
        return this.managerIp;
    }

    @Override
    public String getBrokerIp() {
        return this.brokerIp;
    }

    @Override
    public String getApplianceGateway() {
        return this.applianceGateway;
    }

    @Override
    public String getApplianceSubnetMask() {
        return this.applianceSubnetMask;
    }

    @Override
    public Date getCurrentServerTime() {
        return new Date();
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setApplianceIp(String applianceIp) {
        this.applianceIp = applianceIp;
    }

    public void setManagerIp(String managerIp) {
        this.managerIp = managerIp;
    }

    public void setApplianceGateway(String applianceGateway) {
        this.applianceGateway = applianceGateway;
    }

    public void setApplianceSubnetMask(String applianceSubnetMask) {
        this.applianceSubnetMask = applianceSubnetMask;
    }

    public void setRx(Long rx) {
        this.rx = rx;
    }

    public void setTxSva(Long txSva) {
        this.txSva = txSva;
    }

    public void setDropSva(Long dropSva) {
        this.dropSva = dropSva;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    @Override
    public Boolean isDiscovered() {
        return this.discovered;
    }

    @Override
    public Boolean isInspectionReady() {
        return this.inspectionReady;
    }

    public void setDiscovered(Boolean discovered) {
        this.discovered = discovered;
    }

    public void setInspectionReady(Boolean inspectionReady) {
        this.inspectionReady = inspectionReady;
    }

    @Override
    public String getPublicIp() {
        return this.publicIp;
    }

    public void getPublicIp(String ip) {
        this.publicIp = ip;
    }

    public void setBrokerIp(String brokerIp) {
        this.brokerIp = brokerIp;
    }

    @Override
    public String getApplianceName() {
        return this.name;
    }

    public void setDistributedApplianceInstanceElement(DistributedApplianceInstanceElement dai) {
        // TODO - remove DistributedApplianceElement - Sudhir
        this.dai = dai;
    }

    @Override
    public DistributedApplianceInstanceElement getDistributedApplianceInstanceElement() {
        // TODO - remove DistributedApplianceElement - Sudhir
        return this.dai;
    }
}
