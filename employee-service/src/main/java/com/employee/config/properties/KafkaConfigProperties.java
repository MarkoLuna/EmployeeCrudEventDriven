package com.employee.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "kafka.producer")
public class KafkaConfigProperties {

    private String bootstrapAddress;
    private String employeeUpsertTopic;
    private String employeeDeletionTopic;
}
