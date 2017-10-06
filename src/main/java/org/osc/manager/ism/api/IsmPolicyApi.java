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
package org.osc.manager.ism.api;

import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.osc.manager.ism.entities.DomainEntity;
import org.osc.manager.ism.entities.PolicyEntity;
import org.osc.manager.ism.utils.LogProvider;
import org.osc.sdk.manager.api.ManagerPolicyApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osgi.service.transaction.control.TransactionControl;
import org.slf4j.Logger;

public class IsmPolicyApi implements ManagerPolicyApi {

    private static final Logger LOG = LogProvider.getLogger(IsmPolicyApi.class);
    private TransactionControl txControl;
    private EntityManager em;
    ApplianceManagerConnectorElement mc;

    private IsmPolicyApi(ApplianceManagerConnectorElement mc, TransactionControl txControl, EntityManager em)
            throws Exception {
        this.txControl = txControl;
        this.em = em;
        this.mc = mc;
    }

    public static IsmPolicyApi create(ApplianceManagerConnectorElement mc, TransactionControl txControl,
            EntityManager em) throws Exception {
        return new IsmPolicyApi(mc, txControl, em);
    }

    @Override
    public PolicyEntity getPolicy(String policyId, String domainId) throws Exception {

        return this.txControl.supports(new Callable<PolicyEntity>() {

            @Override
            public PolicyEntity call() throws Exception {

                CriteriaBuilder criteriaBuilder = IsmPolicyApi.this.em.getCriteriaBuilder();
                CriteriaQuery<PolicyEntity> query = criteriaBuilder.createQuery(PolicyEntity.class);
                Root<PolicyEntity> r = query.from(PolicyEntity.class);
                query.select(r)
                        .where(criteriaBuilder.and(
                                criteriaBuilder.equal(r.get("domain").get("id"), Long.parseLong(domainId)),
                                criteriaBuilder.equal(r.get("id"), Long.parseLong(policyId))));
                List<PolicyEntity> result = IsmPolicyApi.this.em.createQuery(query).getResultList();
                if (result.isEmpty()) {
                    throw new Exception("Policy or Domain Entity does not exists...");
                    //TODO - Add 404 error response - Sudhir
                }
                return result.get(0);
            }
        });
    }

    @Override
    public List<PolicyEntity> getPolicyList(String domainId) throws Exception {

        return this.txControl.supports(new Callable<List<PolicyEntity>>() {

            @Override
            public List<PolicyEntity> call() throws Exception {

                DomainEntity result = IsmPolicyApi.this.em.find(DomainEntity.class, Long.parseLong(domainId));
                if (result == null) {
                    throw new Exception("Domain Entity does not exists...");
                    //TODO - to add RETURN 404 error:Sudhir
                }
                CriteriaBuilder criteriaBuilder = IsmPolicyApi.this.em.getCriteriaBuilder();
                CriteriaQuery<PolicyEntity> query = criteriaBuilder.createQuery(PolicyEntity.class);
                Root<PolicyEntity> r = query.from(PolicyEntity.class);
                query.select(r).where(criteriaBuilder
                        .and(criteriaBuilder.equal(r.get("domain").get("id"), Long.parseLong(domainId))));
                return IsmPolicyApi.this.em.createQuery(query).getResultList();
            }
        });
    }
}
