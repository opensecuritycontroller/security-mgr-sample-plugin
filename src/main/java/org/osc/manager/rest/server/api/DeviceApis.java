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
package org.osc.manager.rest.server.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.osc.manager.ism.api.IsmDeviceApi;
import org.osc.manager.ism.entities.DeviceEntity;
import org.osc.manager.ism.entities.DeviceMemberEntity;
import org.osc.manager.rest.server.SecurityManagerServerRestConstants;
import org.osc.sdk.manager.element.ApplianceSoftwareVersionElement;
import org.osc.sdk.manager.element.DistributedApplianceElement;
import org.osc.sdk.manager.element.DomainElement;
import org.osc.sdk.manager.element.ManagerDeviceElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.osc.sdk.manager.element.VirtualizationConnectorElement;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.transaction.control.TransactionControl;

@Component(service = DeviceApis.class)
@Path(SecurityManagerServerRestConstants.SERVER_API_PATH_PREFIX + "/devices")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class DeviceApis {

    private static final Logger logger = Logger.getLogger(DeviceApis.class);
    private EntityManager em;
    private TransactionControl txControl;
    private IsmDeviceApi api;

    public static final class VSElement implements VirtualSystemElement {

        private final Long id;

        private final String name;

        public VSElement(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public VirtualizationConnectorElement getVirtualizationConnector() {
            return null;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getMgrId() {
            return null;
        }

        @Override
        public byte[] getKeyStore() {
            return null;
        }

        @Override
        public Long getId() {
            return this.id;
        }

        @Override
        public DomainElement getDomain() {
            return null;
        }

        @Override
        public DistributedApplianceElement getDistributedAppliance() {
            return null;
        }

        @Override
        public ApplianceSoftwareVersionElement getApplianceSoftwareVersion() {
            return null;
        }
    }

    /**
     * init the DB
     *
     *
     */
    public void init(EntityManager em, TransactionControl txControl) throws Exception {
        this.em = em;
        this.txControl = txControl;
    }

    /**
     * Creates the Device for a given VS
     *
     * @return Device
     * @throws Exception
     */
    @POST
    public String createDevice(DeviceEntity entity) throws Exception {

        logger.info("Creating Device Entity with Name:...." + entity.getName());
        logger.info("and with VS ID:...." + entity.getVsId());

        VSElement vsElement = new VSElement(entity.getVsId(), entity.getName());
        this.api = new IsmDeviceApi(vsElement, this.txControl, this.em);
        return this.api.createVSSDevice();
    }

    /**
     * Updates the Device for a given device Id
     *
     * @return - updated Device
     * @throws Exception
     */
    @Path("/{deviceId}")
    @PUT
    public DeviceEntity updateDevice(@PathParam("deviceId") Long deviceId, DeviceEntity entity) throws Exception {

        logger.info("Updating Device Entity ID...:" + deviceId);

        entity.setId(deviceId);
        this.api = new IsmDeviceApi(null, this.txControl, this.em);
        this.api.updateVSSDevice(entity);
        return entity;
    }

    /**
     * Deletes the Device for a given device Id
     *
     * @return - deleted Device
     * @throws Exception
     */
    @Path("/{deviceId}")
    @DELETE
    public void deleteDevice(@PathParam("deviceId") Long deviceId) throws Exception {

        logger.info("Deleting Device Entity ID...:" + deviceId);
        this.txControl.required(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                DeviceEntity result = DeviceApis.this.em.find(DeviceEntity.class, deviceId);
                if (result == null) {
                    return null;
                }

                DeviceApis.this.em.remove(result);
                return null;
            }
        });
    }

    /**
     * Lists the Device Id's
     *
     * @return - Device Id's
     * @throws Exception
     */
    @GET
    public List<String> getDeviceIds() throws Exception {

        logger.info("Listing Device Ids'");

        this.api = new IsmDeviceApi(null, this.txControl, this.em);
        List<? extends ManagerDeviceElement> result = this.api.listDevices();
        List<String> deviceList = new ArrayList<String>();
        if (result.isEmpty()) {
            return deviceList;
        }

        for (ManagerDeviceElement device : result) {
            deviceList.add(new String(device.getId()));
        }
        return deviceList;
    }

    /**
     * Gets the Device for a given device Id
     *
     * @return - Device
     *
     */
    @Path("/{deviceId}")
    @GET
    public DeviceEntity getDevice(@PathParam("deviceId") Long deviceId) throws Exception {

        logger.info("getting Device for ID...:" + deviceId);
        this.api = new IsmDeviceApi(null, this.txControl, this.em);
        return (DeviceEntity) this.api.getDeviceById(Long.toString(deviceId));
    }

    /**
     * Creates the Device Member for a given Device
     *
     * @return device member id
     * @throws Exception
     */
    @Path("/{deviceId}/members")
    @POST
    public String createDeviceMember(@PathParam("deviceId") Long deviceId, DeviceMemberEntity entity) throws Exception {

        logger.info("Creating Device Members...:" + entity.getName());

        return this.txControl.required(new Callable<DeviceMemberEntity>() {

            @Override
            public DeviceMemberEntity call() throws Exception {

                DeviceEntity result = DeviceApis.this.em.find(DeviceEntity.class, deviceId);
                if (result == null) {
                    throw new Exception("Device Entity does not exists...");
                    //TODO - to add RETURN 404 error:Sudhir
                }
                CriteriaBuilder criteriaBuilder = DeviceApis.this.em.getCriteriaBuilder();
                CriteriaQuery<DeviceMemberEntity> query = criteriaBuilder.createQuery(DeviceMemberEntity.class);
                Root<DeviceMemberEntity> r = query.from(DeviceMemberEntity.class);
                query.select(r).where(criteriaBuilder.and(criteriaBuilder.equal(r.get("name"), entity.getName())));

                List<DeviceMemberEntity> deviceMemberResult = DeviceApis.this.em.createQuery(query).getResultList();
                if (!deviceMemberResult.isEmpty()) {
                    throw new Exception("Device Member Entity name already exists...:");
                    //TODO - to add RETURN 404 error:Sudhir
                }
                entity.setParent(result);
                DeviceApis.this.em.persist(entity);
                return entity;
            }
        }).getId();
    }

    /**
     * Updates the Device Member for a given Device Id and the Device Member Id
     *
     * @return - updated Device Member
     */
    @Path("/{deviceId}/members/{memberId}")
    @PUT
    public DeviceMemberEntity updateDeviceMember(@PathParam("deviceId") Long deviceId,
            @PathParam("memberId") Long memberId, DeviceMemberEntity entity) {

        logger.info("Updating for Device Member with Device Member Id...:" + memberId);
        logger.info("and Device Id...:" + deviceId);

        return this.txControl.required(new Callable<DeviceMemberEntity>() {

            @Override
            public DeviceMemberEntity call() throws Exception {

                CriteriaBuilder criteriaBuilder = DeviceApis.this.em.getCriteriaBuilder();

                CriteriaQuery<DeviceMemberEntity> query = criteriaBuilder.createQuery(DeviceMemberEntity.class);
                Root<DeviceMemberEntity> r = query.from(DeviceMemberEntity.class);
                query.select(r).where(criteriaBuilder.and(criteriaBuilder.equal(r.get("parent").get("id"), deviceId)),
                        criteriaBuilder.equal(r.get("id"), memberId));

                List<DeviceMemberEntity> result = DeviceApis.this.em.createQuery(query).getResultList();
                if (result.isEmpty()) {
                    throw new Exception("Device or the Device Member Entity does not exists...");
                    //TODO - to add RETURN 404 error:Sudhir
                }
                result.get(0).setName(entity.getName());
                DeviceApis.this.em.persist(result.get(0));
                return result.get(0);
            }
        });
    }

    /**
     * Deletes the Device Member for a given device Id and the member Id
     *
     *
     */
    @Path("/{deviceId}/members/{memberId}")
    @DELETE
    public void deleteDeviceMember(@PathParam("deviceId") Long deviceId, @PathParam("memberId") Long memberId,
            DeviceMemberEntity entity) {

        logger.info("Deleting the Device Member with device member Id...:" + memberId);

        this.txControl.required(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                DeviceEntity result = DeviceApis.this.em.find(DeviceEntity.class, deviceId);
                if (result == null) {
                    return null;
                }
                DeviceMemberEntity memberResult = DeviceApis.this.em.find(DeviceMemberEntity.class, memberId);
                if (memberResult == null) {
                    return null;
                }
                DeviceApis.this.em.remove(memberResult);
                return null;
            }
        });
    }

    /**
     * Lists the Device Member Id's for a given Device Id
     *
     * @return - Device Member Id's
     * @throws Exception
     */
    @Path("/{deviceId}/members")
    @GET
    public List<String> getDeviceMembersIds(@PathParam("deviceId") Long deviceId) throws Exception {

        logger.info("Listing Device Members Ids'for device Id ...:" + deviceId);

        return this.txControl.supports(new Callable<List<String>>() {

            @Override
            public List<String> call() throws Exception {

                CriteriaBuilder criteriaBuilder = DeviceApis.this.em.getCriteriaBuilder();
                CriteriaQuery<String> query = criteriaBuilder.createQuery(String.class);
                Root<DeviceMemberEntity> r = query.from(DeviceMemberEntity.class);
                query.select(r.get("id").as(String.class)).where(criteriaBuilder
                        .and(criteriaBuilder.equal(r.get("parent").get("id"), deviceId)));
                return DeviceApis.this.em.createQuery(query).getResultList();
            }
        });
    }

    /**
     * Gets the Device Member for a given device id and the member id
     *
     * @return - device member
     *
     */
    @Path("/{deviceId}/members/{memberId}")
    @GET
    public DeviceMemberEntity getDeviceMember(@PathParam("deviceId") Long deviceId,
            @PathParam("memberId") Long memberId) throws Exception {

        logger.info("getting Device Member for Member ID..:" + memberId);

        return this.txControl.supports(new Callable<DeviceMemberEntity>() {

            @Override
            public DeviceMemberEntity call() throws Exception {
                CriteriaBuilder criteriaBuilder = DeviceApis.this.em.getCriteriaBuilder();
                CriteriaQuery<DeviceMemberEntity> query = criteriaBuilder.createQuery(DeviceMemberEntity.class);
                Root<DeviceMemberEntity> r = query.from(DeviceMemberEntity.class);
                query.select(r).where(criteriaBuilder.and(criteriaBuilder.equal(r.get("parent").get("id"), deviceId),
                        criteriaBuilder.equal(r.get("id"), memberId)));
                List<DeviceMemberEntity> result = DeviceApis.this.em.createQuery(query).getResultList();
                if (result.isEmpty()) {
                    throw new Exception("Device or Device Member does not exists...");
                    //TODO - Add 404 error response - Sudhir
                }
                return result.get(0);
            }
        });
    }

}
