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
import org.osc.manager.ism.entities.PolicyEntity;
import org.osc.manager.rest.server.SecurityManagerServerRestConstants;
import org.osgi.service.component.annotations.Component;

@Component(service = DomainApis.class)
@Path(SecurityManagerServerRestConstants.SERVER_API_PATH_PREFIX + "/domains")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})

public class DomainApis {

    private static final Logger logger = Logger.getLogger(DomainApis.class);

    /**
     * Creates the Policy for a given domain
     *
     * @return policy
     */
    @Path("/{domainid}")
    @POST
    public PolicyEntity createPolicy(@PathParam("domainid") Long domainid, PolicyEntity entity) {

        logger.info("Creating Policy Entity...");
        //TODO Sudhir: Add db calls here
        return null;
    }

    /**
     * Updates the Policy for a given domain and the policy
     *
     * @return - updated policy
     */
    @Path("/{domainid}/policies/{policyid}")
    @PUT
    public PolicyEntity updatePolicy(@PathParam("domainid") Long domainid, @PathParam("policyid") Long policyid,
            PolicyEntity entity) {

        logger.info("Updating Policy Entity ...");
        //TODO Sudhir: Add db calls here
        return null;
    }

    /**
     * Deletes the Policy for a given domain and the policy
     *
     * @return - deleted policy
     */
    @Path("/{domainid}/policies/{policyid}")
    @DELETE
    public PolicyEntity deletePolicy(@PathParam("domainid") Long domainid, @PathParam("policyid") Long policyid,
            PolicyEntity entity) {

        logger.info("Deleting Policies Entity ...");
        //TODO Sudhir: Add db calls here
        return null;
    }

    /**
     * Lists the Policy Id's for a given domain
     *
     * @return - Policy Id's
     */
    @Path("/{domainid}")
    @GET
    public List<String> getPolicyIds(@PathParam("domainid") Long domainid) {

        logger.info("Listing Policy Ids'");
	    //TODO Sudhir: Add db calls here
		return null;
    }

    /**
     * Gets the Policy for a given domain and the policy
     *
     * @return - Policy
     */
    @Path("/{domainid}/policies/{policyid}")
	@GET
    public PolicyEntity getPolicy(@PathParam("domainid") Long domainid, @PathParam("policyId") Long policyid) {

        logger.info("getting Policy ");
        //TODO Sudhir: Add db calls here
		return null;
    }
}
