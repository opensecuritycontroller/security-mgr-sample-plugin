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
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
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

public class IsmSecurityGroupInterfaceApi implements ManagerSecurityGroupInterfaceApi {

    private static Logger LOGGER = Logger.getLogger(IsmSecurityGroupInterfaceApi.class);
    private static String SGI_NOT_FOUND_MESSAGE = "A security group interface with id %s was not found.";
    private TransactionControl txControl;
    private EntityManager em;
    private VirtualSystemElement vs;
    private ApplianceManagerConnectorElement mc;
    private IsmDeviceApi api;

    public IsmSecurityGroupInterfaceApi(VirtualSystemElement vs, ApplianceManagerConnectorElement mc,
            TransactionControl txControl, EntityManager em) {
        // Set vs as class variable as needed
        this.txControl = txControl;
        this.em = em;
        this.vs = vs;
        this.mc = mc;
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

        this.api = new IsmDeviceApi(null, this.txControl, this.em);
        DeviceEntity dev = (DeviceEntity) this.api.getDeviceById(mgrDeviceId);
        if (dev == null) {
            String msg = String.format("Cannot find the device id: %s\n", mgrDeviceId);
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
        String existingSGIId = findSecurityGroupInterfaceByName(sgiElement.getName());
        if (existingSGIId != null) {
            return existingSGIId;
        }

        Set<PolicyEntity> policies = getPoliciesById(sgiElement);
        return this.txControl.required(new Callable<String>() {
            @Override
            public String call() throws Exception {

                SecurityGroupEntity sg = getSecurityGroupbyId(mgrDeviceId, sgiElement.getManagerSecurityGroupId());
                SecurityGroupInterfaceEntity sgi = getSecurityGroupInterface(mgrDeviceId,
                        sgiElement.getManagerSecurityGroupId(), sgiElement.getName());
                if (sgi != null) {
                    String msg = String.format("SGI already exists name: %s\n", sgiElement.getName());
                    LOGGER.error(msg);
                    throw new IllegalArgumentException(msg);
                }
                SecurityGroupInterfaceEntity newSGI = new SecurityGroupInterfaceEntity(sgiElement.getName(), policies,
                        sgiElement.getTag(), sg);
                newSGI.setDevice(dev);
                IsmSecurityGroupInterfaceApi.this.em.persist(newSGI);
                return newSGI.getSecurityGroupInterfaceId();
            }
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
            String message = String.format(SGI_NOT_FOUND_MESSAGE, sgiElement.getManagerSecurityGroupInterfaceId());
            throw new IllegalArgumentException(message);
        }
        SecurityGroupInterfaceEntity sgi = getSecurityGroupInterface(mgrDeviceId,
                sgiElement.getManagerSecurityGroupId(), sgiElement.getName());
        if (sgi != null) {
            String msg = String.format("SGI already exists name: %s\n", sgiElement.getName());
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
        existingSgi.setName(sgiElement.getName());
        existingSgi.setPolicies(getPoliciesById(sgiElement));
        existingSgi.setSecurityGroup(getSecurityGroupbyId(mgrDeviceId, sgiElement.getManagerSecurityGroupId()));
        existingSgi.setTag(sgiElement.getTag());
        this.txControl.required(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                IsmSecurityGroupInterfaceApi.this.em.merge(existingSgi);
                return null;
            }
        });
    }

    @Override
    public void deleteSecurityGroupInterface(String id) throws Exception {
        deleteSecurityGroupInterface(this.vs.getMgrId(), id);
    }

    public void deleteSecurityGroupInterface(String mgrDeviceId, String mgrSecurityGroupId) throws Exception {
        this.txControl.required(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SecurityGroupInterfaceEntity existingSgi = getSecurityGroupInterface(mgrDeviceId, mgrSecurityGroupId);
                if (existingSgi == null) {
                    LOGGER.warn(String.format(SGI_NOT_FOUND_MESSAGE, mgrSecurityGroupId));
                    return null;
                }
                IsmSecurityGroupInterfaceApi.this.em.remove(existingSgi);
                return null;
            }
        });
    }

    @Override
    public ManagerSecurityGroupInterfaceElement getSecurityGroupInterfaceById(String mgrSecurityGroupId)
            throws Exception {
        return getSecurityGroupInterface(this.vs.getMgrId(), mgrSecurityGroupId);
    }

    public ManagerSecurityGroupInterfaceElement getSecurityGroupInterfaceById(String mgrDeviceId,
            String mgrSecurityGroupId)
                    throws Exception {
        return getSecurityGroupInterface(mgrDeviceId, mgrSecurityGroupId);
    }

    @Override
    public String findSecurityGroupInterfaceByName(String name) throws Exception {
        return this.txControl.supports(new Callable<String>() {
            @Override
            public String call() throws Exception {
                CriteriaBuilder criteriaBuilder = IsmSecurityGroupInterfaceApi.this.em.getCriteriaBuilder();
                CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
                Root<SecurityGroupInterfaceEntity> r = query.from(SecurityGroupInterfaceEntity.class);
                query.select(r.get("id").as(Long.class)).where(criteriaBuilder.equal(r.get("name"), name));
                Long result = null;
                try {
                    result = IsmSecurityGroupInterfaceApi.this.em.createQuery(query).getSingleResult();
                } catch (Exception e) {
                    LOGGER.error("Finding sg result in", e);
                }
                return result == null ? null : result.toString();
            }
        });
    }

    @Override
    public List<? extends ManagerSecurityGroupInterfaceElement> listSecurityGroupInterfaces() throws Exception {
        return listSecurityGroupInterfaces(this.vs.getMgrId());
    }

    public List<? extends ManagerSecurityGroupInterfaceElement> listSecurityGroupInterfaces(String mgrDeviceId)
            throws Exception {
        return this.txControl.supports(new Callable<List<? extends ManagerSecurityGroupInterfaceElement>>() {
            @Override
            public List<? extends ManagerSecurityGroupInterfaceElement> call() throws Exception {
                CriteriaBuilder criteriaBuilder = IsmSecurityGroupInterfaceApi.this.em.getCriteriaBuilder();
                CriteriaQuery<SecurityGroupInterfaceEntity> query = criteriaBuilder
                        .createQuery(SecurityGroupInterfaceEntity.class);
                Root<SecurityGroupInterfaceEntity> r = query.from(SecurityGroupInterfaceEntity.class);
                query.select(r).where(criteriaBuilder.equal(r.get("device").get("id"), mgrDeviceId));
                return IsmSecurityGroupInterfaceApi.this.em.createQuery(query).getResultList();
            }
        });
    }

    @Override
    public void close() {
        LOGGER.info("Closing connection to the database");
        this.txControl.required(() -> {
            this.em.close();
            return null;
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
        Set<PolicyEntity> policies = new HashSet<>();
        IsmPolicyApi ismPolicyApi = IsmPolicyApi.create(this.mc, this.txControl, this.em);
        for (ManagerPolicyElement mgrPolicyElement : sgiElement.getManagerPolicyElements()) {
            policies.add(ismPolicyApi.getPolicy(mgrPolicyElement.getId(), mgrPolicyElement.getDomainId()));
        }
        if (policies.isEmpty()) {
            throw new Exception("No policies found");
        }
        return policies;
    }

    private SecurityGroupInterfaceEntity getSecurityGroupInterface(String mgrDeviceId, String mgrSecurityGroupId)
            throws Exception {
        return getSecurityGroupInterface(mgrDeviceId, mgrSecurityGroupId, null);
    }

    private SecurityGroupInterfaceEntity getSecurityGroupInterface(String mgrDeviceId, String mgrSecurityGroupId,
            String name) throws Exception {
        return this.txControl.supports(new Callable<SecurityGroupInterfaceEntity>() {

            @Override
            public SecurityGroupInterfaceEntity call() throws Exception {
                CriteriaBuilder criteriaBuilder = IsmSecurityGroupInterfaceApi.this.em.getCriteriaBuilder();
                CriteriaQuery<SecurityGroupInterfaceEntity> query = criteriaBuilder
                        .createQuery(SecurityGroupInterfaceEntity.class);
                Root<SecurityGroupInterfaceEntity> r = query.from(SecurityGroupInterfaceEntity.class);
                if (name != null) {
                    query.select(r)
                    .where(criteriaBuilder.and((criteriaBuilder.equal(r.get("device").get("id"), mgrDeviceId)),
                            (criteriaBuilder.equal(r.get("id"), mgrSecurityGroupId)),
                            (criteriaBuilder.equal(r.get("name"), name))));
                } else {
                    query.select(r)
                    .where(criteriaBuilder.and(
                            (criteriaBuilder.equal(r.get("device").get("id"), mgrDeviceId)),
                            criteriaBuilder.equal(r.get("id"), mgrSecurityGroupId)));
                }
                List<SecurityGroupInterfaceEntity> result = IsmSecurityGroupInterfaceApi.this.em.createQuery(query)
                        .getResultList();
                return result.isEmpty() == true ? null : result.get(0);
            }
        });
    }
}
