package org.osc.manager.ism.entities;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class DeviceMember {
    
    private Long id;
    
    private String name;
    
    private VSSDevice parent;

    @Id
    @GeneratedValue
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

    @ManyToOne(fetch=LAZY)
    @JoinColumn
    public VSSDevice getParent() {
        return parent;
    }

    public void setParent(VSSDevice parent) {
        this.parent = parent;
    }

}
