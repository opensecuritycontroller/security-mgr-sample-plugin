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

package org.osc.manager.rest.server;
import static java.util.Collections.singletonMap;
import static org.osgi.service.http.whiteboard.HttpWhiteboardConstants.*;
import static org.osgi.service.jdbc.DataSourceFactory.*;

import java.io.IOException;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.sql.DataSource;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osc.manager.rest.server.api.DeviceApis;
import org.osc.manager.rest.server.api.DomainApis;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jpa.JPAEntityManagerProviderFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

@Component(name = "sm.servlet", service = Servlet.class, property = {

        HTTP_WHITEBOARD_SERVLET_NAME + "=" + "OSC-API", HTTP_WHITEBOARD_SERVLET_PATTERN + "=/testplugin/*",
        HTTP_WHITEBOARD_CONTEXT_SELECT + "=(" + HTTP_WHITEBOARD_CONTEXT_NAME + "=" + "OSC-API"
        + ")",
        HTTP_WHITEBOARD_TARGET + "=(" + "org.apache.felix.http.name" + "=" + "OSC-API" + ")"

 })

public class SecurityManagerServletDelegate extends ResourceConfig implements Servlet {

    static final long serialVersionUID = 1L;

    @Reference
    private DomainApis domainApis;

    @Reference
    private DeviceApis deviceApis;

    @Reference(target = "(osgi.local.enabled=true)")
    private TransactionControl txControl;

    @Reference(target = "(osgi.unit.name=ism-mgr)")
    private EntityManagerFactoryBuilder builder;

    @Reference(target = "(osgi.jdbc.driver.class=org.h2.Driver)")
    private DataSourceFactory jdbcFactory;

    @Reference(target = "(osgi.local.enabled=true)")
    private JPAEntityManagerProviderFactory resourceFactory;
    private EntityManager em;

    /** The Jersey REST container */
    private ServletContainer container;

    @Activate
    void activate() throws Exception {

        super.register(JacksonJaxbJsonProvider.class);
        super.property(ServerProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);

        Properties props = new Properties();
        props.setProperty(JDBC_URL, "jdbc:h2:./ismPlugin");
        props.setProperty(JDBC_USER, "admin");
        props.setProperty(JDBC_PASSWORD, "abc12345");

        DataSource ds = this.jdbcFactory.createDataSource(props);

        this.em = this.resourceFactory
                .getProviderFor(this.builder, singletonMap("javax.persistence.nonJtaDataSource", (Object) ds), null)
                .getResource(this.txControl);

        this.domainApis.init(this.em, this.txControl);
        this.deviceApis.init(this.em, this.txControl);

        super.registerInstances(this.domainApis, this.deviceApis);
        this.container = new ServletContainer(this);
       }

    @Override
    public void destroy() {
        this.container.destroy();
    }

    @Override
    public ServletConfig getServletConfig() {
        return this.container.getServletConfig();
    }

    @Override
    public String getServletInfo() {
        return this.container.getServletInfo();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.container.init(config);
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        this.container.service(request, response);
    }
}
