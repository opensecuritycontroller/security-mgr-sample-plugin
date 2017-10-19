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

import org.osc.manager.ism.api.IsmSecurityGroupApi;
import org.osc.manager.ism.api.util.ValidationUtil;
import org.osc.manager.ism.entities.DeviceEntity;
import org.osc.manager.ism.entities.SecurityGroupEntity;
import org.osc.manager.ism.entities.VirtualSystemElementImpl;
import org.osc.sdk.manager.element.ManagerSecurityGroupElement;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.transaction.control.TransactionControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = SecurityGroupApis.class)
@Path("/devices/{deviceId}/securitygroups")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class SecurityGroupApis {

    private static Logger LOG = LoggerFactory.getLogger(SecurityGroupApis.class);

    private IsmSecurityGroupApi sgApi;

    private EntityManager em;

    private TransactionControl txControl;

    private ValidationUtil validationUtil;

    public void init(EntityManager em, TransactionControl txControl) throws Exception {
        this.em = em;
        this.txControl = txControl;
        this.validationUtil = new ValidationUtil(txControl, em);
    }

    @POST
    public String createSecurityGroup(@PathParam("deviceId") Long deviceId, SecurityGroupEntity entity)
            throws Exception {
        LOG.info(String.format("Creating security group  with name %s", entity.getName()));

        DeviceEntity device = this.validationUtil.getDeviceOrThrow(Long.toString(deviceId));
        this.validationUtil.validateParentIdMatches(device, Long.parseLong(entity.getDevice().getId()),
                "SecurityGroup");

        // TODO : SUDHIR - Add SecurityGroupMember

        VirtualSystemElementImpl vs = new VirtualSystemElementImpl(deviceId, null);
        this.sgApi = new IsmSecurityGroupApi(vs, this.txControl, this.em);

        return this.sgApi.createSecurityGroup(entity.getName(), null, null);
    }

    @Path("/{sgId}")
    @PUT
    public SecurityGroupEntity updateSecurityGroup(@PathParam("deviceId") Long deviceId, @PathParam("sgId") Long sgId,
            SecurityGroupEntity entity)
                    throws Exception {
        LOG.info(String.format("Updating the security group for id %s ", Long.toString(sgId)));

        DeviceEntity device = this.validationUtil.getDeviceOrThrow(Long.toString(deviceId));
        this.validationUtil.validateParentIdMatches(device, Long.parseLong(entity.getDevice().getId()),
                "SecurityGroup");

        // TODO : SUDHIR - Add SecurityGroupMember

        VirtualSystemElementImpl vs = new VirtualSystemElementImpl(deviceId, null);
        this.sgApi = new IsmSecurityGroupApi(vs, this.txControl, this.em);

        this.sgApi.updateSecurityGroup(Long.toString(sgId), entity.getName(), null);

        return entity;
    }

    @Path("/{sgId}")
    @DELETE
    public void deleteSecurityGroup(@PathParam("sgId") Long sgId, @PathParam("deviceId") Long deviceId)
            throws Exception {
        LOG.info(String.format("Deleting the security group for id %s ", Long.toString(sgId)));

        this.validationUtil.getDeviceOrThrow(Long.toString(deviceId));

        VirtualSystemElementImpl vs = new VirtualSystemElementImpl(deviceId, null);
        this.sgApi = new IsmSecurityGroupApi(vs, this.txControl, this.em);

        this.sgApi.deleteSecurityGroup(Long.toString(sgId));
    }

    @GET
    public List<String> getSecurityGroupIds(@PathParam("deviceId") Long deviceId) throws Exception {
        LOG.info("Listing security group ids'");

        this.validationUtil.getDeviceOrThrow(Long.toString(deviceId));

        VirtualSystemElementImpl vs = new VirtualSystemElementImpl(deviceId, null);
        this.sgApi = new IsmSecurityGroupApi(vs, this.txControl, this.em);

        return this.sgApi.getSecurityGroupList().stream().map(ManagerSecurityGroupElement::getSGId)
                .collect(Collectors.toList());
    }

    @Path("/{sgId}")
    @GET
    public SecurityGroupEntity getSecurityGroup(@PathParam("sgId") Long sgId, @PathParam("deviceId") Long deviceId)
            throws Exception {
        LOG.info(String.format("Getting the security group for id %s ", Long.toString(sgId)));

        this.validationUtil.getDeviceOrThrow(Long.toString(deviceId));

        VirtualSystemElementImpl vs = new VirtualSystemElementImpl(deviceId, null);
        this.sgApi = new IsmSecurityGroupApi(vs, this.txControl, this.em);

        return (SecurityGroupEntity) this.sgApi.getSecurityGroupById(Long.toString(sgId));
    }
}
