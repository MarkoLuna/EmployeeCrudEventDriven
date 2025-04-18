package com.employee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {
	DataSourceAutoConfiguration.class
})
public class EmployeeConsumerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeeConsumerServiceApplication.class, args);
	}

}
