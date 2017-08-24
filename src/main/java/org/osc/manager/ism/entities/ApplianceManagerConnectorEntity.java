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

import static javax.persistence.FetchType.LAZY;

import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.ManagerTypeElement;

@Entity
public class ApplianceManagerConnectorEntity implements ApplianceManagerConnectorElement {

    @GeneratedValue
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "name", unique = true, nullable = false)
    private String name;
    @Transient
    private String ipAddress;
    @Transient
    private String username;
    @Transient
    private String password;
    @Transient
    @Column(name = "api_key")
    private String ApiKey;
    @Transient
    private String lastKnownBrokerIpAddress;
    @Transient
    private byte[] publicKey = null;
    @Transient
    private String clientIpAddress;
    @Transient
    private String serviceType;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = LAZY, mappedBy = "parent")
    private List<PolicyEntity> policies;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = LAZY, mappedBy = "applianceManagerConnector")
    private List<DomainEntity> domains;

    public Long getid() {
        return this.id;
    }

    public void setid(Long id) {
        this.id = id;
    }

    public List<PolicyEntity> getPolicies() {
        return this.policies;
    }

    public void setPolicies(List<PolicyEntity> policies) {
        this.policies = policies;
    }

    public List<DomainEntity> getDomains() {
        return this.domains;
    }

    public void setDomains(List<DomainEntity> domains) {
        this.domains = domains;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public ManagerTypeElement getManagerType() {
        return null;
    }

    public String getServiceType() {
        return this.serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public byte[] getPublicKey() {
        return this.publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String getApiKey() {
        return this.ApiKey;
    }

    public void setApiKey(String apiKey) {
        this.ApiKey = apiKey;
    }

    @Override
    public SSLContext getSslContext() {
        return null;
    }

    @Override
    public String getLastKnownNotificationIpAddress() {
        return this.lastKnownBrokerIpAddress;
    }

    public void setLastKnownNotificationIpAddress(String lastKnownNotificationIpAddress) {
        this.lastKnownBrokerIpAddress = lastKnownNotificationIpAddress;
    }

    @Override
    public TrustManager[] getTruststoreManager() throws Exception {
        return null;
    }

    @Override
    public String getClientIpAddress() {
        return this.clientIpAddress;
    }

    public void setClientIpAddress(String clientIpAddress) {
        this.clientIpAddress = clientIpAddress;
    }
}
