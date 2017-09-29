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
import org.osc.manager.ism.entities.DeviceEntity;
import org.osc.manager.ism.entities.DeviceMemberEntity;
import org.osc.sdk.manager.api.ManagerDeviceApi;
import org.osc.sdk.manager.element.ApplianceBootstrapInformationElement;
import org.osc.sdk.manager.element.BootStrapInfoProviderElement;
import org.osc.sdk.manager.element.DistributedApplianceInstanceElement;
import org.osc.sdk.manager.element.ManagerDeviceElement;
import org.osc.sdk.manager.element.ManagerDeviceMemberElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.osgi.service.transaction.control.TransactionControl;

public class IsmDeviceApi implements ManagerDeviceApi {

    private static Logger LOG = Logger.getLogger(IsmDeviceApi.class);
    private VirtualSystemElement vs;
    private TransactionControl txControl;
    private EntityManager em;

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
        return this.txControl.supports(new Callable<DeviceEntity>() {
            @Override
            public DeviceEntity call() throws Exception {
                return IsmDeviceApi.this.em.find(DeviceEntity.class, Long.valueOf(id));
            }
        });
    }

    @Override
    public String findDeviceByName(final String name) throws Exception {
        return this.txControl.supports(new Callable<String>() {
            @Override
            public String call() throws Exception {
                CriteriaBuilder criteriaBuilder = IsmDeviceApi.this.em.getCriteriaBuilder();
                CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
                Root<DeviceEntity> r = query.from(DeviceEntity.class);
                query.select(r.get("id").as(Long.class)).where(criteriaBuilder.equal(r.get("name"), name));

                List<Long> result = IsmDeviceApi.this.em.createQuery(query).getResultList();

                return result.isEmpty() == true ? null : result.get(0).toString();
            }
        });
    }

    @Override
    public List<? extends ManagerDeviceElement> listDevices() throws Exception {
        return this.txControl.supports(new Callable<List<DeviceEntity>>() {
            @Override
            public List<DeviceEntity> call() throws Exception {
                CriteriaBuilder criteriaBuilder = IsmDeviceApi.this.em.getCriteriaBuilder();
                CriteriaQuery<DeviceEntity> query = criteriaBuilder.createQuery(DeviceEntity.class);
                Root<DeviceEntity> r = query.from(DeviceEntity.class);
                query.select(r);

                return IsmDeviceApi.this.em.createQuery(query).getResultList();
            }
        });
    }

    @Override
    public String createVSSDevice() throws Exception {
        DeviceEntity entity = new DeviceEntity();
        entity.setName(this.vs.getName());

        return createDevice(entity);
    }

    public String createDevice(DeviceEntity entity) throws Exception {
        return this.txControl.required(new Callable<DeviceEntity>() {

            @Override
            public DeviceEntity call() throws Exception {
                String device = findDeviceByName(entity.getName());

                if (device != null) {
                    String msg = String.format("Device already exists name: %s\n", entity.getName());
                    LOG.error(msg);
                    throw new IllegalArgumentException(msg);
                }
                IsmDeviceApi.this.em.persist(entity);
                return entity;
            }
        }).getId();
    }

    @Override
    public void updateVSSDevice(ManagerDeviceElement device) throws Exception {
        this.txControl.required(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                DeviceEntity result = IsmDeviceApi.this.em.find(DeviceEntity.class, Long.parseLong(device.getId()));

                if (result == null) {
                    String msg = String.format("Cannot find the device id: %s\n", device.getId());
                    LOG.error(msg);
                    throw new IllegalArgumentException(msg);
                }

                result.setName(device.getName());
                IsmDeviceApi.this.em.merge(result);

                return null;
            }
        });
    }

    @Override
    public void deleteVSSDevice() throws Exception {
        this.txControl.required(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                DeviceEntity device = IsmDeviceApi.this.em.find(DeviceEntity.class,
                        Long.parseLong(IsmDeviceApi.this.vs.getMgrId()));

                if (device == null) {
                    LOG.info(String.format("Attempt to delete device for VS mgrid %s and device not found, no-op.",
                            "" + IsmDeviceApi.this.vs.getMgrId()));
                    return null;
                }

                IsmDeviceApi.this.em.remove(device);

                return null;
            }
        });
    }

    @Override
    public String createDeviceMember(final String name, String ipAddress, String vserverIpAddress, String contactIpAddress,
            String gateway, String prefixLength) throws Exception {
        DeviceMemberEntity deviceMember = new DeviceMemberEntity();
        updateMember(deviceMember, vserverIpAddress, contactIpAddress, ipAddress, gateway, prefixLength);
        deviceMember.setName(name);
        return createDeviceMember(deviceMember, Long.parseLong(this.vs.getMgrId()));
    }

    public String createDeviceMember(DeviceMemberEntity entity, Long deviceId) throws Exception {
        return this.txControl.required(new Callable<DeviceMemberEntity>() {
            @Override
            public DeviceMemberEntity call() throws Exception {
                DeviceEntity device;

                device = IsmDeviceApi.this.em.find(DeviceEntity.class, deviceId);

                if (device == null) {
                    String msg = String.format("Cannot find the device for id: %s", deviceId);
                    LOG.error(msg);
                    throw new IllegalArgumentException(msg);
                }

                CriteriaBuilder memberBuilder = IsmDeviceApi.this.em.getCriteriaBuilder();
                CriteriaQuery<DeviceMemberEntity> memberQuery = memberBuilder.createQuery(DeviceMemberEntity.class);
                Root<DeviceMemberEntity> res = memberQuery.from(DeviceMemberEntity.class);
                memberQuery.select(res)
                .where(memberBuilder.and((memberBuilder.equal(res.get("name"), entity.getName()))));
                List<DeviceMemberEntity> deviceMemberResult = IsmDeviceApi.this.em.createQuery(memberQuery)
                        .getResultList();

                if (!deviceMemberResult.isEmpty()) {
                    String msg = String.format("Device member already exists name: %s\n", entity.getName());
                    LOG.error(msg);
                    throw new IllegalArgumentException(msg);
                }
                entity.setDevice(device);
                IsmDeviceApi.this.em.persist(entity);

                return entity;
            }
        }).getId();
    }

    @Override
    public String updateDeviceMember(ManagerDeviceMemberElement deviceElement, String name, String vserverIpAddress,
            String contactIpAddress, String ipAddress, String gateway, String prefixLength) throws Exception {
        DeviceMemberEntity deviceMember = new DeviceMemberEntity();
        deviceMember.setId(Long.parseLong(deviceElement.getId()));
        deviceMember.setName(name);
        updateMember(deviceMember, vserverIpAddress, contactIpAddress, ipAddress, gateway, prefixLength);
        return updateDeviceMember(deviceMember, Long.parseLong(this.vs.getMgrId()));
    }

    public String updateDeviceMember(DeviceMemberEntity deviceElement, Long deviceId) throws Exception {
        return this.txControl.required(new Callable<DeviceMemberEntity>() {
            @Override
            public DeviceMemberEntity call() throws Exception {
                DeviceMemberEntity result = getDeviceMember(deviceId, Long.parseLong(deviceElement.getId()), null);

                if (result == null) {
                    String msg = String.format("Cannot find the device member id: %s\n",
                            deviceElement.getId());
                    LOG.error(msg);
                    throw new IllegalArgumentException(msg);
                }

                result.updateDeviceMember(deviceElement);
                IsmDeviceApi.this.em.merge(result);

                return result;
            }
        }).getId();
    }

    @Override
    public void deleteDeviceMember(String id) throws Exception {
        deleteDeviceMember(Long.parseLong(this.vs.getMgrId()), Long.parseLong(id));
    }

    public void deleteDeviceMember(Long deviceId, Long memberId) throws Exception {
        this.txControl.required(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                DeviceMemberEntity memberResult = getDeviceMember(deviceId, memberId, null);

                if (memberResult == null) {
                    LOG.info(String.format("Attempt to delete device member for id %s and device not found, no-op.",
                            "" + Long.toString(memberId)));
                    return null;
                }

                IsmDeviceApi.this.em.remove(memberResult);

                return null;
            }
        });
    }

    @Override
    public ManagerDeviceMemberElement getDeviceMemberById(final String id) throws Exception {
        return getDeviceMemberById(Long.parseLong(this.vs.getMgrId()), Long.parseLong(id));
    }

    public DeviceMemberEntity getDeviceMemberById(Long deviceId, Long memberId) throws Exception {
        return this.txControl.supports(new Callable<DeviceMemberEntity>() {
            @Override
            public DeviceMemberEntity call() throws Exception {
                DeviceMemberEntity result = getDeviceMember(deviceId, memberId, null);

                return result;
            }
        });
    }

    @Override
    public ManagerDeviceMemberElement findDeviceMemberByName(final String name) throws Exception {
        return this.txControl.supports(new Callable<DeviceMemberEntity>() {
            @Override
            public DeviceMemberEntity call() throws Exception {
                DeviceMemberEntity result = getDeviceMember(Long.parseLong(IsmDeviceApi.this.vs.getMgrId()), null,
                        name);

                return result;
            }
        });
    }

    @Override
    public List<? extends ManagerDeviceMemberElement> listDeviceMembers() throws Exception {
        return listDeviceMembers(Long.parseLong(this.vs.getMgrId()));
    }

    public List<? extends ManagerDeviceMemberElement> listDeviceMembers(Long deviceId) throws Exception {
        return this.txControl.supports(new Callable<List<DeviceMemberEntity>>() {
            @Override
            public List<DeviceMemberEntity> call() throws Exception {
                CriteriaBuilder criteriaBuilder = IsmDeviceApi.this.em.getCriteriaBuilder();
                CriteriaQuery<DeviceMemberEntity> query = criteriaBuilder.createQuery(DeviceMemberEntity.class);
                Root<DeviceMemberEntity> r = query.from(DeviceMemberEntity.class);
                query.select(r)
                .where(criteriaBuilder.and(criteriaBuilder.equal(r.get("device").get("id"), deviceId)));

                return IsmDeviceApi.this.em.createQuery(query).getResultList();
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

    private DeviceMemberEntity getDeviceMember(Long deviceId, Long memberId, String memName) {
        CriteriaBuilder criteriaBuilder = IsmDeviceApi.this.em.getCriteriaBuilder();
        CriteriaQuery<DeviceMemberEntity> query = criteriaBuilder.createQuery(DeviceMemberEntity.class);
        Root<DeviceMemberEntity> r = query.from(DeviceMemberEntity.class);

        if (memName != null) {
            query.select(r)
            .where(criteriaBuilder.and((criteriaBuilder.equal(r.get("device").get("id"), deviceId)),
                    (criteriaBuilder.equal(r.get("name"), memName))));
        } else {
            query.select(r).where(criteriaBuilder.and((criteriaBuilder.equal(r.get("device").get("id"), deviceId)),
                    criteriaBuilder.equal(r.get("id"), memberId)));
        }

        List<DeviceMemberEntity> result = IsmDeviceApi.this.em.createQuery(query).getResultList();

        return result.isEmpty() == true ? null : result.get(0);
    }

    private void updateMember(DeviceMemberEntity deviceMember, String vserverIpAddress, String contactIpAddress,
            String ipAddress, String gateway, String prefixLength) {
        deviceMember.setPublicIp(ipAddress);
        deviceMember.setManagerIp(vserverIpAddress);
        deviceMember.setApplianceIp(contactIpAddress);
        deviceMember.setApplianceGateway(gateway);
        deviceMember.setApplianceSubnetMask(prefixLength);
    }
}
