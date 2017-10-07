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

import org.osc.manager.ism.entities.DeviceEntity;
import org.osc.manager.ism.entities.SecurityGroupEntity;
import org.osc.sdk.manager.api.ManagerSecurityGroupApi;
import org.osc.sdk.manager.element.ManagerSecurityGroupElement;
import org.osc.sdk.manager.element.SecurityGroupMemberListElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.osgi.service.transaction.control.TransactionControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsmSecurityGroupApi implements ManagerSecurityGroupApi {

    private static final Logger LOG = LoggerFactory.getLogger(IsmSecurityGroupApi.class);
    private VirtualSystemElement vs;
    private TransactionControl txControl;
    private EntityManager em;
    private static final String SG_NOT_FOUND_MESSAGE = "A security group with id %s was not found.";
    private IsmDeviceApi api;

    public IsmSecurityGroupApi(VirtualSystemElement vs, TransactionControl txControl, EntityManager em) {
        super();
        this.vs = vs;
        this.txControl = txControl;
        this.em = em;
    }

    public static IsmSecurityGroupApi create(VirtualSystemElement vs, TransactionControl txControl, EntityManager em)
            throws Exception {
        return new IsmSecurityGroupApi(vs, txControl, em);
    }

    @Override
    public String createSecurityGroup(String name, String oscSgId, SecurityGroupMemberListElement memberList)
            throws Exception {
        // TODO : SUDHIR - Please handle the memberList
        return createSecurityGroup(IsmSecurityGroupApi.this.vs.getMgrId(), name, oscSgId, memberList);
    }

    public String createSecurityGroup(String mgrDeviceId, String name, String oscSgId,
            SecurityGroupMemberListElement memberList)
                    throws Exception {
        this.api = new IsmDeviceApi(null, this.txControl, this.em);
        DeviceEntity dev = (DeviceEntity) this.api.getDeviceById(mgrDeviceId);
        if (dev == null) {
            String msg = String.format("Cannot find the device id: %s\n", mgrDeviceId);
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }

        SecurityGroupEntity existingSG = findSecurityGroupByName(name);
        if (existingSG != null) {
            throw new IllegalArgumentException(String.format("Security Group with name %s already exists", name));
        }
        return this.txControl.required(new Callable<String>() {
            @Override
            public String call() throws Exception {
                SecurityGroupEntity newSG = new SecurityGroupEntity(name);
                newSG.setDevice(dev);
                IsmSecurityGroupApi.this.em.persist(newSG);
                return newSG.getSGId();
            }
        });
    }

    @Override
    public void updateSecurityGroup(String mgrSecurityGroupId, String name, SecurityGroupMemberListElement memberList)
            throws Exception {
        updateSecurityGroup(this.vs.getMgrId(), mgrSecurityGroupId, name, memberList);
    }

    public void updateSecurityGroup(String mgrDeviceId, String mgrSecurityGroupId, String name,
            SecurityGroupMemberListElement memberList) throws Exception {
        SecurityGroupEntity existingSg = getSecurityGroup(mgrDeviceId, mgrSecurityGroupId);
        if (existingSg == null) {
            String message = String.format(SG_NOT_FOUND_MESSAGE, mgrSecurityGroupId);
            throw new IllegalArgumentException(message);
        }
        existingSg.setName(name);
        this.txControl.required(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                IsmSecurityGroupApi.this.em.merge(existingSg);
                return null;
            }
        });
    }

    @Override
    public void deleteSecurityGroup(String mgrSecurityGroupId) throws Exception {
        deleteSecurityGroup(IsmSecurityGroupApi.this.vs.getMgrId(), mgrSecurityGroupId);
    }

    public void deleteSecurityGroup(String mgrDeviceId, String mgrSecurityGroupId) throws Exception {
        this.txControl.required(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SecurityGroupEntity existingSg = getSecurityGroup(mgrDeviceId, mgrSecurityGroupId);
                if (existingSg == null) {
                    LOG.warn(String.format(SG_NOT_FOUND_MESSAGE, mgrSecurityGroupId));
                    return null;
                }
                IsmSecurityGroupApi.this.em.remove(existingSg);
                return null;
            }
        });
    }

    @Override
    public List<? extends ManagerSecurityGroupElement> getSecurityGroupList() throws Exception {
        return getSecurityGroupList(this.vs.getMgrId());
    }

    public List<? extends ManagerSecurityGroupElement> getSecurityGroupList(String mgrDeviceId) throws Exception {
        return this.txControl.supports(new Callable<List<? extends ManagerSecurityGroupElement>>() {
            @Override
            public List<? extends ManagerSecurityGroupElement> call() throws Exception {
                CriteriaBuilder criteriaBuilder = IsmSecurityGroupApi.this.em.getCriteriaBuilder();
                CriteriaQuery<SecurityGroupEntity> query = criteriaBuilder.createQuery(SecurityGroupEntity.class);
                Root<SecurityGroupEntity> r = query.from(SecurityGroupEntity.class);
                query.select(r)
                .where(criteriaBuilder.and(criteriaBuilder.equal(r.get("device").get("id"), mgrDeviceId)));
                return IsmSecurityGroupApi.this.em.createQuery(query).getResultList();
            }
        });
    }

    @Override
    public ManagerSecurityGroupElement getSecurityGroupById(String mgrSecurityGroupId) throws Exception {
        return getSecurityGroup(this.vs.getMgrId(), mgrSecurityGroupId);
    }

    public SecurityGroupEntity getSecurityGroup(String mgrDeviceId, String mgrSecurityGroupId) throws Exception {
        if (mgrSecurityGroupId == null) {
            return null;
        }
        return this.txControl.supports(new Callable<SecurityGroupEntity>() {
            @Override
            public SecurityGroupEntity call() throws Exception {
                CriteriaBuilder criteriaBuilder = IsmSecurityGroupApi.this.em.getCriteriaBuilder();
                CriteriaQuery<SecurityGroupEntity> query = criteriaBuilder.createQuery(SecurityGroupEntity.class);
                Root<SecurityGroupEntity> r = query.from(SecurityGroupEntity.class);
                query.select(r)
                .where(criteriaBuilder.and((criteriaBuilder.equal(r.get("device").get("id"), mgrDeviceId)),
                        criteriaBuilder.equal(r.get("id"), mgrSecurityGroupId)));
                List<SecurityGroupEntity> result = IsmSecurityGroupApi.this.em.createQuery(query).getResultList();
                return result.isEmpty() == true ? null : result.get(0);
            }
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

    private SecurityGroupEntity findSecurityGroupByName(String name) throws Exception {
        return this.txControl.supports(new Callable<SecurityGroupEntity>() {
            @Override
            public SecurityGroupEntity call() throws Exception {
                CriteriaBuilder criteriaBuilder  = IsmSecurityGroupApi.this.em.getCriteriaBuilder();
                CriteriaQuery<SecurityGroupEntity> query = criteriaBuilder .createQuery(SecurityGroupEntity.class);
                Root<SecurityGroupEntity> r = query.from(SecurityGroupEntity.class);
                query.select(r).where(criteriaBuilder .equal(r.get("name"), name));
                SecurityGroupEntity result = null;
                try {
                    result = IsmSecurityGroupApi.this.em.createQuery(query).getSingleResult();
                } catch (Exception e) {
                    LOG.error("Finding sg result in", e);
                }
                return result;
            }
        });
    }
}
