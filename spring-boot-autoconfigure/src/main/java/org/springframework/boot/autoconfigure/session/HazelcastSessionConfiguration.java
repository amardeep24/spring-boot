/*
 * Copyright 2012-2016 the original author or authors.
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

package org.springframework.boot.autoconfigure.session;

import javax.annotation.PostConstruct;

import com.hazelcast.core.HazelcastInstance;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;
import org.springframework.session.hazelcast.config.annotation.web.http.HazelcastHttpSessionConfiguration;

/**
 * Hazelcast backed session auto-configuration.
 *
 * @author Tommy Ludwig
 * @since 1.4.0
 */
@Configuration
@ConditionalOnClass({ HazelcastInstance.class, HazelcastHttpSessionConfiguration.class })
@ConditionalOnMissingBean({ SessionRepository.class, HazelcastHttpSessionConfiguration.class })
@EnableHazelcastHttpSession
@Conditional(SessionCondition.class)
class HazelcastSessionConfiguration {

	private final ServerProperties serverProperties;

	private final MapSessionRepository sessionRepository;

	HazelcastSessionConfiguration(ServerProperties serverProperties, MapSessionRepository sessionRepository) {
		this.serverProperties = serverProperties;
		this.sessionRepository = sessionRepository;
	}

	@PostConstruct
	public void applyConfigurationProperties() {
		Integer timeout = this.serverProperties.getSession().getTimeout();
		if (timeout != null) {
			this.sessionRepository.setDefaultMaxInactiveInterval(timeout);
		}
	}
}
