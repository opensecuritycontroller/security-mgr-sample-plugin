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

import org.apache.log4j.Logger;
import org.osc.manager.ism.api.IsmSecurityGroupApi;
import org.osc.manager.ism.entities.SecurityGroupEntity;
import org.osc.manager.rest.server.SecurityManagerServerRestConstants;
import org.osc.sdk.manager.element.ManagerSecurityGroupElement;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.transaction.control.TransactionControl;

@Component(service = SecurityGroupApis.class)
@Path(SecurityManagerServerRestConstants.SERVER_API_PATH_PREFIX + "/securitygroups")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class SecurityGroupApis {
    private static Logger LOG = Logger.getLogger(SecurityGroupApis.class);
    private IsmSecurityGroupApi sgApi;

    public void init(EntityManager em, TransactionControl txControl) throws Exception {
        this.sgApi = new IsmSecurityGroupApi(null, txControl, em);
    }

    @Path("/{oscSgId}")
    @POST
    public String createSecurityGroup(@PathParam("oscSgId") String oscSgId, SecurityGroupEntity entity)
            throws Exception {
        LOG.info(String.format("Creating security group  with name %s", entity.getName()));
        // TODO : SUDHIR - Add SecurityGroupMember
        return this.sgApi.createSecurityGroup(entity.getSgDevice().getId(), entity.getName(), oscSgId, null);
    }

    @Path("/{deviceId}/sg/{sgId}")
    @PUT
    public SecurityGroupEntity updateSecurityGroup(@PathParam("sgId") Long sgId, SecurityGroupEntity entity)
            throws Exception {
        LOG.info(String.format("Updating the security group for id %s ", Long.toString(sgId)));
        // TODO : SUDHIR - Add SecurityGroupMember
        this.sgApi.updateSecurityGroup(entity.getSgDevice().getId(), Long.toString(sgId), entity.getName(), null);
        return entity;
    }

    @Path("/{deviceId}/sg/{sgId}")
    @DELETE
    public void deleteSecurityGroup(@PathParam("sgId") Long sgId, @PathParam("deviceId") Long deviceId)
            throws Exception {
        LOG.info(String.format("Deleting the security group for id %s ", Long.toString(sgId)));
        SecurityGroupApis.this.sgApi.deleteSecurityGroup(Long.toString(deviceId), Long.toString(sgId));
    }

    @Path("/{deviceId}/sg")
    @GET
    public List<String> getSecurityGroupIds(@PathParam("deviceId") Long deviceId) throws Exception {
        LOG.info("Listing security group ids'");
        List<? extends ManagerSecurityGroupElement> securityGroups = this.sgApi
                .getSecurityGroupList(Long.toString(deviceId));
        List<String> sgList = new ArrayList<String>();
        if (!securityGroups.isEmpty()) {
            sgList = securityGroups.stream().map(ManagerSecurityGroupElement::getSGId).collect(Collectors.toList());
        }
        return sgList;
    }

    @Path("/{deviceId}/sg/{sgId}")
    @GET
    public SecurityGroupEntity getSecurityGroup(@PathParam("sgId") Long sgId, @PathParam("deviceId") Long deviceId)
            throws Exception {
        LOG.info(String.format("Getting the security group for id %s ", Long.toString(sgId)));
        return this.sgApi.getSecurityGroup(Long.toString(deviceId), Long.toString(sgId));
    }
}
