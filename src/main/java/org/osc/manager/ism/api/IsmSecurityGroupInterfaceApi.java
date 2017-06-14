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
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.osc.manager.ism.entities.SecurityGroupInterface;
import org.osc.sdk.manager.api.ManagerSecurityGroupInterfaceApi;
import org.osc.sdk.manager.element.ManagerSecurityGroupInterfaceElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.osgi.service.transaction.control.TransactionControl;

public class IsmSecurityGroupInterfaceApi implements ManagerSecurityGroupInterfaceApi {

    private static final Logger LOGGER = Logger.getLogger(IsmSecurityGroupInterfaceApi.class);
    private static final String SGI_NOT_FOUND_MESSAGE = "A security group interface with id %s was not found.";

    private final TransactionControl txControl;

    private final EntityManager em;

    public IsmSecurityGroupInterfaceApi(VirtualSystemElement vs, TransactionControl txControl, EntityManager em) {
        // Set vs as class variable as needed
        this.txControl = txControl;
        this.em = em;
    }

    @Override
    public String createSecurityGroupInterface(String name, String policyId, String tag) throws Exception {
        String existingSGIId = findSecurityGroupInterfaceByName(name);
        if (existingSGIId != null) {
            return existingSGIId;
        }

        return this.txControl.supports(new Callable<String>() {
            @Override
            public String call() throws Exception {
                SecurityGroupInterface newSGI = new SecurityGroupInterface(name, policyId, tag);
                IsmSecurityGroupInterfaceApi.this.em.persist(newSGI);
                if (newSGI.getId() == null) {
                    String message = String.format("The identifier of the created security group interface with name %s should not be null.", name);
                    LOGGER.error(message);
                    throw new IllegalStateException(message);
                }

                return newSGI.getId().toString();
            }
        });
    }

    @Override
    public void updateSecurityGroupInterface(String id, String name, String policyId, String tag) throws Exception {
        SecurityGroupInterface existingSgi = getSecurityGroupInterface(id);

        if (existingSgi == null) {
            String message = String.format(SGI_NOT_FOUND_MESSAGE, id);
            LOGGER.error(message);
            throw new EntityNotFoundException(message);
        }

        existingSgi.setName(name);
        existingSgi.setPolicyId(policyId);
        existingSgi.setTag(tag);

        this.txControl.supports(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                IsmSecurityGroupInterfaceApi.this.em.merge(existingSgi);
                return null;
            }
        });
    }

    @Override
    public void deleteSecurityGroupInterface(String id) throws Exception {
        SecurityGroupInterface existingSgi = getSecurityGroupInterface(id);

        if (existingSgi == null) {
            LOGGER.info(String.format(SGI_NOT_FOUND_MESSAGE, id));
            return;
        }

        this.txControl.supports(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                IsmSecurityGroupInterfaceApi.this.em.remove(existingSgi);
                return null;
            }
        });
    }

    @Override
    public ManagerSecurityGroupInterfaceElement getSecurityGroupInterfaceById(final String id) throws Exception {
        SecurityGroupInterface sgi = getSecurityGroupInterface(id);
        return sgi == null ? null : new org.osc.manager.ism.model.SecurityGroupInterface(sgi.getId(),
                sgi.getName(), sgi.getPolicyId(), sgi.getTag());
    }

    private SecurityGroupInterface getSecurityGroupInterface(final String id) throws Exception {
        return this.txControl.supports(new Callable<SecurityGroupInterface>() {

            @Override
            public SecurityGroupInterface call() throws Exception {
                return IsmSecurityGroupInterfaceApi.this.em.find(SecurityGroupInterface.class, Long.valueOf(id));
            }
        });
    }

    @Override
    public String findSecurityGroupInterfaceByName(final String name) throws Exception {
        return this.txControl.supports(new Callable<String>() {

            @Override
            public String call() throws Exception {
                CriteriaBuilder cb = IsmSecurityGroupInterfaceApi.this.em.getCriteriaBuilder();

                CriteriaQuery<Long> q = cb.createQuery(Long.class);
                Root<SecurityGroupInterface> r = q.from(SecurityGroupInterface.class);
                q.select(r.get("id").as(Long.class)).where(cb.equal(r.get("name"), name));

                Long result = null;
                try {
                    result = IsmSecurityGroupInterfaceApi.this.em.createQuery(q).getSingleResult();
                } catch (Exception e) {
                    LOGGER.error("Finding sg result in", e);
                }

                return result == null ? null : result.toString();
            }
        });
    }

    @Override
    public List<? extends ManagerSecurityGroupInterfaceElement> listSecurityGroupInterfaces() throws Exception {
        return this.txControl.supports(new Callable<List<? extends ManagerSecurityGroupInterfaceElement>>() {

            @Override
            public List<? extends ManagerSecurityGroupInterfaceElement> call() throws Exception {
                CriteriaBuilder cb = IsmSecurityGroupInterfaceApi.this.em.getCriteriaBuilder();

                CriteriaQuery<SecurityGroupInterface> q = cb.createQuery(SecurityGroupInterface.class);
                Root<SecurityGroupInterface> r = q.from(SecurityGroupInterface.class);
                q.select(r);

                List<org.osc.manager.ism.model.SecurityGroupInterface> devices = new ArrayList<>();
                for (SecurityGroupInterface sg : IsmSecurityGroupInterfaceApi.this.em.createQuery(q).getResultList()) {
                    devices.add(new org.osc.manager.ism.model.SecurityGroupInterface(sg.getId(),
                            sg.getName(), sg.getPolicyId(), sg.getTag()));
                }

                return devices;
            }
        });
    }

    @Override
    public void close() {
    }
}
