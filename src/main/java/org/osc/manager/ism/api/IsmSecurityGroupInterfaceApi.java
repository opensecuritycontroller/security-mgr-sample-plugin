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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.osc.manager.ism.api.util.ValidationUtil;
import org.osc.manager.ism.entities.DeviceEntity;
import org.osc.manager.ism.entities.PolicyEntity;
import org.osc.manager.ism.entities.SecurityGroupEntity;
import org.osc.manager.ism.entities.SecurityGroupInterfaceEntity;
import org.osc.sdk.manager.api.ManagerSecurityGroupInterfaceApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.ManagerPolicyElement;
import org.osc.sdk.manager.element.ManagerSecurityGroupInterfaceElement;
import org.osc.sdk.manager.element.SecurityGroupInterfaceElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.osgi.service.transaction.control.TransactionControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsmSecurityGroupInterfaceApi implements ManagerSecurityGroupInterfaceApi {

    private static final Logger LOG = LoggerFactory.getLogger(IsmSecurityGroupInterfaceApi.class);

    private final TransactionControl txControl;

    private final EntityManager em;

    private VirtualSystemElement vs;

    private ApplianceManagerConnectorElement mc;

    private ValidationUtil validationUtil;

    public IsmSecurityGroupInterfaceApi(VirtualSystemElement vs, ApplianceManagerConnectorElement mc,
            TransactionControl txControl, EntityManager em) {
        // Set vs as class variable as needed
        this.txControl = txControl;
        this.em = em;
        this.vs = vs;
        this.mc = mc;
        this.validationUtil = new ValidationUtil(txControl, em);
    }

    public static IsmSecurityGroupInterfaceApi create(VirtualSystemElement vs, ApplianceManagerConnectorElement mc,
            TransactionControl txControl, EntityManager em) throws Exception {
        return new IsmSecurityGroupInterfaceApi(vs, mc, txControl, em);
    }

    @Override
    public String createSecurityGroupInterface(SecurityGroupInterfaceElement sgiElement) throws Exception {
        DeviceEntity device = this.validationUtil.getDeviceOrThrow(this.vs.getMgrId());

        SecurityGroupInterfaceEntity existingSGI = findSecurityGroupInterfaceEntityByName(sgiElement.getName(), device);
        if (existingSGI != null) {
            throw new IllegalStateException(
                    String.format("Security Group interface with name %s already exists", sgiElement.getName()));
        }

        String mgrSecurityGroupId = sgiElement.getManagerSecurityGroupId();

        @SuppressWarnings("resource")
        SecurityGroupEntity sg = (SecurityGroupEntity) new IsmSecurityGroupApi(this.vs, this.txControl, this.em)
                .getSecurityGroupById(mgrSecurityGroupId);
        Set<PolicyEntity> policies = getPoliciesById(sgiElement);

        return this.txControl.required(() -> {
            SecurityGroupInterfaceEntity newSGI = new SecurityGroupInterfaceEntity(sgiElement.getName(), policies,
                    sgiElement.getTag(), sg, device);
            IsmSecurityGroupInterfaceApi.this.em.persist(newSGI);
            return newSGI.getSecurityGroupInterfaceId();
        });
    }

    @Override
    public void updateSecurityGroupInterface(SecurityGroupInterfaceElement sgiElement) throws Exception {
        SecurityGroupInterfaceEntity existingSgi = (SecurityGroupInterfaceEntity) getSecurityGroupInterfaceById(
                sgiElement.getManagerSecurityGroupInterfaceId());

        if (existingSgi == null) {
            throw new IllegalStateException(String.format("Security group interface with id %s was not found.",
                    sgiElement.getManagerSecurityGroupInterfaceId()));
        }
        existingSgi.setName(sgiElement.getName());
        existingSgi.setPolicies(getPoliciesById(sgiElement));

        @SuppressWarnings("resource")
        SecurityGroupEntity sg = (SecurityGroupEntity) new IsmSecurityGroupApi(this.vs, this.txControl, this.em)
                .getSecurityGroupById(sgiElement.getManagerSecurityGroupId());

        existingSgi.setSecurityGroup(sg);
        existingSgi.setTag(sgiElement.getTag());

        this.txControl.required(() -> {
            IsmSecurityGroupInterfaceApi.this.em.merge(existingSgi);
            return null;
        });
    }

    @Override
    public void deleteSecurityGroupInterface(String id) throws Exception {
        SecurityGroupInterfaceEntity existingSgi = (SecurityGroupInterfaceEntity) getSecurityGroupInterfaceById(id);
        if (existingSgi == null) {
            LOG.warn(String.format("Security group interface with id %s was not found.", id));
            return;
        }
        this.txControl.required(() -> {
            IsmSecurityGroupInterfaceApi.this.em.remove(existingSgi);
            return null;
        });
    }

    @Override
    public ManagerSecurityGroupInterfaceElement getSecurityGroupInterfaceById(String id) throws Exception {
        DeviceEntity device = this.validationUtil.getDeviceOrThrow(this.vs.getMgrId());

        SecurityGroupInterfaceEntity sgi = getSecurityGroupInterfaceById(id, device);
        return sgi;
    }

    @Override
    public String findSecurityGroupInterfaceByName(String name) throws Exception {
        DeviceEntity device = this.validationUtil.getDeviceOrThrow(this.vs.getMgrId());

        SecurityGroupInterfaceEntity sgi = findSecurityGroupInterfaceEntityByName(name, device);

        return sgi == null ? null : sgi.getId().toString();
    }

    @Override
    public List<? extends ManagerSecurityGroupInterfaceElement> listSecurityGroupInterfaces() throws Exception {
        DeviceEntity device = this.validationUtil.getDeviceOrThrow(this.vs.getMgrId());

        return this.txControl.supports(() -> {
            CriteriaBuilder cb = IsmSecurityGroupInterfaceApi.this.em.getCriteriaBuilder();
            CriteriaQuery<SecurityGroupInterfaceEntity> query = cb.createQuery(SecurityGroupInterfaceEntity.class);
            Root<SecurityGroupInterfaceEntity> root = query.from(SecurityGroupInterfaceEntity.class);

            query.select(root).where(cb.equal(root.get("device"), device));
            return IsmSecurityGroupInterfaceApi.this.em.createQuery(query).getResultList();
        });
    }

    private SecurityGroupInterfaceEntity getSecurityGroupInterfaceById(String id, DeviceEntity device)
            throws Exception {
        return this.txControl.supports(() -> {
            CriteriaBuilder cb = IsmSecurityGroupInterfaceApi.this.em.getCriteriaBuilder();
            CriteriaQuery<SecurityGroupInterfaceEntity> query = cb.createQuery(SecurityGroupInterfaceEntity.class);
            Root<SecurityGroupInterfaceEntity> root = query.from(SecurityGroupInterfaceEntity.class);

            query.select(root).where(cb.equal(root.get("id"), Long.valueOf(id)), cb.equal(root.get("device"), device));

            SecurityGroupInterfaceEntity result = null;
            try {
                result = IsmSecurityGroupInterfaceApi.this.em.createQuery(query).getSingleResult();
            } catch (NoResultException e) {
                LOG.error(String.format("Cannot find Security group interface with id %s under device %s", id,
                        device.getId()));
            }
            return result;
        });
    }

    private SecurityGroupInterfaceEntity findSecurityGroupInterfaceEntityByName(String name, DeviceEntity device) {

        return this.txControl.supports(() -> {
            CriteriaBuilder cb = IsmSecurityGroupInterfaceApi.this.em.getCriteriaBuilder();
            CriteriaQuery<SecurityGroupInterfaceEntity> query = cb.createQuery(SecurityGroupInterfaceEntity.class);
            Root<SecurityGroupInterfaceEntity> root = query.from(SecurityGroupInterfaceEntity.class);

            query.select(root).where(cb.equal(root.get("name"), name), cb.equal(root.get("device"), device));

            SecurityGroupInterfaceEntity result = null;
            try {
                result = IsmSecurityGroupInterfaceApi.this.em.createQuery(query).getSingleResult();
            } catch (Exception e) {
                LOG.error(String.format("Cannot find Security group interface with name %s under device %s", name,
                        device.getId()));
            }
            return result;
        });
    }

    private Set<PolicyEntity> getPoliciesById(SecurityGroupInterfaceElement sgiElement) throws Exception {
        // TODO: Validate policies exist in the db
        Set<PolicyEntity> policies = new HashSet<>();
        IsmPolicyApi ismPolicyApi = IsmPolicyApi.create(this.mc, this.txControl, this.em);
        for (ManagerPolicyElement mgrPolicyElement : sgiElement.getManagerPolicyElements()) {
            policies.add(ismPolicyApi.getPolicy(mgrPolicyElement.getId(), mgrPolicyElement.getDomainId()));
        }
        if (policies.isEmpty()) {
            throw new IllegalStateException("Cannot find policies");
        }
        return policies;
    }

    @Override
    public void close() {
        LOG.info("Closing connection to the database");
        this.txControl.required(() -> {
            this.em.close();
            return null;
        });
    }
}
