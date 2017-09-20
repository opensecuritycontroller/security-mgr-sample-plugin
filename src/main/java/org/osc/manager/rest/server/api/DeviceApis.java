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

    public void init(EntityManager em, TransactionControl txControl) throws Exception {
        this.em = em;
        this.txControl = txControl;
        this.api = new IsmDeviceApi(null, this.txControl, this.em);
    }

    @POST
    public String createDevice(DeviceEntity entity) throws Exception {
        LOG.info(String.format("Creating device entity for (name %s ; VSid %s)", "" + entity.getName(),
                "" + entity.getVsId()));
        return this.api.createDevice(entity);
    }

    @Path("/{deviceId}")
    @PUT
    public DeviceEntity updateDevice(@PathParam("deviceId") Long deviceId, DeviceEntity entity) throws Exception {

        LOG.info(String.format("Updating the device for id %s ", "" + Long.toString(deviceId)));
        entity.setId(deviceId);
        this.api.updateVSSDevice(entity);
        return entity;
    }

    @Path("/{deviceId}")
    @DELETE
    public void deleteDevice(@PathParam("deviceId") Long deviceId) throws Exception {
        LOG.info(String.format("Deleting the device for id %s ", "" + Long.toString(deviceId)));
        this.txControl.required(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                DeviceEntity result = DeviceApis.this.em.find(DeviceEntity.class, deviceId);
                if (result == null) {
                    LOG.info(String.format("Attempt to delete device for id %s and device not found, no-op.",
                            "" + Long.toString(deviceId)));
                    return null;
                }
                DeviceApis.this.em.remove(result);
                return null;
            }
        });
    }

    @GET
    public List<String> getDeviceIds() throws Exception {
        LOG.info("Listing device ids'");
        List<? extends ManagerDeviceElement> devices = this.api.listDevices();
        List<String> deviceList = new ArrayList<String>();
        if (devices.isEmpty()) {
            return deviceList;
        }
        deviceList = devices.stream().map(ManagerDeviceElement::getId).collect(Collectors.toList());
        return deviceList;
    }

    @Path("/{deviceId}")
    @GET
    public DeviceEntity getDevice(@PathParam("deviceId") Long deviceId) throws Exception {
        LOG.info(String.format("Getting the device for id %s ", "" + Long.toString(deviceId)));
        return (DeviceEntity) this.api.getDeviceById(Long.toString(deviceId));
    }

    @Path("/{deviceId}/members")
    @POST
    public String createDeviceMember(@PathParam("deviceId") Long deviceId, DeviceMemberEntity entity) throws Exception {
        LOG.info(String.format("Creating the device with name %s ", "" + entity.getName()));
        return this.api.createDeviceMember(entity, deviceId, null);
    }

    @Path("/{deviceId}/members/{memberId}")
    @PUT
    public DeviceMemberEntity updateDeviceMember(@PathParam("deviceId") Long deviceId,
            @PathParam("memberId") Long memberId, DeviceMemberEntity entity) throws Exception {
        LOG.info(String.format("Updating the device member with (memberid %s ; deviceid %s)",
                "" + Long.toString(memberId), "" + Long.toString(deviceId)));
        entity.setId(memberId);
        this.api.updateDeviceMember(entity, deviceId, null);
        return entity;
    }

    @Path("/{deviceId}/members/{memberId}")
    @DELETE
    public void deleteDeviceMember(@PathParam("deviceId") Long deviceId, @PathParam("memberId") Long memberId,
            DeviceMemberEntity entity) throws Exception {
        LOG.info(String.format("Deleting the device member with (memberid %s ; deviceid %s)",
                "" + Long.toString(memberId), "" + Long.toString(deviceId)));
        this.api.deleteDeviceMember(deviceId, null, memberId);
    }

    @SuppressWarnings("unchecked")
    @Path("/{deviceId}/members")
    @GET
    public List<String> getDeviceMembersIds(@PathParam("deviceId") Long deviceId) throws Exception {
        LOG.info(String.format("Getting the device members ids for deviceid %s ", "" + Long.toString(deviceId)));
        List<DeviceMemberEntity> deviceMembers = new ArrayList<DeviceMemberEntity>();
        List<String> members = new ArrayList<String>();
        deviceMembers = (List<DeviceMemberEntity>) this.api.listDeviceMembers(deviceId, null);
        if (!deviceMembers.isEmpty()) {
            members = deviceMembers.stream().map(DeviceMemberEntity::getId).collect(Collectors.toList());
        }
        return members;
    }

    @Path("/{deviceId}/members/{memberId}")
    @GET
    public DeviceMemberEntity getDeviceMember(@PathParam("deviceId") Long deviceId,
            @PathParam("memberId") Long memberId) throws Exception {
        LOG.info(String.format("getting the device member with (memberid %s ; deviceid %s)",
                "" + Long.toString(memberId), "" + Long.toString(deviceId)));
        return this.api.getDeviceMemberById(deviceId, null, memberId);
    }
}
