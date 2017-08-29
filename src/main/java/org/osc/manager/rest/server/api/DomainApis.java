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
import org.osc.manager.ism.entities.DomainEntity;
import org.osc.manager.ism.entities.PolicyEntity;
import org.osc.manager.rest.server.SecurityManagerServerRestConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.transaction.control.TransactionControl;

@Component(service = DomainApis.class)
@Path(SecurityManagerServerRestConstants.SERVER_API_PATH_PREFIX + "/domains")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})

public class DomainApis {

    private static final Logger logger = Logger.getLogger(DomainApis.class);
    private EntityManager em;
    private TransactionControl txControl;

    /**
     * init the DB
     *
     *
     */
    public void init(EntityManager em, TransactionControl txControl) throws Exception {
        this.em = em;
        this.txControl = txControl;
    }

    /**
     * Creates the Policy for a given domain
     *
     * @return policy
     */
    @Path("/{domainid}/policies")
    @POST
    public String createPolicy(@PathParam("domainid") Long domainid, PolicyEntity entity) {

        logger.info("Creating Policy Entity...:" + entity.getName());

        return this.txControl.required(new Callable<String>() {

            @Override
            public String call() throws Exception {

                CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

                CriteriaQuery<DomainEntity> query = criteriaBuilder.createQuery(DomainEntity.class);
                Root<DomainEntity> r = query.from(DomainEntity.class);
                query.select(r).where(criteriaBuilder.and(criteriaBuilder.equal(r.get("id"), domainid)));
                List<DomainEntity> result = em.createQuery(query).getResultList();

                if (result.isEmpty()) {
                    throw new Exception("Domain Entity does not exists...");
                    //TODO - Add Exception to return 404 error code: Sudhir
                }
                CriteriaQuery<PolicyEntity> policyquery = criteriaBuilder.createQuery(PolicyEntity.class);
                Root<PolicyEntity> rpolicy = policyquery.from(PolicyEntity.class);
                policyquery.select(rpolicy)
                        .where(criteriaBuilder.and(criteriaBuilder.equal(rpolicy.get("name"), entity.getName())));

                List<PolicyEntity> policyresult = em.createQuery(policyquery).getResultList();
                if (!policyresult.isEmpty()) {
                    throw new Exception("Policy Entity name already exists...:");
                    //TODO - to add RETURN 404 error:Sudhir
                }
                PolicyEntity policy = new PolicyEntity();
                policy.setName(entity.getName());
                policy.setDomain(result.get(0));
                policy.setParent(result.get(0).getapplianceManagerConnector());
                em.persist(policy);
                return policy.getId();
            }
        });
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

        logger.info("Updating for Policy Entity Id...:" + policyid);

        return this.txControl.required(new Callable<PolicyEntity>() {

            @Override
            public PolicyEntity call() throws Exception {

                CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

                CriteriaQuery<PolicyEntity> query = criteriaBuilder.createQuery(PolicyEntity.class);
                Root<PolicyEntity> r = query.from(PolicyEntity.class);
                query.select(r).where(criteriaBuilder.and(criteriaBuilder.equal(r.get("domain").get("id"), domainid)),
                        criteriaBuilder.equal(r.get("id"), policyid));

                List<PolicyEntity> result = em.createQuery(query).getResultList();
                if (result.isEmpty()) {
                    throw new Exception("domain or the policy Entity does not exists...");
                    //TODO - to add RETURN 404 error:Sudhir
                }
                result.get(0).setName(entity.getName());
                em.persist(result.get(0));
                return entity;
            }
        });
    }

    /**
     * Deletes the Policy for a given domain and the policy
     *
     * @return - deleted policy
     */
    @Path("/{domainid}/policies/{policyid}")
    @DELETE
    public void deletePolicy(@PathParam("domainid") Long domainid, @PathParam("policyid") Long policyid,
            PolicyEntity entity) {

        logger.info("Deleting for Policies Entity Id...:" + policyid);

        this.txControl.required(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

                CriteriaQuery<PolicyEntity> query = criteriaBuilder.createQuery(PolicyEntity.class);
                Root<PolicyEntity> r = query.from(PolicyEntity.class);
                query.select(r).where(criteriaBuilder.and(criteriaBuilder.equal(r.get("domain").get("id"), domainid)),
                        criteriaBuilder.equal(r.get("id"), policyid));

                List<PolicyEntity> result = DomainApis.this.em.createQuery(query).getResultList();
                if (result.isEmpty()) {
                    return null;
                }
                em.remove(result.get(0));
                return null;
            }
        });
    }

    /**
     * Lists the Policy Id's for a given domain
     *
     * @return - Policy Id's
     */
    @Path("/{domainid}/policies")
    @GET
    public List<String> getPolicyIds(@PathParam("domainid") Long domainid) {

        logger.info("Listing Policy Ids'for domain Id ...:" + domainid);


        return this.txControl.supports(new Callable<List<String>>() {

            @Override
            public List<String> call() throws Exception {
                CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
                CriteriaQuery<PolicyEntity> query = criteriaBuilder.createQuery(PolicyEntity.class);
                Root<PolicyEntity> r = query.from(PolicyEntity.class);
                query.select(r).where(criteriaBuilder.and(criteriaBuilder.equal(r.get("domain").get("id"), domainid)));
                List<PolicyEntity> PolicyList = em.createQuery(query).getResultList();

                if (PolicyList.isEmpty()) {
                    throw new Exception("Domain Entity does not exists...");
                    //TODO - to add RETURN 404 error:Sudhir
                }
                List<String> policy = new ArrayList<String>();
                for (PolicyEntity mgrPolicy : PolicyList) {
                    policy.add(new String(mgrPolicy.getId()));
                }
                if (policy.isEmpty()) {
                    return null;
                }
                return policy;
            }
        });
    }

    /**
     * Gets the Policy for a given domain and the policy
     *
     * @return - Policy
     * @throws Exception
     */
    @Path("/{domainid}/policies/{policyid}")
	@GET
    public PolicyEntity getPolicy(@PathParam("domainid") Long domainid, @PathParam("policyid") Long policyid)
            throws Exception {

        logger.info("getting Policy for Policy ID..:" + policyid);

        return this.txControl.supports(new Callable<PolicyEntity>() {

            @Override
            public PolicyEntity call() throws Exception {
                CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
                CriteriaQuery<PolicyEntity> query = criteriaBuilder.createQuery(PolicyEntity.class);
                Root<PolicyEntity> r = query.from(PolicyEntity.class);
                query.select(r).where(criteriaBuilder.and(criteriaBuilder.equal(r.get("domain").get("id"), domainid),
                        criteriaBuilder.equal(r.get("id"), policyid)));
                List<PolicyEntity> result = em.createQuery(query).getResultList();
                if (result.isEmpty()) {
                    throw new Exception("Policy or Domain Entity does not exists...");
                    //TODO - Add 404 error response - Sudhir
                }
                PolicyEntity policy = new PolicyEntity();
                policy.setName(result.get(0).getName());
                policy.setId(Long.parseLong(result.get(0).getId()));
                return policy;
            }
        });
    }

    /**
     * Creates the Domain for a given ApplianceManagerConnector
     *
     * @return Domain
     */
    @POST
    public String createDomain(DomainEntity entity) {

        logger.info("Creating Domain Entity...:" + entity.getName());

        return this.txControl.required(new Callable<String>() {

            @Override
            public String call() throws Exception {

                DomainEntity domainElement = null;
                CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

                CriteriaQuery<ApplianceManagerConnectorEntity> query = criteriaBuilder
                        .createQuery(ApplianceManagerConnectorEntity.class);
                Root<ApplianceManagerConnectorEntity> r = query.from(ApplianceManagerConnectorEntity.class);
                query.select(r)
                        .where(criteriaBuilder.and(
                                criteriaBuilder.equal(r.get("name"), entity.getapplianceManagerConnector().getName())));

                List<ApplianceManagerConnectorEntity> result = em.createQuery(query).getResultList();

                if (result.isEmpty()) {
                    throw new Exception("Appliance Manager Connector does not exists...");
                    //TODO - to add RETURN 400 error:Sudhir
                }

                CriteriaQuery<DomainEntity> domainquery = criteriaBuilder.createQuery(DomainEntity.class);
                Root<DomainEntity> rdomain = domainquery.from(DomainEntity.class);
                domainquery.select(rdomain)
                        .where(criteriaBuilder
                        .and(criteriaBuilder.equal(r.get("name"), entity.getName())));

                List<DomainEntity> domainresult = em.createQuery(domainquery).getResultList();
                if (!domainresult.isEmpty()) {
                    throw new Exception("Domain name already exists...");
                    //TODO - to add RETURN 400 error:Sudhir
                }
                domainElement = new DomainEntity();
                domainElement.setName(entity.getName());
                domainElement.setapplianceManagerConnector(result.get(0));
                em.persist(domainElement);
                return domainElement.getId();
            }
        });
    }

    /**
     * Updates the Domain for a given domain Id
     *
     * @return - updated Domain
     */
    @Path("/{domainid}")
    @PUT
    public DomainEntity updateDomain(@PathParam("domainid") Long domainid, DomainEntity entity) {

        logger.info("Updating Domain Entity ID...:" + domainid);

        return this.txControl.required(new Callable<DomainEntity>() {

            @Override
            public DomainEntity call() throws Exception {

                CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
                CriteriaQuery<DomainEntity> query = criteriaBuilder.createQuery(DomainEntity.class);
                Root<DomainEntity> r = query.from(DomainEntity.class);
                query.select(r).where(criteriaBuilder.and(criteriaBuilder.equal(r.get("id"), domainid)));
                List<DomainEntity> result = em.createQuery(query).getResultList();
                if (result.isEmpty()) {
                    throw new Exception("Domain Entity does not exists...");
                    //TODO - to add RETURN 404 error:Sudhir
                }
                result.get(0).setName(entity.getName());
                em.persist(result.get(0));
                DomainEntity domain = new DomainEntity();
                domain.setName(result.get(0).getName());
                domain.setId(Long.parseLong(result.get(0).getId()));
                return domain;
            }
        });
    }

    /**
     * Deletes the Domain for a given domain Id
     *
     * @return - deleted Domain
     */
    @Path("/{domainid}")
    @DELETE
    public void deleteDomain(@PathParam("domainid") Long domainid) {

        logger.info("Deleting Domain Entity ID...:" + domainid);

        this.txControl.required(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
                CriteriaQuery<DomainEntity> query = criteriaBuilder.createQuery(DomainEntity.class);
                Root<DomainEntity> r = query.from(DomainEntity.class);
                query.select(r).where(criteriaBuilder.and(criteriaBuilder.equal(r.get("id"), domainid)));
                List<DomainEntity> result = em.createQuery(query).getResultList();
                if (result.isEmpty()) {
                    return null;
                }
                em.remove(result.get(0));
                return null;
            }
        });
    }

    /**
     * Lists the Domain Id's for a given domain
     *
     * @return - Domain Id's
     */
    @GET
    public List<String> getDomainIds() {

        logger.info("Listing Domain Ids'");

        return this.txControl.supports(new Callable<List<String>>() {

            @Override
            public List<String> call() throws Exception {
                CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
                CriteriaQuery<DomainEntity> query = criteriaBuilder
                        .createQuery(DomainEntity.class);
                Root<DomainEntity> r = query.from(DomainEntity.class);
                query.select(r);
                List<DomainEntity> result = em.createQuery(query).getResultList();
                if (result.isEmpty()) {
                    throw new Exception("Domain Entity does not exists...");
                    //TODO - to add RETURN 404 error:Sudhir
                }
                List<String> domainList = new ArrayList<String>();
                for (DomainEntity mgrDm : result) {
                    domainList.add(new String(mgrDm.getId()));
                }
                if (domainList.isEmpty()) {
                    return null;
                }
                return domainList;
            }
        });
    }

    /**
     * Gets the Domain for a given domain Id
     *
     * @return - Domain
     */
    @Path("/{domainid}")
    @GET
    public DomainEntity getDomain(@PathParam("domainid") Long domainid) {

        logger.info("getting Domain for ID...:" + domainid);

        return this.txControl.required(new Callable<DomainEntity>() {

            @Override
            public DomainEntity call() throws Exception {
                CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

                CriteriaQuery<DomainEntity> query = criteriaBuilder.createQuery(DomainEntity.class);
                Root<DomainEntity> r = query.from(DomainEntity.class);
                query.select(r).where(criteriaBuilder.and(criteriaBuilder.equal(r.get("id"), domainid)));
                List<DomainEntity> result = em.createQuery(query).getResultList();
                if (result.isEmpty()) {
                    return null;
                }
                DomainEntity domain = new DomainEntity();
                domain.setName(result.get(0).getName());
                domain.setId(Long.parseLong(result.get(0).getId()));
                return domain;
            }
        });
    }
}
