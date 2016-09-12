package org.osc.manager.ism.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.osc.core.broker.model.entities.appliance.VirtualSystem;
import org.osc.core.broker.util.db.HibernateUtil;
import org.osc.manager.ism.model.SecurityGroupInterface;
import org.osc.sdk.manager.api.ManagerSecurityGroupInterfaceApi;
import org.osc.sdk.manager.element.ManagerSecurityGroupInterfaceElement;
import org.osc.sdk.manager.element.VirtualSystemElement;

public class IsmSecurityGroupInterfaceApi implements ManagerSecurityGroupInterfaceApi {

    Logger log = Logger.getLogger(IsmSecurityGroupInterfaceApi.class);

    private VirtualSystem vs;

    public interface TransactionalLogic<ResponseClass, RequestClass> {
        public abstract ResponseClass run(Session session, RequestClass param);
    }

    public class TransactionalRunner<ResponseClass, RequestClass> {
        private Session session;
        private Transaction tx;

        public TransactionalRunner() {
            this.session = HibernateUtil.getSessionFactory().openSession();
        }

        private ResponseClass exec(TransactionalLogic<ResponseClass, RequestClass> logic) {
            return exec(logic, null);
        }

        private ResponseClass exec(TransactionalLogic<ResponseClass, RequestClass> logic, RequestClass param) {
            try {
                this.tx = this.session.beginTransaction();

                ResponseClass obj = logic.run(this.session, param);

                this.tx.commit();

                return obj;
            } catch (Exception e) {

                IsmSecurityGroupInterfaceApi.this.log.error("Fail to execute transaction logic.", e);
                if (this.tx != null) {
                    this.tx.rollback();
                }

            } finally {
                if (this.session != null) {
                    this.session.close();
                }
            }

            return null;
        }

    }

    public static ManagerSecurityGroupInterfaceApi create(VirtualSystemElement vs) throws Exception {
        return new IsmSecurityGroupInterfaceApi(vs);
    }

    private IsmSecurityGroupInterfaceApi(final VirtualSystemElement vs) throws Exception {

        TransactionalRunner<Object, Object> tr = new TransactionalRunner<Object, Object>();
        tr.exec(new TransactionalLogic<Object, Object>() {

            @Override
            public Object run(Session session, Object param) {
                IsmSecurityGroupInterfaceApi.this.vs = (VirtualSystem) session.get(VirtualSystem.class, vs.getId());
                // Force population of child objects
                IsmSecurityGroupInterfaceApi.this.vs.getDistributedApplianceInstances().isEmpty();
                IsmSecurityGroupInterfaceApi.this.vs.getSecurityGroupInterfaces().isEmpty();
                return IsmSecurityGroupInterfaceApi.this.vs;
            }
        });

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
    public ManagerSecurityGroupInterfaceElement getSecurityGroupInterfaceById(String id) throws Exception {
        for (org.osc.core.broker.model.entities.virtualization.SecurityGroupInterface sgi : this.vs
                .getSecurityGroupInterfaces()) {
            if (sgi.getId().toString().equals(id)) {
                return new SecurityGroupInterface(sgi.getId(), sgi.getName(), sgi.getPolicyId(), sgi.getTag());
            }
        }

        return null;
    }

    @Override
    public String findSecurityGroupInterfaceByName(String name) throws Exception {
        for (org.osc.core.broker.model.entities.virtualization.SecurityGroupInterface sgi : this.vs
                .getSecurityGroupInterfaces()) {
            if (sgi.getName().equals(name)) {
                return sgi.getId().toString();
            }
        }
        return null;
    }

    @Override
    public List<? extends ManagerSecurityGroupInterfaceElement> listSecurityGroupInterfaces() throws Exception {
        List<ManagerSecurityGroupInterfaceElement> sgis = new ArrayList<ManagerSecurityGroupInterfaceElement>();
        for (org.osc.core.broker.model.entities.virtualization.SecurityGroupInterface sgi : this.vs
                .getSecurityGroupInterfaces()) {
            sgis.add(new SecurityGroupInterface(sgi.getId(), sgi.getName(), sgi.getPolicyId(), sgi.getTag()));
        }

        return sgis;
    }

    @Override
    public void close() {
    }

}
