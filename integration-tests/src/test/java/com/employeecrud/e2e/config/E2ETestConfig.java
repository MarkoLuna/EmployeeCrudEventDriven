package com.employeecrud.e2e.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(E2EProperties.class)
@ComponentScan("com.employeecrud.e2e")
public class E2ETestConfig {}
