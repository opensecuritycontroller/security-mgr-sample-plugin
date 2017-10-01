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
import org.osc.manager.ism.api.IsmSecurityGroupApi;
import org.osc.manager.ism.api.IsmSecurityGroupInterfaceApi;
import org.osc.manager.ism.entities.SecurityGroupEntity;
import org.osc.manager.ism.entities.SecurityGroupInterfaceEntity;
import org.osc.manager.rest.server.SecurityManagerServerRestConstants;
import org.osc.sdk.manager.element.ManagerSecurityGroupElement;
import org.osc.sdk.manager.element.ManagerSecurityGroupInterfaceElement;
import org.osc.sdk.manager.element.SecurityGroupInterfaceElement;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.transaction.control.TransactionControl;

@Component(service = SecurityApis.class)
@Path(SecurityManagerServerRestConstants.SERVER_API_PATH_PREFIX + "/securitygroups")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class SecurityApis {
    private static Logger LOG = Logger.getLogger(SecurityApis.class);
    private EntityManager em;
    private TransactionControl txControl;
    private IsmSecurityGroupApi sgApi;
    private IsmSecurityGroupInterfaceApi sgiApi;

    public void init(EntityManager em, TransactionControl txControl) throws Exception {
        this.em = em;
        this.txControl = txControl;
        this.sgApi = new IsmSecurityGroupApi(null, txControl, em);
        this.sgiApi = new IsmSecurityGroupInterfaceApi(null, null, txControl, em);
    }

    @POST
    public String createSecurityGroup(SecurityGroupEntity entity) throws Exception {
        LOG.info(String.format("Creating security group  with name %s", "" + entity.getName()));
        return this.sgApi.createSecurityGroup(entity.getName(), null, null);
    }

    @Path("/{sgId}")
    @PUT
    public SecurityGroupEntity updateSecurityGroup(@PathParam("sgId") Long sgId, SecurityGroupEntity entity)
            throws Exception {
        LOG.info(String.format("Updating the security group for id %s ", "" + Long.toString(sgId)));
        this.sgApi.updateSecurityGroup(Long.toString(sgId), entity.getName(), null);
        return entity;
    }

    @Path("/{sgId}")
    @DELETE
    public void deleteSecurityGroup(@PathParam("sgId") Long sgId) throws Exception {
        LOG.info(String.format("Deleting the security group for id %s ", "" + Long.toString(sgId)));
        this.txControl.required(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SecurityApis.this.sgApi.deleteSecurityGroup(Long.toString(sgId));
                return null;
            }
        });
    }

    @GET
    public List<String> getSecurityGroupIds() throws Exception {
        LOG.info("Listing security group ids'");
        List<? extends ManagerSecurityGroupElement> securityGroups = this.sgApi.getSecurityGroupList();
        List<String> sgList = new ArrayList<String>();
        if (securityGroups.isEmpty()) {
            return sgList;
        }
        sgList = securityGroups.stream().map(ManagerSecurityGroupElement::getSGId).collect(Collectors.toList());
        return sgList;
    }

    @Path("/{sgId}")
    @GET
    public SecurityGroupEntity getSecurityGroup(@PathParam("sgId") Long sgId) throws Exception {
        LOG.info(String.format("Getting the security group for id %s ", "" + Long.toString(sgId)));
        return this.sgApi.getSecurityGroup(Long.toString(sgId));
    }

    @Path("/{sgId}/sgIntf")
    @POST
    public String createSecurityGroupInterface(@PathParam("sgId") Long sgId, SecurityGroupInterfaceEntity entity)
            throws Exception {
        LOG.info(String.format("Creating the security group interface with name %s ", "" + entity.getName()));
        SecurityGroupEntity sgElement = new SecurityGroupEntity();
        sgElement.setId(sgId);
        entity.setSecurityGroup(sgElement);
        return this.sgiApi.createSecurityGroupInterface(entity);
    }

    @Path("/{sgId}/sgIntf/{sgIntfId}")
    @PUT
    public SecurityGroupInterfaceElement updateSecurityGroupInterface(@PathParam("sgId") Long sgId,
            @PathParam("sgIntfId") Long sgIntfId, SecurityGroupInterfaceEntity entity) throws Exception {
        LOG.info(String.format("Updating the security group interface with sgid %s ; sginterfaceid %s",
                "" + Long.toString(sgId), "" + Long.toString(sgIntfId)));
        SecurityGroupEntity sgElement = new SecurityGroupEntity();
        sgElement.setId(sgId);
        entity.setSecurityGroup(sgElement);
        entity.setId(sgIntfId);
        this.sgiApi.updateSecurityGroupInterface(entity);
        return entity;
    }

    @Path("/{sgId}/sgIntf/{sgIntfId}")
    @DELETE
    public void deleteSecurityGroupInterface(@PathParam("sgId") Long sgId, @PathParam("sgIntfId") Long sgIntfId)
            throws Exception {
        LOG.info(String.format("Deleting the security group interface with sgid %s ; sginterfaceid %s",
                "" + Long.toString(sgId), "" + Long.toString(sgIntfId)));
        this.txControl.required(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SecurityApis.this.sgiApi.deleteSecurityGroupInterface(Long.toString(sgIntfId));
                return null;
            }
        });
    }

    @Path("/{sgId}/sgIntf")
    @GET
    public List<String> getSecurityGroupInterfaceIds() throws Exception {
        LOG.info("Listing security group interface ids'");
        List<? extends ManagerSecurityGroupInterfaceElement> sgiElements = this.sgiApi.listSecurityGroupInterfaces();
        List<String> sgiList = new ArrayList<String>();
        if (!sgiElements.isEmpty()) {
            sgiList = sgiElements.stream().map(ManagerSecurityGroupInterfaceElement::getSecurityGroupInterfaceId)
                    .collect(Collectors.toList());
        }
        return sgiList;
    }

    @Path("/{sgId}/sgIntf/{sgIntfId}")
    @GET
    public SecurityGroupInterfaceEntity getSecurityGroupInterface(@PathParam("sgId") Long sgId,
            @PathParam("sgIntfId") Long sgIntfId) throws Exception {
        LOG.info(String.format("getting the security group interface with sgid %s ; sginterfaceid %s)",
                "" + Long.toString(sgId), "" + Long.toString(sgIntfId)));
        return (SecurityGroupInterfaceEntity) this.sgiApi.getSecurityGroupInterfaceById(Long.toString(sgIntfId));
    }
}
