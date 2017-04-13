package org.osc.manager.ism.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class SecurityGroupInterface {
    private Long id;

    private String name;

    private String policyId;

    private String tag;
    
    SecurityGroupInterface(){
    }

    public SecurityGroupInterface(String name, String policyId, String tag) {
        this.name = name;
        this.policyId = policyId;
        this.tag = tag;
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPolicyId() {
        return this.policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}