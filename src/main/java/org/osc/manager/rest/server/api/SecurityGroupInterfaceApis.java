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
import org.osc.manager.ism.entities.SGInterfaceElement;
import org.osc.manager.ism.entities.SecurityGroupEntity;
import org.osc.manager.ism.entities.SecurityGroupInterfaceEntity;
import org.osc.manager.ism.entities.VSElement;
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
        this.validationUtil.validateParentIdMatches(device, Long.parseLong(entity.getDevice().getId()));
        VSElement vs = new VSElement(deviceId, null);
        this.sgiApi = new IsmSecurityGroupInterfaceApi(vs, null, this.txControl, this.em);
        if (entity.getSecurityGroup() != null) {
            SecurityGroupEntity sgElement = new SecurityGroupEntity(entity.getName(), entity.getDevice());
            sgElement.setId(entity.getSecurityGroup().getId());
            entity.setSecurityGroup(sgElement);
        }
        return this.sgiApi.createSecurityGroupInterface(new SGInterfaceElement(entity));
    }

    @Path("/{sgIntfId}")
    @PUT
    public SecurityGroupInterfaceEntity updateSecurityGroupInterface(@PathParam("sgIntfId") Long sgIntfId,
            @PathParam("deviceId") Long deviceId, SecurityGroupInterfaceEntity entity) throws Exception {
        LOG.info(String.format("Updating the security group interface with sginterfaceid %s", Long.toString(sgIntfId)));
        DeviceEntity device = this.validationUtil.getDeviceOrThrow(Long.toString(deviceId));
        this.validationUtil.validateParentIdMatches(device, Long.parseLong(entity.getDevice().getId()));
        VSElement vs = new VSElement(deviceId, null);
        this.sgiApi = new IsmSecurityGroupInterfaceApi(vs, null, this.txControl, this.em);
        entity.setId(sgIntfId);
        this.sgiApi.updateSecurityGroupInterface(new SGInterfaceElement(entity));
        return entity;
    }

    @Path("/{sgIntfId}")
    @DELETE
    public void deleteSecurityGroupInterface(@PathParam("sgIntfId") Long sgIntfId, @PathParam("deviceId") Long deviceId)
            throws Exception {
        LOG.info(String.format("Deleting the security group interface with sginterfaceid %s", Long.toString(sgIntfId)));
        this.validationUtil.getDeviceOrThrow(Long.toString(deviceId));
        VSElement vs = new VSElement(deviceId, null);
        this.sgiApi = new IsmSecurityGroupInterfaceApi(vs, null, this.txControl, this.em);
        this.sgiApi.deleteSecurityGroupInterface(Long.toString(sgIntfId));
    }

    @GET
    public List<String> getSecurityGroupInterfaceIds(@PathParam("deviceId") Long deviceId)
            throws Exception {
        LOG.info("Listing security group interface ids'");
        this.validationUtil.getDeviceOrThrow(Long.toString(deviceId));
        VSElement vs = new VSElement(deviceId, null);
        this.sgiApi = new IsmSecurityGroupInterfaceApi(vs, null, this.txControl, this.em);
        List<? extends ManagerSecurityGroupInterfaceElement> sgiElements = this.sgiApi.listSecurityGroupInterfaces();
        List<String> sgiList = new ArrayList<String>();
        if (!sgiElements.isEmpty()) {
            sgiList = sgiElements.stream().map(ManagerSecurityGroupInterfaceElement::getSecurityGroupInterfaceId)
                    .collect(Collectors.toList());
        }
        return sgiList;
    }

    @Path("/{sgIntfId}")
    @GET
    public SecurityGroupInterfaceEntity getSecurityGroupInterface(@PathParam("sgIntfId") Long sgIntfId,
            @PathParam("deviceId") Long deviceId) throws Exception {
        LOG.info(String.format("getting the security group interface with sginterfaceid %s", Long.toString(sgIntfId)));
        this.validationUtil.getDeviceOrThrow(Long.toString(deviceId));
        VSElement vs = new VSElement(deviceId, null);
        this.sgiApi = new IsmSecurityGroupInterfaceApi(vs, null, this.txControl, this.em);
        SecurityGroupInterfaceEntity sgiElement = (SecurityGroupInterfaceEntity) this.sgiApi
                .getSecurityGroupInterfaceById(Long.toString(sgIntfId));
        return sgiElement == null ? null : sgiElement;
    }
}
