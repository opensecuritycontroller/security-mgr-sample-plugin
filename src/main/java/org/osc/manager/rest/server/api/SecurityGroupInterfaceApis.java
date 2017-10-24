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

import java.util.List;
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

import org.osc.manager.ism.api.IsmSecurityGroupInterfaceApi;
import org.osc.manager.ism.api.util.ValidationUtil;
import org.osc.manager.ism.entities.DeviceEntity;
import org.osc.manager.ism.entities.SecurityGroupInterfaceEntity;
import org.osc.manager.ism.model.SecurityGroupInterfaceElementImpl;
import org.osc.manager.ism.model.VirtualSystemElementImpl;
import org.osc.sdk.manager.element.ManagerSecurityGroupInterfaceElement;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.transaction.control.TransactionControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = SecurityGroupInterfaceApis.class)
@Path("/devices/{deviceId}/securityGroupInterfaces")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class SecurityGroupInterfaceApis {

    private static Logger LOG = LoggerFactory.getLogger(SecurityGroupInterfaceApis.class);

    private IsmSecurityGroupInterfaceApi sgiApi;

    private TransactionControl txControl;

    private EntityManager em;

    private ValidationUtil validationUtil;

    public void init(EntityManager em, TransactionControl txControl) throws Exception {
        this.txControl = txControl;
        this.em = em;
        this.validationUtil = new ValidationUtil(this.txControl, this.em);
    }

    @POST
    public String createSecurityGroupInterface(@PathParam("deviceId") Long deviceId,
            SecurityGroupInterfaceEntity entity)
                    throws Exception {
        LOG.info(String.format("Creating the security group interface with name %s ", entity.getName()));

        DeviceEntity device = this.validationUtil.getDeviceOrThrow(Long.toString(deviceId));
        this.validationUtil.validateIdMatches(device, Long.parseLong(entity.getDevice().getId()),
                "SecurityGroupInterface");

        VirtualSystemElementImpl vs = new VirtualSystemElementImpl(deviceId, null);
        this.sgiApi = new IsmSecurityGroupInterfaceApi(vs, null, this.txControl, this.em);

        return this.sgiApi.createSecurityGroupInterface(new SecurityGroupInterfaceElementImpl(entity));
    }

    @Path("/{sgiId}")
    @PUT
    public SecurityGroupInterfaceEntity updateSecurityGroupInterface(@PathParam("sgiId") Long sgiId,
            @PathParam("deviceId") Long deviceId, SecurityGroupInterfaceEntity entity) throws Exception {
        LOG.info(String.format("Updating the security group interface with sginterfaceid %s", Long.toString(sgiId)));

        DeviceEntity device = this.validationUtil.getDeviceOrThrow(Long.toString(deviceId));
        this.validationUtil.validateIdMatches(device, Long.parseLong(entity.getDevice().getId()),
                "SecurityGroupInterface");

        VirtualSystemElementImpl vs = new VirtualSystemElementImpl(deviceId, null);
        this.sgiApi = new IsmSecurityGroupInterfaceApi(vs, null, this.txControl, this.em);
        entity.setId(sgiId);

        this.sgiApi.updateSecurityGroupInterface(new SecurityGroupInterfaceElementImpl(entity));

        return entity;
    }

    @Path("/{sgiId}")
    @DELETE
    public void deleteSecurityGroupInterface(@PathParam("sgiId") Long sgiId, @PathParam("deviceId") Long deviceId)
            throws Exception {
        LOG.info(String.format("Deleting the security group interface with sginterfaceid %s", Long.toString(sgiId)));

        this.validationUtil.getDeviceOrThrow(Long.toString(deviceId));

        VirtualSystemElementImpl vs = new VirtualSystemElementImpl(deviceId, null);
        this.sgiApi = new IsmSecurityGroupInterfaceApi(vs, null, this.txControl, this.em);

        this.sgiApi.deleteSecurityGroupInterface(Long.toString(sgiId));
    }

    @GET
    public List<String> getSecurityGroupInterfaceIds(@PathParam("deviceId") Long deviceId)
            throws Exception {
        LOG.info("Listing security group interface ids'");

        this.validationUtil.getDeviceOrThrow(Long.toString(deviceId));

        VirtualSystemElementImpl vs = new VirtualSystemElementImpl(deviceId, null);
        this.sgiApi = new IsmSecurityGroupInterfaceApi(vs, null, this.txControl, this.em);

        return this.sgiApi.listSecurityGroupInterfaces().stream()
                .map(ManagerSecurityGroupInterfaceElement::getSecurityGroupInterfaceId).collect(Collectors.toList());
    }

    @Path("/{sgiId}")
    @GET
    public SecurityGroupInterfaceEntity getSecurityGroupInterface(@PathParam("sgiId") Long sgiId,
            @PathParam("deviceId") Long deviceId) throws Exception {
        LOG.info(String.format("getting the security group interface with sginterfaceid %s", Long.toString(sgiId)));

        this.validationUtil.getDeviceOrThrow(Long.toString(deviceId));

        VirtualSystemElementImpl vs = new VirtualSystemElementImpl(deviceId, null);
        this.sgiApi = new IsmSecurityGroupInterfaceApi(vs, null, this.txControl, this.em);

        return (SecurityGroupInterfaceEntity) this.sgiApi.getSecurityGroupInterfaceById(Long.toString(sgiId));
    }
}
