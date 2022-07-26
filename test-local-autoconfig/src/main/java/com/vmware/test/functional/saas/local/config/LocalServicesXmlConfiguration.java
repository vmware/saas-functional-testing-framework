/*
 * Copyright 2022 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:docker-container.properties")
@EnableConfigurationProperties(DockerConfig.class)
@ImportResource({"classpath:/local-service-config.xml", "classpath:/local-aws-service-config.xml"})
public class LocalServicesXmlConfiguration {
}
