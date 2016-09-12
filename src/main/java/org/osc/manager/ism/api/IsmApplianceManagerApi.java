package org.osc.manager.ism.api;

import org.osc.sdk.manager.ManagerAuthenticationType;
import org.osc.sdk.manager.ManagerNotificationSubscriptionType;
import org.osc.sdk.manager.api.ApplianceManagerApi;
import org.osc.sdk.manager.api.IscJobNotificationApi;
import org.osc.sdk.manager.api.ManagerCallbackNotificationApi;
import org.osc.sdk.manager.api.ManagerDeviceApi;
import org.osc.sdk.manager.api.ManagerDomainApi;
import org.osc.sdk.manager.api.ManagerPolicyApi;
import org.osc.sdk.manager.api.ManagerSecurityGroupApi;
import org.osc.sdk.manager.api.ManagerSecurityGroupInterfaceApi;
import org.osc.sdk.manager.api.ManagerWebSocketNotificationApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.VirtualSystemElement;

public class IsmApplianceManagerApi implements ApplianceManagerApi {

    public IsmApplianceManagerApi() {

    }

    public static IsmApplianceManagerApi create() {
        return new IsmApplianceManagerApi();
    }

    @Override
    public ManagerDeviceApi createManagerDeviceApi(ApplianceManagerConnectorElement mc, VirtualSystemElement vs) throws Exception {
        return IsmDeviceApi.create(vs);
    }

    @Override
    public ManagerSecurityGroupInterfaceApi createManagerSecurityGroupInterfaceApi(ApplianceManagerConnectorElement mc,
            VirtualSystemElement vs) throws Exception {
        return IsmSecurityGroupInterfaceApi.create(vs);
    }

    @Override
    public ManagerSecurityGroupApi createManagerSecurityGroupApi(ApplianceManagerConnectorElement mc,
            VirtualSystemElement vs) throws Exception {
        throw new UnsupportedOperationException("Security Group sync is not supported.");
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
    public IscJobNotificationApi createIscJobNotificationApi(ApplianceManagerConnectorElement mc, VirtualSystemElement vs) throws Exception {
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
        return ManagerNotificationSubscriptionType.NONE;
    }

    @Override
    public boolean isSecurityGroupSyncSupport() {
        return false;
    }

    @Override
    public boolean isAgentManaged() {
        return true;
    }

    @Override
    public boolean isPolicyMappingSupported() {
        return false;
    }

    @Override
    public void checkConnection(ApplianceManagerConnectorElement mc) throws Exception {

    }

    @Override
    public String getVendorName() {
        return "ISM";
    }

}
