package com.employeecrud.e2e;

import com.employeecrud.e2e.config.E2ETestConfig;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = E2ETestConfig.class)
public class CucumberSpringConfiguration {}
