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

import org.osc.manager.ism.entities.PolicyEntity;
import org.osc.manager.ism.entities.SecurityGroupEntity;
import org.osc.manager.ism.entities.SecurityGroupInterfaceEntity;
import org.slf4j.LoggerFactory;
import org.osc.sdk.manager.api.ManagerSecurityGroupInterfaceApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.ManagerPolicyElement;
import org.osc.sdk.manager.element.ManagerSecurityGroupInterfaceElement;
import org.osc.sdk.manager.element.SecurityGroupInterfaceElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.osgi.service.transaction.control.TransactionControl;
import org.slf4j.Logger;

public class IsmSecurityGroupInterfaceApi implements ManagerSecurityGroupInterfaceApi {

	private static final Logger LOG = LoggerFactory.getLogger(IsmSecurityGroupInterfaceApi.class);
	private static final String SGI_NOT_FOUND_MESSAGE = "A security group interface with id %s was not found.";

	private final TransactionControl txControl;

	private final EntityManager em;

	private VirtualSystemElement vs;

	private ApplianceManagerConnectorElement mc;

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
		String existingSGIId = findSecurityGroupInterfaceByName(sgiElement.getName());
		if (existingSGIId != null) {
			return existingSGIId;
		}
		SecurityGroupEntity sg = getSecurityGroupbyId(sgiElement.getManagerSecurityGroupId());
		Set<PolicyEntity> policies = getPoliciesById(sgiElement);
		return this.txControl.required(new Callable<String>() {
			@Override
			public String call() throws Exception {
				SecurityGroupInterfaceEntity newSGI = new SecurityGroupInterfaceEntity(sgiElement.getName(), policies,
						sgiElement.getTag(), sg);
				IsmSecurityGroupInterfaceApi.this.em.persist(newSGI);
				return newSGI.getSecurityGroupInterfaceId();
			}
		});
	}

	@Override
	public void updateSecurityGroupInterface(SecurityGroupInterfaceElement sgiElement) throws Exception {
		SecurityGroupInterfaceEntity existingSgi = getSecurityGroupInterface(
				sgiElement.getManagerSecurityGroupInterfaceId());
		if (existingSgi == null) {
			String message = String.format(SGI_NOT_FOUND_MESSAGE, sgiElement.getManagerSecurityGroupInterfaceId());
			throw new Exception(message);
		}
		existingSgi.setName(sgiElement.getName());
		existingSgi.setPolicies(getPoliciesById(sgiElement));
		existingSgi.setSecurityGroup(getSecurityGroupbyId(sgiElement.getManagerSecurityGroupId()));
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
		SecurityGroupInterfaceEntity existingSgi = getSecurityGroupInterface(id);
		if (existingSgi == null) {
			LOG.info(String.format(SGI_NOT_FOUND_MESSAGE, id));
			return;
		}
		this.txControl.required(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				IsmSecurityGroupInterfaceApi.this.em.remove(existingSgi);
				return null;
			}
		});
	}

	@Override
	public ManagerSecurityGroupInterfaceElement getSecurityGroupInterfaceById(String id) throws Exception {
		SecurityGroupInterfaceEntity sgi = getSecurityGroupInterface(id);
		return sgi;
	}

	private SecurityGroupInterfaceEntity getSecurityGroupInterface(String id) throws Exception {
		if (id == null) {
			return null;
		}
		return this.txControl.supports(new Callable<SecurityGroupInterfaceEntity>() {
			@Override
			public SecurityGroupInterfaceEntity call() throws Exception {
				return IsmSecurityGroupInterfaceApi.this.em.find(SecurityGroupInterfaceEntity.class, Long.valueOf(id));
			}
		});
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
					LOG.error("Finding sg result in", e);
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
				CriteriaBuilder criteriaBuilder = IsmSecurityGroupInterfaceApi.this.em.getCriteriaBuilder();
				CriteriaQuery<SecurityGroupInterfaceEntity> query = criteriaBuilder
						.createQuery(SecurityGroupInterfaceEntity.class);
				Root<SecurityGroupInterfaceEntity> r = query.from(SecurityGroupInterfaceEntity.class);
				query.select(r);
				return IsmSecurityGroupInterfaceApi.this.em.createQuery(query).getResultList();
			}
		});
	}

	private SecurityGroupEntity getSecurityGroupbyId(String sgId) throws Exception {
		IsmSecurityGroupApi ismSecurityGroupApi = IsmSecurityGroupApi.create(this.vs, this.txControl, this.em);
		SecurityGroupEntity result = ismSecurityGroupApi.getSecurityGroup(sgId);
		if (result == null) {
			throw new Exception(String.format("Security Group with %s not found", sgId));
		}
		return result;
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

	@Override
	public void close() {
		LOG.info("Closing connection to the database");
		this.txControl.required(() -> {
			this.em.close();
			return null;
		});
	}
}
