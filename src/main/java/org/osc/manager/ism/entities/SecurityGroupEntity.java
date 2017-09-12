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
package org.osc.manager.ism.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.osc.sdk.manager.element.ManagerSecurityGroupElement;

@Entity
@Table(name = "SECURITY_GROUP")
public class SecurityGroupEntity implements ManagerSecurityGroupElement {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@Column(name = "name", unique = true, nullable = false)
	private String name;

	@OneToOne(mappedBy = "securityGroup", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private SecurityGroupInterfaceEntity securityGroupInterfaces;

	public SecurityGroupEntity() {
	}

	public SecurityGroupEntity(String name) {
		this.name = name;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SecurityGroupInterfaceEntity getSecurityGroupInterfaces() {
		return this.securityGroupInterfaces;
	}

	public void setSecurityGroupInterfaces(SecurityGroupInterfaceEntity securityGroupInterfaces) {
		this.securityGroupInterfaces = securityGroupInterfaces;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getSGId() {
		return getId() == null ? null : getId().toString();
	}

}
