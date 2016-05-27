package com.mcafee.ism.api;

import com.intelsecurity.isc.plugin.manager.ManagerAuthenticationType;
import com.intelsecurity.isc.plugin.manager.ManagerNotificationSubscriptionType;
import com.intelsecurity.isc.plugin.manager.api.ApplianceManagerApi;
import com.intelsecurity.isc.plugin.manager.api.IscJobNotificationApi;
import com.intelsecurity.isc.plugin.manager.api.ManagerDeviceApi;
import com.intelsecurity.isc.plugin.manager.api.ManagerDomainApi;
import com.intelsecurity.isc.plugin.manager.api.ManagerPolicyApi;
import com.intelsecurity.isc.plugin.manager.api.ManagerCallbackNotificationApi;
import com.intelsecurity.isc.plugin.manager.api.ManagerWebSocketNotificationApi;
import com.intelsecurity.isc.plugin.manager.element.ApplianceManagerConnectorElement;
import com.intelsecurity.isc.plugin.manager.element.VirtualSystemElement;

public class IsmApplianceManagerApi implements ApplianceManagerApi {

    public IsmApplianceManagerApi() {

    }

    public static IsmApplianceManagerApi create() {
        return new IsmApplianceManagerApi();
    }

    @Override
    public ManagerDeviceApi createManagerDeviceApi(VirtualSystemElement vs) throws Exception {
        return IsmDeviceApi.create(vs);
    }

    @Override
    public ManagerPolicyApi createManagerPolicyApi(ApplianceManagerConnectorElement mc) throws Exception {
        return IsmPolicyApi.create(mc);
    }

    @Override
    public ManagerDomainApi createManagerDomainApi(ApplianceManagerConnectorElement mc) throws Exception {
        return IsmDomainApi.create(mc);
    }

    @Override
    public ManagerWebSocketNotificationApi createManagerWebSocketNotificationApi(ApplianceManagerConnectorElement mc)
            throws Exception {
        throw new UnsupportedOperationException("WebSocket Notification not implemented");
    }

    @Override
    public ManagerCallbackNotificationApi createManagerCallbackNotificationApi(ApplianceManagerConnectorElement mc)
            throws Exception {
        throw new UnsupportedOperationException("Manager does not support notification");
    }

    @Override
    public IscJobNotificationApi createIscJobNotificationApi(VirtualSystemElement vs) throws Exception {
        return null;
    }

    @Override
    public byte[] getPublicKey(ApplianceManagerConnectorElement mc) throws Exception {
        return null;
    }

    @Override
    public String getName() {
        return "ISM";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getServiceName() {
        return "DPI";
    }

    @Override
    public String getNsxServiceName() {
        return "IPS_IDS";
    }

    @Override
    public String getManagerUrl(String ipAddress) {
        return "http://" + ipAddress;
    }

    @Override
    public ManagerAuthenticationType getAuthenticationType() {
        return ManagerAuthenticationType.BASIC_AUTH;
    }

    @Override
    public ManagerNotificationSubscriptionType getNotificationType() {
        return ManagerNotificationSubscriptionType.CALLBACK_URL;
    }

    @Override
    public boolean isNotificationSupported() {
        return false;
    }

    @Override
    public boolean isSecurityGroupSyncSupport() {
        return false;
    }

    @Override
    public String getVendorName() {
        return "ISM";
    }

}
