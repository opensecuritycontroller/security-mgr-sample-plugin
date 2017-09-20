/*******************************************************************************
 * Copyright (c) Intel Corporation
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.osc.ism;

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
import org.osc.sdk.manager.element.ManagerDeviceMemberElement;
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

                // Adding a service endpoint
                mavenBundle("javax.servlet", "javax.servlet-api").versionAsInProject(),
                mavenBundle("javax.ws.rs", "javax.ws.rs-api").versionAsInProject(),
                mavenBundle("org.glassfish.jersey.core", "jersey-server").versionAsInProject(),
                mavenBundle("org.glassfish.jersey.core", "jersey-client").versionAsInProject(),
                mavenBundle("javax.annotation", "javax.annotation-api").versionAsInProject(),
                mavenBundle("javax.validation", "validation-api").versionAsInProject(),
                mavenBundle("org.glassfish.jersey.bundles.repackaged", "jersey-guava").versionAsInProject(),
                mavenBundle("org.glassfish.hk2", "hk2-api").versionAsInProject(),
                mavenBundle("org.glassfish.hk2", "hk2-locator").versionAsInProject(),
                mavenBundle("org.glassfish.hk2", "hk2-utils").versionAsInProject(),
                mavenBundle("org.glassfish.hk2.external", "aopalliance-repackaged").versionAsInProject(),
                mavenBundle("org.glassfish.jersey.core", "jersey-common").versionAsInProject(),
                mavenBundle("org.glassfish.hk2", "osgi-resource-locator").versionAsInProject(),
                mavenBundle("org.glassfish.jersey.containers", "jersey-container-servlet-core").versionAsInProject(),
                mavenBundle("com.fasterxml.jackson.jaxrs", "jackson-jaxrs-json-provider").versionAsInProject(),
                mavenBundle("com.fasterxml.jackson.core", "jackson-core").versionAsInProject(),
                mavenBundle("com.fasterxml.jackson.core", "jackson-databind").versionAsInProject(),
                mavenBundle("com.fasterxml.jackson.jaxrs", "jackson-jaxrs-base").versionAsInProject(),
                mavenBundle("com.fasterxml.jackson.core", "jackson-annotations").versionAsInProject(),


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
                mavenBundle("org.javassist", "javassist", "3.20.0-GA"),
                mavenBundle("org.jboss.logging", "jboss-logging", "3.3.0.Final"),
                mavenBundle("org.jboss", "jandex", "2.0.0.Final"),
                mavenBundle("org.hibernate.common", "hibernate-commons-annotations", "5.0.1.Final"),
                mavenBundle("org.hibernate", "hibernate-core", "5.2.3.Final"),
                mavenBundle("org.hibernate", "hibernate-osgi", "5.2.3.Final"),
                mavenBundle("com.fasterxml", "classmate").versionAsInProject(),
                //mavenBundle("org.hibernate", "hibernate-entitymanager", "5.2.3.Final"),

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

        assertNotNull(managerDeviceApi.createVSSDevice());

        assertEquals(1, managerDeviceApi.listDevices().size());

        assertNotNull(managerDeviceApi.createDeviceMember(DEVICE_MEMBER_TEST, null, null, null, null, null));

    }


    @After
    public void stop() throws Exception {

        VirtualSystemElement vse = new VSElement(1L, VSS_TEST);
        ManagerDeviceApi managerDeviceApi = this.api.createManagerDeviceApi(null, vse);
        ManagerDeviceMemberElement member = managerDeviceApi.findDeviceMemberByName(DEVICE_MEMBER_TEST);
        managerDeviceApi.deleteDeviceMember(member.getId());
        managerDeviceApi.deleteVSSDevice();
        assertEquals(Collections.emptyList(), managerDeviceApi.listDevices());
    }

    @Test
    public void testRegistered() throws Exception {

        VirtualSystemElement vs = new VSElement(0L, "abc");

        ManagerDeviceApi managerDeviceApi = this.api.createManagerDeviceApi(null, vs);

        assertEquals(1, managerDeviceApi.listDevices().size());

        assertNotNull(managerDeviceApi.findDeviceByName(VSS_TEST));
        assertEquals(0, managerDeviceApi.listDeviceMembers().size());

        vs = new VSElement(1L, VSS_TEST);
        managerDeviceApi = this.api.createManagerDeviceApi(null, vs);
        assertEquals(1, managerDeviceApi.listDeviceMembers().size());

        assertNotNull(managerDeviceApi.findDeviceMemberByName(DEVICE_MEMBER_TEST));
    }
}
