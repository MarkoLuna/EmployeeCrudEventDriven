package com.employee.config;

import com.common.employee.dto.EmployeeMessage;
import com.employee.config.properties.KafkaConfigProperties;
import com.nimbusds.jose.shaded.gson.JsonSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, EmployeeMessage> producerFactory(
            KafkaConfigProperties kafkaConfigProperties) {

        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigProperties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, EmployeeMessage> employeeUpsertKafkaTemplate(
            ProducerFactory<String, EmployeeMessage> producerFactory,
            KafkaConfigProperties kafkaConfigProperties) {

        var kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setDefaultTopic(kafkaConfigProperties.getEmployeeUpsertTopic());

        return kafkaTemplate;
    }

    @Bean
    public KafkaTemplate<String, EmployeeMessage> employeeDeletionKafkaTemplate(
            ProducerFactory<String, EmployeeMessage> producerFactory,
            KafkaConfigProperties kafkaConfigProperties) {

        var kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setDefaultTopic(kafkaConfigProperties.getEmployeeDeletionTopic());

        return kafkaTemplate;
    }
}
