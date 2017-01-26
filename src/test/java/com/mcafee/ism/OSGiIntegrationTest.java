package com.mcafee.ism;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.*;

import java.util.Collections;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.util.PathUtils;
import org.osc.sdk.manager.api.ApplianceManagerApi;
import org.osc.sdk.manager.api.ManagerDeviceApi;
import org.osc.sdk.manager.element.ApplianceSoftwareVersionElement;
import org.osc.sdk.manager.element.DistributedApplianceElement;
import org.osc.sdk.manager.element.DomainElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
import org.osc.sdk.manager.element.VirtualizationConnectorElement;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class OSGiIntegrationTest {

    public static final class VSElement implements VirtualSystemElement {

        private final Long id;

        private final String name;

        public VSElement(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public VirtualizationConnectorElement getVirtualizationConnector() {
            return null;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getMgrId() {
            return null;
        }

        @Override
        public byte[] getKeyStore() {
            return null;
        }

        @Override
        public Long getId() {
            return this.id;
        }

        @Override
        public DomainElement getDomain() {
            return null;
        }

        @Override
        public DistributedApplianceElement getDistributedAppliance() {
            return null;
        }

        @Override
        public ApplianceSoftwareVersionElement getApplianceSoftwareVersion() {
            return null;
        }
    }

    private static final String VSS_TEST = "VssTest";

    private static final String DEVICE_MEMBER_TEST = "deviceMemberTest";

    @Inject
    ApplianceManagerApi api;

    @org.ops4j.pax.exam.Configuration
    public Option[] config() {

        return options(
                // Load the current module from its built classes so we get the latest from Eclipse
                bundle("reference:file:" + PathUtils.getBaseDir() + "/target/classes/"),

                // And some dependencies
                mavenBundle("org.apache.felix", "org.apache.felix.scr").versionAsInProject(),

                mavenBundle("org.osc.api", "security-mgr-api").versionAsInProject(),
                mavenBundle("javax.websocket", "javax.websocket-api").versionAsInProject(),
                mavenBundle("log4j", "log4j").versionAsInProject(),
                mavenBundle("org.apache.aries.jpa", "org.apache.aries.jpa.container").versionAsInProject(),
                mavenBundle("org.apache.aries.tx-control", "tx-control-service-local").versionAsInProject(),
                mavenBundle("org.apache.aries.tx-control", "tx-control-provider-jpa-local").versionAsInProject(),
                mavenBundle("com.h2database", "h2").versionAsInProject(),

                // Hibernate

                systemPackage("javax.xml.stream;version=1.0"),
                systemPackage("javax.xml.stream.events;version=1.0"),
                systemPackage("javax.xml.stream.util;version=1.0"),
                systemPackage("javax.transaction;version=1.1"),
                systemPackage("javax.transaction.xa;version=1.1"),
                bootClasspathLibrary(mavenBundle("org.apache.geronimo.specs", "geronimo-jta_1.1_spec", "1.1.1")).beforeFramework(),

                // Hibernate bundles and their dependencies (JPA API is available from the tx-control)
                mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.antlr", "2.7.7_5"),
                mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.dom4j", "1.6.1_5"),
                mavenBundle("org.javassist", "javassist", "3.18.1-GA"),
                mavenBundle("org.jboss.logging", "jboss-logging", "3.3.0.Final"),
                mavenBundle("org.jboss", "jandex", "2.0.0.Final"),
                mavenBundle("org.hibernate.common", "hibernate-commons-annotations", "5.0.1.Final"),
                mavenBundle("org.hibernate", "hibernate-core", "5.0.9.Final"),
                mavenBundle("org.hibernate", "hibernate-osgi", "5.0.9.Final"),
                mavenBundle("org.hibernate", "hibernate-entitymanager", "5.0.9.Final"),

                // Just needed for the test so we can configure the client to point at the local test server
                //                mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.8.10"),

                // Uncomment this line to allow remote debugging
                // CoreOptions.vmOption("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044"),

                junitBundles()
                );
    }

    @Before
    public void start() throws Exception {

        VirtualSystemElement vse = new VSElement(1L, VSS_TEST);

        ManagerDeviceApi managerDeviceApi = this.api.createManagerDeviceApi(null, vse);

        assertEquals("1", managerDeviceApi.createVSSDevice());

        assertNotNull(managerDeviceApi.createDeviceMember(DEVICE_MEMBER_TEST, null, null, null, null, null));
    }

    @After
    public void stop() throws Exception {
        VirtualSystemElement vse = new VSElement(1L, VSS_TEST);

        ManagerDeviceApi managerDeviceApi = this.api.createManagerDeviceApi(null, vse);
        managerDeviceApi.deleteVSSDevice();
        assertEquals(Collections.emptyList(), managerDeviceApi.listDevices());
    }

    @Test
    public void testRegistered() throws Exception {

        VirtualSystemElement vs = new VSElement(0L, "abc");

        ManagerDeviceApi managerDeviceApi = this.api.createManagerDeviceApi(null, vs);

        assertEquals(1, managerDeviceApi.listDevices().size());

        assertEquals("1", managerDeviceApi.findDeviceByName(VSS_TEST));
        assertEquals(0, managerDeviceApi.listDeviceMembers().size());

        vs = new VSElement(1L, VSS_TEST);
        managerDeviceApi = this.api.createManagerDeviceApi(null, vs);
        assertEquals(1, managerDeviceApi.listDeviceMembers().size());

        assertNotNull(managerDeviceApi.findDeviceMemberByName(DEVICE_MEMBER_TEST));
    }
}
