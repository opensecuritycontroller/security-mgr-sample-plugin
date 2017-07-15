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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.osc.manager.rest.server.SecureManagerServerRestConstants;
import org.osc.manager.ism.entities.PolicyElements;
import org.osgi.service.component.annotations.Component;




@Component(service = PolicyApis.class)
@Path(SecureManagerServerRestConstants.SERVER_API_PATH_PREFIX + "/policies")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})

public class PolicyApis {
	
	private static final Logger logger = Logger.getLogger(PolicyApis.class);

    /**
     * Creates the ISM policies
     *
     * @return policy
     */
    @POST
    public PolicyElements createIsmPolicies(@Context HttpHeaders headers ,PolicyElements elements){
    	
    	logger.info("Creating Policies Elements...");
       
    	// TODO DB Create

        return elements;
    }
    
    
    /**
     * Updates the ISM policy
     *
     * @return - updated policy
     */
    @Path("/{policyId}")    
    @PUT
    public PolicyElements updateIsmPolicies(@Context HttpHeaders headers ,@PathParam("policyId") Long policyId,
    										PolicyElements elements){
    	
    	logger.info("Updating Policies Element ..." + policyId);
       
    	// TODO DBUpdate

        return elements;
    }
    
    /**
     * Deletes the ISM Policy
     *
     * @return - deleted policy
     */
    @Path("/{policyId}")    
    @DELETE
    public PolicyElements deleteIsmPolicies(@Context HttpHeaders headers ,@PathParam("policyId") Long policyId,
    										PolicyElements elements){
    	
    	logger.info("Deleting Policies Element ..." + policyId);
       
    	// TODO DBUpdate

        return elements;
    }

    
    /**
     * Lists the ISM Policy
     *
     * @return - Policy List
     */
	@GET
    public List<PolicyElements> getPolicyList(@Context HttpHeaders headers) {
		
		logger.info("Listing Policies");
        
		// TODO retrieve from DB 
		
		return null;
    }
	
    /**
     * Gets the ISM Policy
     *
     * @return - Policy
     */
	
    @Path("/{policyId}")
	@GET
    public PolicyElements getPolicy(@Context HttpHeaders headers,@PathParam("policyId") Long policyId) {
		
    	 logger.info("getting Policy " + policyId);
        
		// TODO retrieve from DB 
		
		return null;
    }

	
}
