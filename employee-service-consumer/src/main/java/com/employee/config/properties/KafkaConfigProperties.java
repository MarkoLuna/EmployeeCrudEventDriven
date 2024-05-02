package com.employee.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "kafka.consumer")
public class KafkaConfigProperties {

    private boolean enabled;
    private String groupId;
    private String bootstrapAddress;
    private String employeeUpsertTopic;
    private String employeeDeletionTopic;
}
