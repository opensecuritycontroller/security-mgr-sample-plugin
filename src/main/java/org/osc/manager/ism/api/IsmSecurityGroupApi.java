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

import org.apache.log4j.Logger;
import org.osc.manager.ism.entities.SecurityGroupEntity;
import org.osc.sdk.manager.api.ManagerSecurityGroupApi;
import org.osc.sdk.manager.element.ManagerSecurityGroupElement;
import org.osc.sdk.manager.element.SecurityGroupMemberListElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.osgi.service.transaction.control.TransactionControl;

public class IsmSecurityGroupApi implements ManagerSecurityGroupApi {

	private static final Logger LOGGER = Logger.getLogger(IsmSecurityGroupApi.class);
	private VirtualSystemElement vs;

	private final TransactionControl txControl;
	private final EntityManager em;

	private static final String SG_NOT_FOUND_MESSAGE = "A security group with id %s was not found.";

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
		// TODO Sudhir: Please handle the memberList
		SecurityGroupEntity existingSG = findSecurityGroupByName(name);
		if (existingSG != null) {
			throw new Exception(String.format("Security Group with name %s already exists", name));
		}
		return this.txControl.required(new Callable<String>() {
			@Override
			public String call() throws Exception {
				SecurityGroupEntity newSG = new SecurityGroupEntity(name);
				IsmSecurityGroupApi.this.em.persist(newSG);
				return newSG.getSGId();
			}
		});
	}

	@Override
	public void updateSecurityGroup(String mgrSecurityGroupId, String name, SecurityGroupMemberListElement memberList)
			throws Exception {
		SecurityGroupEntity existingSg = getSecurityGroup(mgrSecurityGroupId);
		if (existingSg == null) {
			String message = String.format(SG_NOT_FOUND_MESSAGE, mgrSecurityGroupId);
			throw new Exception(message);
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
		SecurityGroupEntity existingSg = getSecurityGroup(mgrSecurityGroupId);
		if (existingSg == null) {
			throw new Exception(String.format(SG_NOT_FOUND_MESSAGE, mgrSecurityGroupId));
		}
		this.txControl.required(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				IsmSecurityGroupApi.this.em.remove(existingSg);
				return null;
			}
		});
	}

	@Override
	public List<? extends ManagerSecurityGroupElement> getSecurityGroupList() throws Exception {
		return this.txControl.supports(new Callable<List<? extends ManagerSecurityGroupElement>>() {
			@Override
			public List<? extends ManagerSecurityGroupElement> call() throws Exception {
				CriteriaBuilder criteriaBuilder  = IsmSecurityGroupApi.this.em.getCriteriaBuilder();
				CriteriaQuery<SecurityGroupEntity> query = criteriaBuilder .createQuery(SecurityGroupEntity.class);
				Root<SecurityGroupEntity> r = query.from(SecurityGroupEntity.class);
				query.select(r);
				return IsmSecurityGroupApi.this.em.createQuery(query).getResultList();
			}
		});
	}

	@Override
	public ManagerSecurityGroupElement getSecurityGroupById(String mgrSecurityGroupId) throws Exception {
		SecurityGroupEntity sg = getSecurityGroup(mgrSecurityGroupId);
		return sg;
	}

	public SecurityGroupEntity getSecurityGroup(final String mgrSecurityGroupId) throws Exception {
		if (mgrSecurityGroupId == null) {
			return null;
		}
		return this.txControl.supports(new Callable<SecurityGroupEntity>() {
			@Override
			public SecurityGroupEntity call() throws Exception {
				return IsmSecurityGroupApi.this.em.find(SecurityGroupEntity.class, Long.valueOf(mgrSecurityGroupId));
			}
		});
	}

	private SecurityGroupEntity findSecurityGroupByName(final String name) throws Exception {
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
					LOGGER.error("Finding sg result in", e);
				}
				return result == null ? null : result;
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

}
