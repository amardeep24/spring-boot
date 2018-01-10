/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.autoconfigure.endpoint.web.servlet;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.annotation.WebAnnotationEndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.endpoint.web.EndpointMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * {@link ManagementContextConfiguration} for Spring MVC {@link Endpoint} concerns.
 *
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @since 2.0.0
 */
@ManagementContextConfiguration
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass(DispatcherServlet.class)
@ConditionalOnBean({ DispatcherServlet.class, WebAnnotationEndpointDiscoverer.class })
@EnableConfigurationProperties(CorsEndpointProperties.class)
public class WebMvcEndpointManagementContextConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(
			WebAnnotationEndpointDiscoverer endpointDiscoverer,
			EndpointMediaTypes endpointMediaTypes, CorsEndpointProperties corsProperties,
			WebEndpointProperties webEndpointProperties) {
		WebMvcEndpointHandlerMapping handlerMapping = new WebMvcEndpointHandlerMapping(
				new EndpointMapping(webEndpointProperties.getBasePath()),
				endpointDiscoverer.discoverEndpoints(), endpointMediaTypes,
				corsProperties.toCorsConfiguration());
		return handlerMapping;
	}

}
