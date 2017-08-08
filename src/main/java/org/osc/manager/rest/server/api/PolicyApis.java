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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.osc.manager.ism.entities.PolicyEntity;
import org.osc.manager.rest.server.SecurityManagerServerRestConstants;
import org.osgi.service.component.annotations.Component;

@Component(service = PolicyApis.class)
@Path(SecurityManagerServerRestConstants.SERVER_API_PATH_PREFIX + "/policy")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})

public class PolicyApis {

    private static final Logger logger = Logger.getLogger(PolicyApis.class);

    /**
     * Creates the Policy
     *
     * @return policy
     */
    @POST
    public PolicyEntity createPolicy(@Context HttpHeaders headers ,PolicyEntity entity) {

        logger.info("Creating Policy Entity...");
        //TODO Sudhir: Add db calls here
        return entity;
    }

    /**
     * Updates the Policy
     *
     * @return - updated policy
     */
    @Path("/{policyId}")
    @PUT
    public PolicyEntity updatePolicy(@Context HttpHeaders headers ,@PathParam("policyId") Long policyId,PolicyEntity entity) {

        logger.info("Updating Policy Entity ..." + policyId);
        //TODO Sudhir: Add db calls here
        return entity;
    }

    /**
     * Deletes the Delete Policy
     *
     * @return - deleted policy
     */
    @Path("/{policyId}")
    @DELETE
    public PolicyEntity deletePolicy(@Context HttpHeaders headers,@PathParam("policyId") Long policyId,PolicyEntity entity) {

        logger.info("Deleting Policies Entity ..." + policyId);
        //TODO Sudhir: Add db calls here
        return entity;
    }

    /**
     * Lists the Policy Id's
     *
     * @return - Policy Id's
     */
	@GET
    public List<String> getPolicyIds(@Context HttpHeaders headers) {

	    logger.info("Listing Policy Ids'");
	    //TODO Sudhir: Add db calls here
		return null;
    }

    /**
     * Gets the Policy
     *
     * @return - Policy
     */

    @Path("/{policyId}")
	@GET
    public PolicyEntity getPolicy(@Context HttpHeaders headers,@PathParam("policyId") Long policyId) {

        logger.info("getting Policy " + policyId);
        //TODO Sudhir: Add db calls here
		return null;
    }
}
