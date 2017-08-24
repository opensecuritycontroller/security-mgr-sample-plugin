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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.osc.manager.ism.entities.PolicyEntity;
import org.osc.sdk.manager.api.ManagerPolicyApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osgi.service.transaction.control.TransactionControl;

public class IsmPolicyApi implements ManagerPolicyApi {

    Logger log = Logger.getLogger(IsmPolicyApi.class);
    private TransactionControl txControl;
    private EntityManager em;
    ApplianceManagerConnectorElement mc;

    public IsmPolicyApi(ApplianceManagerConnectorElement mc, TransactionControl txControl, EntityManager em)
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
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<PolicyEntity> q = cb.createQuery(PolicyEntity.class);
                Root<PolicyEntity> r = q.from(PolicyEntity.class);
                q.select(r)
                        .where(cb.and(cb.equal(r.get("domain").get("Id"), Long.parseLong(domainId)),
                                cb.equal(r.get("id"), Long.parseLong(policyId))));
                List<PolicyEntity> result = em.createQuery(q).getResultList();
                if (result.isEmpty() == false) {
                    PolicyEntity policy = new PolicyEntity();
                    policy.setName(result.get(0).getName());
                    policy.setId(Long.parseLong(result.get(0).getId()));
                    return policy;
                } else {
                    return null;
                }
            }
        });
    }

    @Override
    public List<PolicyEntity> getPolicyList(String domainId) throws Exception {

        return this.txControl.supports(new Callable<List<PolicyEntity>>() {

            @Override
            public List<PolicyEntity> call() throws Exception {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<PolicyEntity> q = cb.createQuery(PolicyEntity.class);
                Root<PolicyEntity> r = q.from(PolicyEntity.class);
                q.select(r).where(cb.and(cb.equal(r.get("domain").get("Id"), Long.parseLong(domainId))));
                List<PolicyEntity> policy = new ArrayList<PolicyEntity>();
                List<PolicyEntity> PolicyList = em.createQuery(q).getResultList();
                if (PolicyList.isEmpty() == false) {
                    for (PolicyEntity mgrPolicy : PolicyList) {
                        PolicyEntity policyElement=  new PolicyEntity();
                        policyElement.setName(mgrPolicy.getName());
                        policy.add(policyElement);
                    }
                    return policy;
                } else {
                    return null;
                }
            }
        });
    }
}
