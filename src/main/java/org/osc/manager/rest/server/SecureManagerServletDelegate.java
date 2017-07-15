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
import static org.osgi.service.http.whiteboard.HttpWhiteboardConstants.*;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osc.manager.rest.server.api.PolicyApis;
//import org.osc.core.broker.rest.server.ApiServletContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

@Component(name = "sm.servlet", service = Servlet.class, property = {

        HTTP_WHITEBOARD_SERVLET_NAME + "=" + "OSC-API", HTTP_WHITEBOARD_SERVLET_PATTERN + "=/testsmplugin/*",
        HTTP_WHITEBOARD_CONTEXT_SELECT + "=(" + HTTP_WHITEBOARD_CONTEXT_NAME + "=" + "OSC-API"
        + ")",        
        HTTP_WHITEBOARD_TARGET + "=(" + "org.apache.felix.http.name" + "=" + "OSC-API" + ")"
        
 })


public class SecureManagerServletDelegate extends ResourceConfig implements Servlet {
    static final long serialVersionUID = 1L;

   @Reference
   private PolicyApis policiesApis;

   
   /** The Jersey REST container */
   private ServletContainer container;
   
   @Activate
   void activate() {
       // Json feature
	   super.register(JacksonJaxbJsonProvider.class);

       //Properties
       super.property(ServerProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);

       // agent & server apis
       super.registerInstances(this.policiesApis);
       
       this.container = new ServletContainer(this);
    }

    @Override
    public void destroy() {
        this.container.destroy();
    }

    // Servlet interface methods

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
