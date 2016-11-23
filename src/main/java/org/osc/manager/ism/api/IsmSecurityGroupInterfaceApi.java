package org.osc.manager.ism.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.osc.manager.ism.entities.SecurityGroup;
import org.osc.manager.ism.model.SecurityGroupInterface;
import org.osc.sdk.manager.api.ManagerSecurityGroupInterfaceApi;
import org.osc.sdk.manager.element.ManagerSecurityGroupInterfaceElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.osgi.service.transaction.control.TransactionControl;

public class IsmSecurityGroupInterfaceApi implements ManagerSecurityGroupInterfaceApi {

    Logger log = Logger.getLogger(IsmSecurityGroupInterfaceApi.class);

    private final VirtualSystemElement vs;
    
    private final TransactionControl txControl;
    
    private final EntityManager em;

    public IsmSecurityGroupInterfaceApi(VirtualSystemElement vs, TransactionControl txControl, EntityManager em) {
        this.vs = vs;
        this.txControl = txControl;
        this.em = em;
    }

    @Override
    public String createSecurityGroupInterface(String name, String policyId, String tag) throws Exception {
        return findSecurityGroupInterfaceByName(name);
    }

    @Override
    public void updateSecurityGroupInterface(String securityGroupId, String name, String policyId,
            String serviceProfileId) throws Exception {
    }

    @Override
    public void deleteSecurityGroupInterface(String id) throws Exception {
    }

    @Override
    public ManagerSecurityGroupInterfaceElement getSecurityGroupInterfaceById(final String id) throws Exception {
        return txControl.supports(new Callable<ManagerSecurityGroupInterfaceElement>() {
            
            @Override
            public ManagerSecurityGroupInterfaceElement call() throws Exception {
                SecurityGroup result = em.find(SecurityGroup.class, Long.valueOf(id));
                return result == null ? null : new SecurityGroupInterface(result.getId(), 
                        result.getName(), result.getPolicyId(), result.getTag());
            }
        });
    }

    @Override
    public String findSecurityGroupInterfaceByName(final String name) throws Exception {
        return txControl.supports(new Callable<String>() {
            
            @Override
            public String call() throws Exception {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                
                CriteriaQuery<Long> q = cb.createQuery(Long.class);
                Root<SecurityGroup> r = q.from(SecurityGroup.class);
                q.select(r.get("id").as(Long.class)).where(cb.equal(r.get("name"), name));
                
                Long result = em.createQuery(q).getSingleResult();
                return result == null ? null : result.toString();
            }
        });
    }

    @Override
    public List<? extends ManagerSecurityGroupInterfaceElement> listSecurityGroupInterfaces() throws Exception {
        return txControl.supports(new Callable<List<? extends ManagerSecurityGroupInterfaceElement>>() {
            
            @Override
            public List<? extends ManagerSecurityGroupInterfaceElement> call() throws Exception {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                
                CriteriaQuery<SecurityGroup> q = cb.createQuery(SecurityGroup.class);
                Root<SecurityGroup> r = q.from(SecurityGroup.class);
                q.select(r).where(cb.equal(r.get("parent").get("id"), vs.getId()));
                
                List<SecurityGroupInterface> devices = new ArrayList<SecurityGroupInterface>();
                for (SecurityGroup sg : em.createQuery(q).getResultList()) {
                    devices.add(new SecurityGroupInterface(sg.getId(), 
                            sg.getName(), sg.getPolicyId(), sg.getTag()));
                }
                return devices;
            }
        });
    }

    @Override
    public void close() {
    }

}
