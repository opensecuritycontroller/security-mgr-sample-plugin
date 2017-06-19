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
package org.osc.manager.ism.model;

import java.util.Date;

import org.osc.sdk.manager.element.DistributedApplianceInstanceElement;
import org.osc.sdk.manager.element.ManagerDeviceMemberStatusElement;

public class IsmAgent implements ManagerDeviceMemberStatusElement {

    private String version;
    public Long rx;             // Received packets to security function appliance
    public Long txSva;          // Packet sent out as instructed by security function appliance
    public Long dropSva;        // Dropped as instructed by security function appliance
    private String applianceIp;
    private String applianceName;
    private String managerIp;
    private String brokerIp;
    private String applianceGateway;
    private String applianceSubnetMask;
    private Boolean isDiscovered;
    private Boolean isInspectionReady;
    private String publicIp;
    private DistributedApplianceInstanceElement distributedApplianceInstanceElement;

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
    public String getApplianceName() {
        return this.applianceName;
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

    public void setApplianceName(String applianceName) {
        this.applianceName = applianceName;
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
        return this.isDiscovered;
    }

    @Override
    public Boolean isInspectionReady() {
        return this.isInspectionReady;
    }

    public void setDiscovered(Boolean isDiscovered) {
        this.isDiscovered = isDiscovered;
    }

    public void setInspectionReady(Boolean isInspectionReady) {
        this.isInspectionReady = isInspectionReady;
    }

    @Override
    public String getPublicIp() {
        return this.publicIp;
    }

    public void getPublicIp(String ip ) {
        this.publicIp = ip;
    }

    public void setBrokerIp(String brokerIp) {
        this.brokerIp = brokerIp;
    }

    @Override
    public DistributedApplianceInstanceElement getDistributedApplianceInstanceElement() {
        return this.distributedApplianceInstanceElement;
    }

    public void setDistributedApplianceInstanceElement(DistributedApplianceInstanceElement daiEl) {
        this.distributedApplianceInstanceElement = daiEl;
    }


}
