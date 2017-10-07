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
import org.slf4j.LoggerFactory;
import org.osc.sdk.manager.api.ManagerDomainApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osgi.service.transaction.control.TransactionControl;
import org.slf4j.Logger;

public class IsmDomainApi implements ManagerDomainApi {

    private static final Logger LOG = LoggerFactory.getLogger(IsmDomainApi.class);
    private TransactionControl txControl;
    private EntityManager em;
    ApplianceManagerConnectorElement mc;

    private IsmDomainApi(ApplianceManagerConnectorElement mc, TransactionControl txControl, EntityManager em)
            throws Exception {
        this.txControl = txControl;
        this.em = em;
        this.mc = mc;
    }

    public static IsmDomainApi create(ApplianceManagerConnectorElement mc, TransactionControl txControl,
            EntityManager em) throws Exception {

        return new IsmDomainApi(mc, txControl, em);
    }

    @Override
    public DomainEntity getDomain(String domainId) throws Exception {

        return this.txControl.supports(new Callable<DomainEntity>() {
            @Override
            public DomainEntity call() throws Exception {

                return IsmDomainApi.this.em.find(DomainEntity.class, Long.parseLong(domainId));
            }
        });
    }

    @Override
    public List<DomainEntity> listDomains() throws Exception {

        return this.txControl.supports(new Callable<List<DomainEntity>>() {

            @Override
            public List<DomainEntity> call() throws Exception {
                CriteriaBuilder criteriaBuilder = IsmDomainApi.this.em.getCriteriaBuilder();
                CriteriaQuery<DomainEntity> query = criteriaBuilder.createQuery(DomainEntity.class);
                Root<DomainEntity> r = query.from(DomainEntity.class);
                query.select(r);
                return IsmDomainApi.this.em.createQuery(query).getResultList();
            }
        });
    }
}
