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

    private static final Logger LOG = Logger.getLogger(IsmDeviceApi.class);
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
        entity.setVsId(this.vs.getId());
        entity.setName(this.vs.getName());
        return createDevice(entity);
    }

    public String createDevice(DeviceEntity entity) throws Exception {

        return this.txControl.required(new Callable<DeviceEntity>() {

            @Override
            public DeviceEntity call() throws Exception {

                CriteriaBuilder criteriaBuilder = IsmDeviceApi.this.em.getCriteriaBuilder();
                CriteriaQuery<DeviceEntity> query = criteriaBuilder.createQuery(DeviceEntity.class);
                Root<DeviceEntity> r = query.from(DeviceEntity.class);
                query.select(r).where(
                        criteriaBuilder.and(criteriaBuilder.equal(r.get("name"), entity.getName())));

                List<DeviceEntity> devices = IsmDeviceApi.this.em.createQuery(query).getResultList();
                if (!devices.isEmpty()) {
                    String msg = String.format("Device Entity already exists" + "name: %s\n", entity.getName());
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
                    String msg = String.format("Cannot find the Device Entity " + "Id: %s\n", device.getId());
                    LOG.error(msg);
                    throw new IllegalArgumentException(msg);
                }
                result.setName(device.getName());
                IsmDeviceApi.this.em.persist(result);
                return null;
            }
        });
    }

    @Override
    public void deleteVSSDevice() throws Exception {

        this.txControl.required(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                CriteriaBuilder criteriaBuilder = IsmDeviceApi.this.em.getCriteriaBuilder();
                CriteriaQuery<DeviceEntity> query = criteriaBuilder.createQuery(DeviceEntity.class);
                Root<DeviceEntity> r = query.from(DeviceEntity.class);
                query.select(r)
                        .where(criteriaBuilder.and(
                                criteriaBuilder.equal(r.get("vsId"), Long.toString(IsmDeviceApi.this.vs.getId()))));

                List<DeviceEntity> devices = IsmDeviceApi.this.em.createQuery(query).getResultList();
                if (devices.isEmpty()) {
                    LOG.info("Attempt to delete device id:...." + IsmDeviceApi.this.vs.getId());
                    LOG.info("..Device not found, no-op.");
                    return null;
                }
                IsmDeviceApi.this.em.remove(devices.get(0));
                return null;
            }
        });
    }

    @Override
    public String createDeviceMember(final String name, String ipAddress, String vserverIpAddress, String contactIpAddress,
            String gateway, String prefixLength) throws Exception {

        DeviceEntity device = new DeviceEntity();
        DeviceMemberEntity deviceMember = new DeviceMemberEntity();
        deviceMember.setParent(device);
        device.setId(null);
        deviceMember.setName(name);
        return createDeviceMember(deviceMember);
    }

    public String createDeviceMember(DeviceMemberEntity entity) throws Exception {

        return this.txControl.required(new Callable<DeviceMemberEntity>() {

            @Override
            public DeviceMemberEntity call() throws Exception {

                CriteriaBuilder criteriaBuilder = IsmDeviceApi.this.em.getCriteriaBuilder();
                CriteriaQuery<DeviceEntity> query = criteriaBuilder.createQuery(DeviceEntity.class);
                Root<DeviceEntity> r = query.from(DeviceEntity.class);
                if (entity.getParent().getId() != null) {
                    query.select(r)
                            .where(criteriaBuilder.and(criteriaBuilder.equal(r.get("id"), entity.getParent().getId())));
                }
                else {
                    query.select(r).where(
                            criteriaBuilder.and(criteriaBuilder.equal(r.get("vsId"), IsmDeviceApi.this.vs.getId())));
                }

                List<DeviceEntity> devices = IsmDeviceApi.this.em.createQuery(query).getResultList();
                if (devices.isEmpty()) {
                    String msg;
                    if (entity.getParent().getId() != null) {
                        msg = String.format("Cannot find the Device Entity " + "Id: %s\n",entity.getParent().getId());
                    }
                    else {
                        msg = String.format("Cannot find the Device Entity " + "vsId: %s\n",
                                IsmDeviceApi.this.vs.getId());
                    }
                    LOG.error(msg);
                    throw new IllegalArgumentException(msg);
                }
                CriteriaBuilder memberBuilder = IsmDeviceApi.this.em.getCriteriaBuilder();
                CriteriaQuery<DeviceMemberEntity> memberQuery = memberBuilder.createQuery(DeviceMemberEntity.class);
                Root<DeviceMemberEntity> res = memberQuery.from(DeviceMemberEntity.class);
                memberQuery.select(res).where(memberBuilder.and(memberBuilder.equal(r.get("name"), entity.getName())));

                List<DeviceMemberEntity> deviceMemberResult = IsmDeviceApi.this.em.createQuery(memberQuery)
                        .getResultList();
                if (!deviceMemberResult.isEmpty()) {
                    String msg = String.format("Device Member Entity already exists " + "name: %s\n", entity.getName());
                    LOG.error(msg);
                    throw new IllegalArgumentException(msg);
                }
                entity.setParent(devices.get(0));
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
        return updateDeviceMember(deviceMember);
        // TODO - Sudhir Add other information like gateway, ipAddress
    }

    public String updateDeviceMember(DeviceMemberEntity deviceElement) throws Exception {

        return this.txControl.required(new Callable<DeviceMemberEntity>() {

            @Override
            public DeviceMemberEntity call() throws Exception {

                CriteriaBuilder criteriaBuilder = IsmDeviceApi.this.em.getCriteriaBuilder();

                CriteriaQuery<DeviceMemberEntity> query = criteriaBuilder.createQuery(DeviceMemberEntity.class);
                Root<DeviceMemberEntity> r = query.from(DeviceMemberEntity.class);
                query.select(r).where(criteriaBuilder.and(criteriaBuilder.equal(r.get("id"), deviceElement.getId())));

                List<DeviceMemberEntity> result = IsmDeviceApi.this.em.createQuery(query).getResultList();
                if (result.isEmpty()) {
                    String msg = String.format("Cannot find the Device Member Entity " + "Id: %s\n",
                            deviceElement.getId());
                    LOG.error(msg);
                    throw new IllegalArgumentException(msg);
                }
                result.get(0).setName(deviceElement.getName());
                IsmDeviceApi.this.em.persist(result.get(0));
                return result.get(0);
            }
        }).getId();
    }

    @Override
    public void deleteDeviceMember(String id) throws Exception {

        this.txControl.required(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                DeviceMemberEntity memberResult = IsmDeviceApi.this.em.find(DeviceMemberEntity.class,
                        Long.parseLong(id));
                if (memberResult == null) {
                    LOG.info("Attempt to delete device member id:...." + Long.parseLong(id));
                    LOG.info("..device member not found, no-op.");
                    return null;
                }
                IsmDeviceApi.this.em.remove(memberResult);
                return null;
            }
        });
    }

    @Override
    public ManagerDeviceMemberElement getDeviceMemberById(final String id) throws Exception {
        return this.txControl.supports(new Callable<DeviceMemberEntity>() {

            @Override
            public DeviceMemberEntity call() throws Exception {
                CriteriaBuilder criteriaBuilder = IsmDeviceApi.this.em.getCriteriaBuilder();
                CriteriaQuery<DeviceMemberEntity> query = criteriaBuilder.createQuery(DeviceMemberEntity.class);
                Root<DeviceMemberEntity> r = query.from(DeviceMemberEntity.class);
                query.select(r).where(
                        criteriaBuilder.and(criteriaBuilder.equal(r.get("id"), id)));
                List<DeviceMemberEntity> result = IsmDeviceApi.this.em.createQuery(query).getResultList();
                return result.isEmpty() == true ? null : result.get(0);
            }
        });
    }

    @Override
    public ManagerDeviceMemberElement findDeviceMemberByName(final String name) throws Exception {
        return this.txControl.supports(new Callable<DeviceMemberEntity>() {

            @Override
            public DeviceMemberEntity call() throws Exception {
                CriteriaBuilder criteriaBuilder = IsmDeviceApi.this.em.getCriteriaBuilder();

                CriteriaQuery<DeviceMemberEntity> query = criteriaBuilder.createQuery(DeviceMemberEntity.class);
                Root<DeviceMemberEntity> r = query.from(DeviceMemberEntity.class);
                query.select(r).where(criteriaBuilder.and(criteriaBuilder.equal(r.get("name"), name)));
                List<DeviceMemberEntity> result = IsmDeviceApi.this.em.createQuery(query).getResultList();
                return result.isEmpty() == true ? null : result.get(0);
            }
        });
    }

    @Override
    public List<? extends ManagerDeviceMemberElement> listDeviceMembers() throws Exception {

        return listDeviceMembers(null);
    }

    public List<? extends ManagerDeviceMemberElement> listDeviceMembers(Long parentId) throws Exception {

        return this.txControl.supports(new Callable<List<DeviceMemberEntity>>() {

            @Override
            public List<DeviceMemberEntity> call() throws Exception {

                CriteriaBuilder criteriaBuilder = IsmDeviceApi.this.em.getCriteriaBuilder();
                CriteriaQuery<DeviceMemberEntity> query = criteriaBuilder.createQuery(DeviceMemberEntity.class);
                Root<DeviceMemberEntity> r = query.from(DeviceMemberEntity.class);
                if (parentId != null) {
                    query.select(r)
                            .where(criteriaBuilder.and(criteriaBuilder.equal(r.get("parent").get("id"), parentId)));
                } else {
                    query.select(r).where(criteriaBuilder
                            .and(criteriaBuilder.equal(r.get("parent").get("vsId"), IsmDeviceApi.this.vs.getId())));
                }
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
}
