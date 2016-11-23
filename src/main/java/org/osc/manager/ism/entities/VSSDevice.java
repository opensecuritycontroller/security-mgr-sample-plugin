package org.osc.manager.ism.entities;

import static javax.persistence.FetchType.LAZY;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class VSSDevice {

    private Long id;
    
    private String name;
    
    private List<DeviceMember> deviceMembers;
    
    private List<SecurityGroup> securityGroups;

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, fetch=LAZY, mappedBy="parent")
    public List<DeviceMember> getDeviceMembers() {
        return deviceMembers;
    }

    public void setDeviceMembers(List<DeviceMember> deviceMembers) {
        this.deviceMembers = deviceMembers;
    }

    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, fetch=LAZY, mappedBy="parent")
    public List<SecurityGroup> getSecurityGroups() {
        return securityGroups;
    }
    
    public void setSecurityGroups(List<SecurityGroup> securityGroups) {
        this.securityGroups = securityGroups;
    }
}
