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
        return createSecurityGroupInterface(this.vs.getMgrId(), sgiElement);
    }

    public String createSecurityGroupInterface(String mgrDeviceId, SecurityGroupInterfaceElement sgiElement)
            throws Exception {

        DeviceEntity device = this.validationUtil.getDeviceOrThrow(mgrDeviceId);
        SecurityGroupInterfaceEntity existingSGI = findSecurityGroupInterfaceEntityByName(sgiElement.getName(), device);

        if (existingSGI != null) {
            throw new IllegalStateException(
                    String.format("Security Group interface with name %s already exists", sgiElement.getName()));
        }

        String mgrSecurityGroupId = sgiElement.getManagerSecurityGroupId();

        @SuppressWarnings("resource")
        SecurityGroupEntity sg = new IsmSecurityGroupApi(this.vs, this.txControl, this.em)
        .getSecurityGroup(mgrDeviceId,mgrSecurityGroupId);
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
        updateSecurityGroupInterface(this.vs.getMgrId(), sgiElement);
    }

    public void updateSecurityGroupInterface(String mgrDeviceId, SecurityGroupInterfaceElement sgiElement)
            throws Exception {

        SecurityGroupInterfaceEntity existingSgi = getSecurityGroupInterface(mgrDeviceId,
                sgiElement.getManagerSecurityGroupInterfaceId());
        if (existingSgi == null) {
            throw new IllegalStateException(String.format("Security group interface with id %s was not found.",
                    sgiElement.getManagerSecurityGroupInterfaceId()));
        }
        existingSgi.setName(sgiElement.getName());
        existingSgi.setPolicies(getPoliciesById(sgiElement));

        @SuppressWarnings("resource")
        SecurityGroupEntity sg = new IsmSecurityGroupApi(this.vs, this.txControl, this.em)
        .getSecurityGroup(mgrDeviceId, sgiElement.getManagerSecurityGroupId());

        existingSgi.setSecurityGroup(sg);
        existingSgi.setTag(sgiElement.getTag());

        this.txControl.required(() -> {
            IsmSecurityGroupInterfaceApi.this.em.merge(existingSgi);
            return null;
        });
    }

    @Override
    public void deleteSecurityGroupInterface(String mgrSecurityGroupInterfaceId) throws Exception {
        deleteSecurityGroupInterface(this.vs.getMgrId(), mgrSecurityGroupInterfaceId);
    }

    public void deleteSecurityGroupInterface(String mgrDeviceId, String id) throws Exception {
        this.txControl.required(() -> {
            SecurityGroupInterfaceEntity existingSgi = getSecurityGroupInterface(mgrDeviceId, id);
            if (existingSgi == null) {
                LOG.warn(String.format("Security group interface with id %s was not found.", id));
                return null;
            }

            IsmSecurityGroupInterfaceApi.this.em.remove(existingSgi);
            return null;
        });
    }

    @Override
    public ManagerSecurityGroupInterfaceElement getSecurityGroupInterfaceById(String id)
            throws Exception {
        return getSecurityGroupInterface(this.vs.getMgrId(), id);
    }

    public ManagerSecurityGroupInterfaceElement getSecurityGroupInterfaceById(String mgrDeviceId, String id)
            throws Exception {
        DeviceEntity device = this.validationUtil.getDeviceOrThrow(mgrDeviceId);

        return getSecurityGroupInterface(mgrDeviceId, id);
    }

    @Override
    public String findSecurityGroupInterfaceByName(String name) throws Exception {

        DeviceEntity device = this.validationUtil.getDeviceOrThrow(this.vs.getMgrId());

        SecurityGroupInterfaceEntity sgi = findSecurityGroupInterfaceEntityByName(name, device);

        return sgi == null ? null : sgi.getId().toString();
    }

    @Override
    public List<? extends ManagerSecurityGroupInterfaceElement> listSecurityGroupInterfaces() throws Exception {
        return listSecurityGroupInterfaces(this.vs.getMgrId());
    }

    public List<? extends ManagerSecurityGroupInterfaceElement> listSecurityGroupInterfaces(String mgrDeviceId)
            throws Exception {

        DeviceEntity device = this.validationUtil.getDeviceOrThrow(mgrDeviceId);

        return this.txControl.supports(() -> {
            CriteriaBuilder cb = IsmSecurityGroupInterfaceApi.this.em.getCriteriaBuilder();
            CriteriaQuery<SecurityGroupInterfaceEntity> query = cb.createQuery(SecurityGroupInterfaceEntity.class);
            Root<SecurityGroupInterfaceEntity> root = query.from(SecurityGroupInterfaceEntity.class);

            query.select(root).where(cb.equal(root.get("device"), device));
            return IsmSecurityGroupInterfaceApi.this.em.createQuery(query).getResultList();
        });
    }

    private SecurityGroupEntity getSecurityGroupbyId(String mgrDeviceId, String mgrSecurityGroupId) throws Exception {
        if (mgrSecurityGroupId == null) {
            return null;
        }
        IsmSecurityGroupApi ismSecurityGroupApi = IsmSecurityGroupApi.create(this.vs, this.txControl, this.em);
        return ismSecurityGroupApi.getSecurityGroup(mgrDeviceId, mgrSecurityGroupId);
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

    private SecurityGroupInterfaceEntity getSecurityGroupInterface(String mgrDeviceId, String mgrSecurityGroupInterfaceId)
            throws Exception {
        return getSecurityGroupInterface(mgrDeviceId, mgrSecurityGroupInterfaceId, null);
    }

    private SecurityGroupInterfaceEntity getSecurityGroupInterface(String mgrDeviceId, String mgrSecurityGroupInterfaceId,
            String name) throws Exception {
        return this.txControl.supports(() -> {

            CriteriaBuilder criteriaBuilder = IsmSecurityGroupInterfaceApi.this.em.getCriteriaBuilder();
            CriteriaQuery<SecurityGroupInterfaceEntity> query = criteriaBuilder
                    .createQuery(SecurityGroupInterfaceEntity.class);
            Root<SecurityGroupInterfaceEntity> r = query.from(SecurityGroupInterfaceEntity.class);

            if (name != null) {
                query.select(r)
                .where(criteriaBuilder.and((criteriaBuilder.equal(r.get("device").get("id"), mgrDeviceId)),
                        (criteriaBuilder.equal(r.get("id"), mgrSecurityGroupInterfaceId)),
                        (criteriaBuilder.equal(r.get("name"), name))));
            } else {
                query.select(r)
                .where(criteriaBuilder.and(
                        (criteriaBuilder.equal(r.get("device").get("id"), mgrDeviceId)),
                        criteriaBuilder.equal(r.get("id"), mgrSecurityGroupInterfaceId)));
            }

            List<SecurityGroupInterfaceEntity> result = IsmSecurityGroupInterfaceApi.this.em.createQuery(query)
                    .getResultList();

            return result.isEmpty() == true ? null : result.get(0);
        });
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
