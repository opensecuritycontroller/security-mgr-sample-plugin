package com.mcafee.ism.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.intelsecurity.isc.plugin.manager.api.ManagerDeviceApi;
import com.intelsecurity.isc.plugin.manager.element.ApplianceBootstrapInformationElement;
import com.intelsecurity.isc.plugin.manager.element.BootStrapInfoProviderElement;
import com.intelsecurity.isc.plugin.manager.element.DistributedApplianceInstanceElement;
import com.intelsecurity.isc.plugin.manager.element.ManagerDeviceElement;
import com.intelsecurity.isc.plugin.manager.element.ManagerDeviceMemberElement;
import com.intelsecurity.isc.plugin.manager.element.VirtualSystemElement;
import com.mcafee.ism.model.Device;
import com.mcafee.ism.model.MemberDevice;
import com.mcafee.vmidc.broker.model.entities.appliance.DistributedApplianceInstance;
import com.mcafee.vmidc.broker.model.entities.appliance.VirtualSystem;
import com.mcafee.vmidc.broker.service.persistence.EntityManager;
import com.mcafee.vmidc.broker.util.db.HibernateUtil;

public class IsmDeviceApi implements ManagerDeviceApi {

    Logger log = Logger.getLogger(IsmDeviceApi.class);

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

                IsmDeviceApi.this.log.error("Fail to execute transaction logic.", e);
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

    public static ManagerDeviceApi create(VirtualSystemElement vs) throws Exception {
        return new IsmDeviceApi(vs);
    }

    private IsmDeviceApi(final VirtualSystemElement vs) throws Exception {

        TransactionalRunner<Object, Object> tr = new TransactionalRunner<Object, Object>();
        tr.exec(new TransactionalLogic<Object, Object>() {

            @Override
            public Object run(Session session, Object param) {
                IsmDeviceApi.this.vs = (VirtualSystem) session.get(VirtualSystem.class, vs.getId());
                // Force population of child objects
                IsmDeviceApi.this.vs.getDistributedApplianceInstances().isEmpty();
                IsmDeviceApi.this.vs.getSecurityGroupInterfaces().isEmpty();
                return IsmDeviceApi.this.vs;
            }
        });

    }

    @Override
    public boolean isDeviceGroupSupported() {
        return true;
    }

    @Override
    public ManagerDeviceElement getDeviceById(String id) throws Exception {
        return new Device(this.vs.getId(), this.vs.getName());
    }

    @Override
    public String findDeviceByName(String name) throws Exception {
        TransactionalRunner<VirtualSystem, String> tr = new TransactionalRunner<VirtualSystem, String>();
        VirtualSystem namedVs = tr.exec(new TransactionalLogic<VirtualSystem, String>() {

            @Override
            public VirtualSystem run(Session session, String param) {
                EntityManager<VirtualSystem> em = new EntityManager<VirtualSystem>(VirtualSystem.class, session);
                return em.findByFieldName("name", param);
            }

        }, name);

        return namedVs != null ? namedVs.getId().toString() : null;
    }

    @Override
    public List<? extends ManagerDeviceElement> listDevices() throws Exception {
        TransactionalRunner<List<VirtualSystem>, Object> tr = new TransactionalRunner<List<VirtualSystem>, Object>();
        List<VirtualSystem> vsList = tr.exec(new TransactionalLogic<List<VirtualSystem>, Object>() {

            @Override
            public List<VirtualSystem> run(Session session, Object param) {
                EntityManager<VirtualSystem> em = new EntityManager<VirtualSystem>(VirtualSystem.class, session);
                return em.listAll();
            }

        });

        List<Device> devices = new ArrayList<Device>();
        for (VirtualSystem vsObj : vsList) {
            devices.add(new Device(vsObj.getId(), vsObj.getName()));
        }

        return devices;
    }

    @Override
    public String createVSSDevice() throws Exception {
        return this.vs.getId().toString();
    }

    @Override
    public void updateVSSDevice(ManagerDeviceElement device) throws Exception {
    }

    @Override
    public void deleteVSSDevice() throws Exception {
    }

    @Override
    public String createDeviceMember(String name, String ipAddress, String vserverIpAddress, String contactIpAddress,
            String gateway, String prefixLength) throws Exception {
        return findDeviceMemberByName(name).getId();
    }


    @Override
    public String updateDeviceMember(ManagerDeviceMemberElement deviceElement, String name, String vserverIpAddress,
            String contactIpAddress, String ipAddress, String gateway, String prefixLength) throws Exception {
        return deviceElement.getId();
    }

    @Override
    public void deleteDeviceMember(String id) throws Exception {
    }

    @Override
    public ManagerDeviceMemberElement getDeviceMemberById(String id) throws Exception {
        for (DistributedApplianceInstance dai : this.vs.getDistributedApplianceInstances()) {
            if (dai.getId().toString().equals(id)) {
                return new MemberDevice(dai.getId(), dai.getName());
            }
        }
        return null;
    }

    @Override
    public ManagerDeviceMemberElement findDeviceMemberByName(String name) throws Exception {
        for (DistributedApplianceInstance dai : this.vs.getDistributedApplianceInstances()) {
            if (dai.getName().equals(name)) {
                return new MemberDevice(dai.getId(), dai.getName());
            }
        }
        return null;
    }

    @Override
    public List<? extends ManagerDeviceMemberElement> listDeviceMembers() throws Exception {

        List<MemberDevice> deviceMembers = new ArrayList<MemberDevice>();
        for (DistributedApplianceInstance dai : this.vs.getDistributedApplianceInstances()) {
            deviceMembers.add(new MemberDevice(dai.getId(), dai.getName()));
        }

        return deviceMembers;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isUpgradeSupported(String modelType, String srcSwVersion, String destSwVersion) throws Exception {
        return true;
    }

    @Override
    public byte[] getDeviceMemberConfigById(String mgrDeviceId) throws Exception {
        return null;
    }

    @Override
    public byte[] getDeviceMemberConfiguration(DistributedApplianceInstanceElement dai) {
        return null;
    }

    @Override
    public byte[] getDeviceMemberAdditionalConfiguration(DistributedApplianceInstanceElement dai) {
        return null;
    }

    @Override
    public ApplianceBootstrapInformationElement getBootstrapinfo(BootStrapInfoProviderElement bootStrapInfo) {
        return null;
    }
}
