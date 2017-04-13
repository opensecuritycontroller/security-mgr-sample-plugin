package org.osc.manager.ism.api;

import static java.util.Collections.singletonMap;
import static org.osc.sdk.manager.Constants.*;
import static org.osgi.service.jdbc.DataSourceFactory.*;

import java.sql.SQLException;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.osc.sdk.manager.api.ApplianceManagerApi;
import org.osc.sdk.manager.api.IscJobNotificationApi;
import org.osc.sdk.manager.api.ManagerCallbackNotificationApi;
import org.osc.sdk.manager.api.ManagerDeviceApi;
import org.osc.sdk.manager.api.ManagerDeviceMemberApi;
import org.osc.sdk.manager.api.ManagerDomainApi;
import org.osc.sdk.manager.api.ManagerPolicyApi;
import org.osc.sdk.manager.api.ManagerSecurityGroupApi;
import org.osc.sdk.manager.api.ManagerSecurityGroupInterfaceApi;
import org.osc.sdk.manager.api.ManagerWebSocketNotificationApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jpa.JPAEntityManagerProviderFactory;


@Component(configurationPid="com.mcafee.nsm.ApplianceManager",
property={
        PLUGIN_NAME + "=ISM",
        VENDOR_NAME + "=Sample",
        SERVICE_NAME + "=DPI",
        EXTERNAL_SERVICE_NAME + "=SAMPLE_IPS",
        AUTHENTICATION_TYPE + "=BASIC_AUTH",
        NOTIFICATION_TYPE + "=NONE",
        SYNC_SECURITY_GROUP + ":Boolean=false",
        PROVIDE_DEVICE_STATUS + ":Boolean=true",
        SYNC_POLICY_MAPPING + ":Boolean=false"})
public class IsmApplianceManagerApi implements ApplianceManagerApi {

    @Reference(target="(osgi.local.enabled=true)")
    private TransactionControl txControl;

    @Reference(target="(osgi.unit.name=ism-mgr)")
    private EntityManagerFactoryBuilder builder;

    @Reference(target="(osgi.jdbc.driver.class=org.h2.Driver)")
    private DataSourceFactory jdbcFactory;

    @Reference(target="(osgi.local.enabled=true)")
    private JPAEntityManagerProviderFactory resourceFactory;

    private EntityManager em;

    @ObjectClassDefinition
    @interface Config {
        String db_url() default "jdbc:h2:./ismPlugin";

        String user() default "admin";

        String _password() default "abc12345";
    }

    @Activate
    void start(Config config) throws SQLException {

        // There is no way to provide generic configuration for
        // a plugin, so we wire this up programatically.

        Properties props = new Properties();

        props.setProperty(JDBC_URL, config.db_url());
        if(config.user() != null && !config.user().isEmpty()) {
            props.setProperty(JDBC_USER, config.user());
            props.setProperty(JDBC_PASSWORD, config._password());
        }

        DataSource ds = this.jdbcFactory.createDataSource(props);

        this.em = this.resourceFactory.getProviderFor(this.builder,
                singletonMap("javax.persistence.nonJtaDataSource", (Object)ds), null)
                .getResource(this.txControl);

    }


    @Override
    public ManagerDeviceApi createManagerDeviceApi(ApplianceManagerConnectorElement mc, VirtualSystemElement vs) throws Exception {
        return new IsmDeviceApi(vs, this.txControl, this.em);
    }

    @Override
    public ManagerSecurityGroupInterfaceApi createManagerSecurityGroupInterfaceApi(ApplianceManagerConnectorElement mc,
            VirtualSystemElement vs) throws Exception {
        return new IsmSecurityGroupInterfaceApi(vs, this.txControl, this.em);
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
    public ManagerDeviceMemberApi createManagerDeviceMemberApi(ApplianceManagerConnectorElement mc, VirtualSystemElement vs) throws Exception {
        return IsmAgentApi.create(mc, vs);
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
    public String getManagerUrl(String ipAddress) {
        return "http://" + ipAddress;
    }

    @Override
    public void checkConnection(ApplianceManagerConnectorElement mc) throws Exception {

    }
}
