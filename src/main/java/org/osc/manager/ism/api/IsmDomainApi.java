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
import org.osc.manager.ism.entities.ApplianceManagerConnectorEntity;
import org.osc.manager.ism.entities.DomainEntity;
import org.osc.sdk.manager.api.ManagerDomainApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.ManagerDomainElement;
import org.osgi.service.transaction.control.TransactionControl;

public class IsmDomainApi implements ManagerDomainApi {

    Logger log = Logger.getLogger(IsmDomainApi.class);
    private TransactionControl txControl;
    private EntityManager em;
    ApplianceManagerConnectorElement mc;

    public IsmDomainApi(ApplianceManagerConnectorElement mc, TransactionControl txControl, EntityManager em)
            throws Exception {
        this.txControl = txControl;
        this.em = em;
        this.mc = mc;
    }

    public static IsmDomainApi create(ApplianceManagerConnectorElement mc, TransactionControl txControl,
            EntityManager em) throws Exception {

        return new IsmDomainApi(mc, txControl, em);
    }

    public String createDomain(ManagerDomainElement dm) throws Exception {

        String name = this.mc.getName();
        DomainEntity domains = new DomainEntity();
        domains.setName(dm.getName());

        return this.txControl.required(new Callable<DomainEntity>() {
            @Override
            public DomainEntity call() throws Exception {
                CriteriaBuilder cb = IsmDomainApi.this.em.getCriteriaBuilder();

                CriteriaQuery<ApplianceManagerConnectorEntity> q = cb
                        .createQuery(ApplianceManagerConnectorEntity.class);
                Root<ApplianceManagerConnectorEntity> r = q.from(ApplianceManagerConnectorEntity.class);
                q.select(r).where(cb.and(cb.equal(r.get("name"), name)));
                ApplianceManagerConnectorEntity result = IsmDomainApi.this.em.createQuery(q).getSingleResult();
                domains.setapplianceManagerConnector(result);
                IsmDomainApi.this.em.persist(domains);
                return domains;
            }
        }).getName();
    }

    @Override
    public DomainEntity getDomain(String domainId) throws Exception {

        return this.txControl.required(new Callable<DomainEntity>() {
            @Override
            public DomainEntity call() throws Exception {
                CriteriaBuilder cb = IsmDomainApi.this.em.getCriteriaBuilder();

                CriteriaQuery<DomainEntity> q = cb.createQuery(DomainEntity.class);
                Root<DomainEntity> r = q.from(DomainEntity.class);
                q.select(r).where(cb.and(cb.equal(r.get("id"), domainId)));
                DomainEntity result = IsmDomainApi.this.em.createQuery(q).getSingleResult();
                return result;
            }
        });
    }

    @Override
    public List<DomainEntity> listDomains() throws Exception {

        List<DomainEntity> domains = new ArrayList<DomainEntity>();

        return this.txControl.supports(new Callable<List<DomainEntity>>() {

            @Override
            public List<DomainEntity> call() throws Exception {
                CriteriaBuilder cb = IsmDomainApi.this.em.getCriteriaBuilder();
                CriteriaQuery<DomainEntity> q = cb.createQuery(DomainEntity.class);
                Root<DomainEntity> r = q.from(DomainEntity.class);
                q.select(r);

                for (DomainEntity domain : IsmDomainApi.this.em.createQuery(q).getResultList()) {
                    DomainEntity domainList = new DomainEntity();
                    domainList.setName(domain.getName());
                    domainList.setId(Long.parseLong(domain.getId()));
                    domains.add(domainList);
                }
                return domains;
            }
        });
    }

}
