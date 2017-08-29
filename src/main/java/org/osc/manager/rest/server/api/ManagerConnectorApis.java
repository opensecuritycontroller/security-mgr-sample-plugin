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

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
import org.osc.manager.ism.entities.ApplianceManagerConnectorEntity;
import org.osc.manager.rest.server.SecurityManagerServerRestConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.transaction.control.TransactionControl;

@Component(service = ManagerConnectorApis.class)
@Path(SecurityManagerServerRestConstants.SERVER_API_PATH_PREFIX + "/applianceManagerConnectors")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

public class ManagerConnectorApis {

    private static final Logger logger = Logger.getLogger(ManagerConnectorApis.class);
    private EntityManager em;
    private TransactionControl txControl;

    /**
     * Init the DB
     *
     *
     */
    public void init(EntityManager em, TransactionControl txControl) throws Exception {
        this.em = em;
        this.txControl = txControl;
    }

    /**
     * Creates the MC
     *
     * @return MC
     * @throws Exception
     */
    @POST
    public String createManagerConnector(ApplianceManagerConnectorEntity entity)
            throws Exception {

        logger.info("Creating Manager Connector...:" + entity.getName());

        return this.txControl.required(new Callable<String>() {

            @Override
            public String call() throws Exception {
                ApplianceManagerConnectorEntity mc = new ApplianceManagerConnectorEntity();
                mc.setName(entity.getName());

                CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

                CriteriaQuery<ApplianceManagerConnectorEntity> query = criteriaBuilder
                        .createQuery(ApplianceManagerConnectorEntity.class);
                Root<ApplianceManagerConnectorEntity> r = query.from(ApplianceManagerConnectorEntity.class);
                query.select(r).where(criteriaBuilder.and(criteriaBuilder.equal(r.get("name"), entity.getName())));
                List<ApplianceManagerConnectorEntity> result = em.createQuery(query).getResultList();
                if (!result.isEmpty()) {
                    throw new Exception("Manager Connector Entity name already exists..");
                    //TODO - to add RETURN 404 error:Sudhir
                }
                em.persist(mc);
                Long id = mc.getid();
                return Long.toString(id);
            }
        });
    }

    /**
     * Updates MC for a given ID
     *
     * @return - updated MC
     */
    @Path("/{applianceManagerConnectorId}")
    @PUT
    public ApplianceManagerConnectorEntity updateManagerConnector(@PathParam("applianceManagerConnectorId") Long amcId,
            ApplianceManagerConnectorEntity entity) {

        logger.info("Updating MC Entity Id...:" + amcId);

        return this.txControl.required(new Callable<ApplianceManagerConnectorEntity>() {
            @Override
            public ApplianceManagerConnectorEntity call() throws Exception {
                CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

                CriteriaQuery<ApplianceManagerConnectorEntity> query = criteriaBuilder
                        .createQuery(ApplianceManagerConnectorEntity.class);
                Root<ApplianceManagerConnectorEntity> r = query.from(ApplianceManagerConnectorEntity.class);
                query.select(r).where(criteriaBuilder.and(criteriaBuilder.equal(r.get("id"), amcId)));

                List<ApplianceManagerConnectorEntity> result = ManagerConnectorApis.this.em.createQuery(query)
                        .getResultList();
                if (result.isEmpty()) {
                    throw new Exception("Manager Connector Entity does not already exists..");
                    //TODO - to add RETURN 404 error:Sudhir
                }
                result.get(0).setName(entity.getName());
                em.persist(result.get(0));
                return entity;

            }
        });
    }

    /**
     * Delete MC for a given ID
     *
     * @return - deleted MC
     */
    @Path("/{applianceManagerConnectorId}")
    @DELETE
    public void deleteManagerConnector(@PathParam("applianceManagerConnectorId") Long amcId,
            ApplianceManagerConnectorEntity entity) {

        logger.info("Deleting MC Entity Id...:" + amcId);

        this.txControl.required(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                CriteriaBuilder criteriaBuilder = ManagerConnectorApis.this.em.getCriteriaBuilder();

                CriteriaQuery<ApplianceManagerConnectorEntity> query = criteriaBuilder
                        .createQuery(ApplianceManagerConnectorEntity.class);
                Root<ApplianceManagerConnectorEntity> r = query.from(ApplianceManagerConnectorEntity.class);
                query.select(r).where(criteriaBuilder.and(criteriaBuilder.equal(r.get("id"), amcId)));

                List<ApplianceManagerConnectorEntity> result = em.createQuery(query).getResultList();
                if (result.isEmpty()) {
                    return null;
                }
                em.remove(result.get(0));
                return null;
            }
        });
    }

    /**
     * Lists the MC for a given ID
     *
     * @return - MC
     */
    @Path("/{applianceManagerConnectorId}")
    @GET
    public ApplianceManagerConnectorEntity getManagerConnector(@PathParam("applianceManagerConnectorId") Long amcId) {

        logger.info("Query MC for Id...:" + amcId);

        return this.txControl.required(new Callable<ApplianceManagerConnectorEntity>() {
            @Override
            public ApplianceManagerConnectorEntity call() throws Exception {
                CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
                CriteriaQuery<ApplianceManagerConnectorEntity> query = criteriaBuilder
                        .createQuery(ApplianceManagerConnectorEntity.class);
                Root<ApplianceManagerConnectorEntity> r = query.from(ApplianceManagerConnectorEntity.class);
                query.select(r).where(criteriaBuilder.and(criteriaBuilder.equal(r.get("id"), amcId)));

                List<ApplianceManagerConnectorEntity> result = em.createQuery(query).getResultList();
                if (result.isEmpty()) {
                    return null;
                }
                ApplianceManagerConnectorEntity mc = new ApplianceManagerConnectorEntity();
                mc.setName(result.get(0).getName());
                mc.setid(result.get(0).getid());
                return mc;
            }
        });
    }

    /**
     * lists all MC's
     *
     * @return - MC lists
     * @throws Exception
     */
    @GET
    public List<String> getManagerConnectorIds() throws Exception {

        logger.info("Listing MC Ids'");

        return this.txControl.supports(new Callable<List<String>>() {

            @Override
            public List<String> call() throws Exception {
                CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
                CriteriaQuery<ApplianceManagerConnectorEntity> query = criteriaBuilder
                        .createQuery(ApplianceManagerConnectorEntity.class);
                Root<ApplianceManagerConnectorEntity> r = query.from(ApplianceManagerConnectorEntity.class);
                query.select(r);
                List<ApplianceManagerConnectorEntity> result = em.createQuery(query).getResultList();
                if (result.isEmpty()) {
                    return null;
                }
                List<String> mcIds = new ArrayList<String>();
                for (ApplianceManagerConnectorEntity mgrMc : result) {
                    mcIds.add(new String(Long.toString(mgrMc.getid())));
                }
                return mcIds;
            }
        });
    }
}
