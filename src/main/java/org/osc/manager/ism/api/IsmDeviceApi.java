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
package org.osc.manager.ism.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.osc.manager.ism.entities.DeviceMember;
import org.osc.manager.ism.entities.VSSDevice;
import org.osc.manager.ism.model.Device;
import org.osc.manager.ism.model.MemberDevice;
import org.osc.sdk.manager.api.ManagerDeviceApi;
import org.osc.sdk.manager.element.ApplianceBootstrapInformationElement;
import org.osc.sdk.manager.element.BootStrapInfoProviderElement;
import org.osc.sdk.manager.element.DistributedApplianceInstanceElement;
import org.osc.sdk.manager.element.ManagerDeviceElement;
import org.osc.sdk.manager.element.ManagerDeviceMemberElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.osgi.service.transaction.control.TransactionControl;

public class IsmDeviceApi implements ManagerDeviceApi {

    Logger log = Logger.getLogger(IsmDeviceApi.class);

    private final VirtualSystemElement vs;
    
    private final TransactionControl txControl;
    
    private final EntityManager em;

    public IsmDeviceApi(VirtualSystemElement vs, TransactionControl txControl, EntityManager em) {
        this.vs = vs;
        this.txControl = txControl;
        this.em = em;
    }

    @Override
    public boolean isDeviceGroupSupported() {
        return true;
    }

    @Override
    public ManagerDeviceElement getDeviceById(String id) throws Exception {
        return new Device(this.vs.getId(), this.vs.getName());
    }

    @Override
    public String findDeviceByName(final String name) throws Exception {
        
        return txControl.supports(new Callable<String>() {
            
            @Override
            public String call() throws Exception {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                
                CriteriaQuery<Long> q = cb.createQuery(Long.class);
                Root<VSSDevice> r = q.from(VSSDevice.class);
                q.select(r.get("id").as(Long.class)).where(cb.equal(r.get("name"), name));
                
                Long result = em.createQuery(q).getSingleResult();
                return result == null ? null : result.toString();
            }
        });
    }

    @Override
    public List<? extends ManagerDeviceElement> listDevices() throws Exception {
        
        return txControl.supports(new Callable<List<? extends ManagerDeviceElement>>() {
            
            @Override
            public List<? extends ManagerDeviceElement> call() throws Exception {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                
                CriteriaQuery<VSSDevice> q = cb.createQuery(VSSDevice.class);
                Root<VSSDevice> r = q.from(VSSDevice.class);
                q.select(r);
                
                List<Device> devices = new ArrayList<Device>();
                for (VSSDevice dev : em.createQuery(q).getResultList()) {
                    devices.add(new Device(dev.getId(), dev.getName()));
                }
                return devices;
            }
        });
    }

    @Override
    public String createVSSDevice() throws Exception {
        return txControl.required(new Callable<VSSDevice>() {
            @Override
            public VSSDevice call() throws Exception {
                VSSDevice device = new VSSDevice();
                device.setId(vs.getId());
                device.setName(vs.getName());
                em.persist(device);
                return device;
            }
        }).getId().toString();
    }

    @Override
    public void updateVSSDevice(ManagerDeviceElement device) throws Exception {
    }

    @Override
    public void deleteVSSDevice() throws Exception {
        txControl.required(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                em.remove(em.find(VSSDevice.class, vs.getId()));
                return null;
            }
        });
    }

    @Override
    public String createDeviceMember(final String name, String ipAddress, String vserverIpAddress, String contactIpAddress,
            String gateway, String prefixLength) throws Exception {
        return txControl.required(new Callable<DeviceMember>() {
            @Override
            public DeviceMember call() throws Exception {
                VSSDevice device = em.find(VSSDevice.class, vs.getId());
                
                DeviceMember dm = new DeviceMember();
                dm.setParent(device);
                dm.setName(name);
                em.persist(dm);
                return dm;
            }
        }).getId().toString();
    }


    @Override
    public String updateDeviceMember(ManagerDeviceMemberElement deviceElement, String name, String vserverIpAddress,
            String contactIpAddress, String ipAddress, String gateway, String prefixLength) throws Exception {
        return deviceElement.getId();
    }

    @Override
    public void deleteDeviceMember(String id) throws Exception {
    }

    @Override
    public ManagerDeviceMemberElement getDeviceMemberById(final String id) throws Exception {
        return txControl.supports(new Callable<ManagerDeviceMemberElement>() {
            
            @Override
            public ManagerDeviceMemberElement call() throws Exception {
                DeviceMember result = em.find(DeviceMember.class, Long.valueOf(id));
                return result == null ? null : new MemberDevice(result.getId(), result.getName());
            }
        });
    }

    @Override
    public ManagerDeviceMemberElement findDeviceMemberByName(final String name) throws Exception {
        return txControl.supports(new Callable<ManagerDeviceMemberElement>() {
            
            @Override
            public ManagerDeviceMemberElement call() throws Exception {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                
                CriteriaQuery<DeviceMember> q = cb.createQuery(DeviceMember.class);
                Root<DeviceMember> r = q.from(DeviceMember.class);
                q.select(r).where(cb.and(cb.equal(r.get("name"), name), cb.equal(
                        r.get("parent").get("id"), vs.getId())));
                
                DeviceMember result = em.createQuery(q).getSingleResult();
                return result == null ? null : new MemberDevice(result.getId(), result.getName());
            }
        });
    }

    @Override
    public List<? extends ManagerDeviceMemberElement> listDeviceMembers() throws Exception {

        return txControl.supports(new Callable<List<? extends ManagerDeviceMemberElement>>() {
            
            @Override
            public List<? extends ManagerDeviceMemberElement> call() throws Exception {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                
                CriteriaQuery<DeviceMember> q = cb.createQuery(DeviceMember.class);
                Root<DeviceMember> r = q.from(DeviceMember.class);
                q.select(r).where(cb.equal(r.get("parent").get("id"), vs.getId()));
                
                List<MemberDevice> devices = new ArrayList<MemberDevice>();
                for (DeviceMember dev : em.createQuery(q).getResultList()) {
                    devices.add(new MemberDevice(dev.getId(), dev.getName()));
                }
                return devices;
            }
        });
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isUpgradeSupported(String modelType, String srcSwVersion, String destSwVersion) throws Exception {
        return true;
    }

    @Override
    public byte[] getDeviceMemberConfigById(String mgrDeviceId) throws Exception {
        return null;
    }

    @Override
    public byte[] getDeviceMemberConfiguration(DistributedApplianceInstanceElement dai) {
        return null;
    }

    @Override
    public byte[] getDeviceMemberAdditionalConfiguration(DistributedApplianceInstanceElement dai) {
        return null;
    }

    @Override
    public ApplianceBootstrapInformationElement getBootstrapinfo(BootStrapInfoProviderElement bootStrapInfo) {
        return new ApplianceBootstrapInformationElement(){
            @Override
            public List<BootstrapFileElement> getBootstrapFiles() {
                return new ArrayList<BootstrapFileElement>();
            }
        };
    }
}
