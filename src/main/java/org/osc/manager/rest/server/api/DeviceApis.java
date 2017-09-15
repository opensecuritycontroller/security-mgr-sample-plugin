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
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
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
import org.osc.sdk.manager.element.ManagerDeviceElement;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.transaction.control.TransactionControl;

@Component(service = DeviceApis.class)
@Path(SecurityManagerServerRestConstants.SERVER_API_PATH_PREFIX + "/devices")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class DeviceApis {

    private static final Logger LOG = Logger.getLogger(DeviceApis.class);
    private EntityManager em;
    private TransactionControl txControl;
    private IsmDeviceApi api;

    /**
     * init the DB
     *
     *
     */
    public void init(EntityManager em, TransactionControl txControl) throws Exception {
        this.em = em;
        this.txControl = txControl;
        this.api = new IsmDeviceApi(null, this.txControl, this.em);
    }

    /**
     * Creates the Device for a given VS
     *
     * @return Device
     * @throws Exception
     */
    @POST
    public String createDevice(DeviceEntity entity) throws Exception {

        LOG.info("Creating Device Entity with Name:...." + entity.getName());
        LOG.info("and with VS ID:...." + entity.getVsId());
        return this.api.createDevice(entity);
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

        LOG.info("Updating Device Entity ID...:" + deviceId);
        entity.setId(deviceId);
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

        LOG.info("Deleting Device Entity ID...:" + deviceId);
        this.txControl.required(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                DeviceEntity result = DeviceApis.this.em.find(DeviceEntity.class, deviceId);
                if (result == null) {
                    LOG.info("Attempt to delete device id:...." + deviceId);
                    LOG.info("..Device not found, no-op.");
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

        LOG.info("Listing Device Ids'");

        List<? extends ManagerDeviceElement> devices = this.api.listDevices();
        List<String> deviceList = new ArrayList<String>();
        if (devices.isEmpty()) {
            return deviceList;
        }
        deviceList = devices.stream().map(ManagerDeviceElement::getId).collect(Collectors.toList());
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

        LOG.info("getting Device for ID...:" + deviceId);
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

        LOG.info("Creating Device Members...:" + entity.getName());
        DeviceEntity device = new DeviceEntity();
        entity.setParent(device);
        device.setId(deviceId);
        return this.api.createDeviceMember(entity);
    }

    /**
     * Updates the Device Member for a given Device Id and the Device Member Id
     *
     * @return - updated Device Member
     * @throws Exception
     */
    @Path("/{deviceId}/members/{memberId}")
    @PUT
    public DeviceMemberEntity updateDeviceMember(@PathParam("deviceId") Long deviceId,
            @PathParam("memberId") Long memberId, DeviceMemberEntity entity) throws Exception {

        LOG.info("Updating for Device Member with Device Member Id...:" + memberId);
        LOG.info("and Device Id...:" + deviceId);
        entity.setId(memberId);
        this.api.updateDeviceMember(entity);
        return entity;
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

        LOG.info("Deleting the Device Member with device member Id...:" + memberId);

        this.txControl.required(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                DeviceEntity result = DeviceApis.this.em.find(DeviceEntity.class, deviceId);
                if (result == null) {
                    LOG.info("Attempt to delete device member, device id:...." + deviceId);
                    LOG.info("..device not found, no-op.");
                    return null;
                }
                DeviceMemberEntity memberResult = DeviceApis.this.em.find(DeviceMemberEntity.class, memberId);
                if (memberResult == null) {
                    LOG.info("Attempt to delete device member id:...." + memberId);
                    LOG.info("..device member not found, no-op.");
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
    @SuppressWarnings("unchecked")
    @Path("/{deviceId}/members")
    @GET
    public List<String> getDeviceMembersIds(@PathParam("deviceId") Long deviceId) throws Exception {

        LOG.info("Listing Device Members Ids'for device Id ...:" + deviceId);
        List<DeviceMemberEntity> deviceMembers = new ArrayList<DeviceMemberEntity>();
        List<String> members = new ArrayList<String>();
        deviceMembers = (List<DeviceMemberEntity>) this.api.listDeviceMembers(deviceId);
        if (!deviceMembers.isEmpty()) {
            members = deviceMembers.stream().map(DeviceMemberEntity::getId).collect(Collectors.toList());
        }
        return members;
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

        LOG.info("getting Device Member for Member ID..:" + memberId);
        return (DeviceMemberEntity) this.api.getDeviceMemberById(Long.toString(memberId));
    }
}
