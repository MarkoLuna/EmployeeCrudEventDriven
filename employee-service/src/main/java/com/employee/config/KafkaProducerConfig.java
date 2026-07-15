package com.employee.config;

import com.common.employee.dto.EmployeeMessage;
import com.employee.config.properties.KafkaConfigProperties;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * Configuration for Kafka producer
 */
@Configuration
public class KafkaProducerConfig {

  /**
   * Creates a producer factory for Kafka
   * 
   * @param kafkaConfigProperties Kafka configuration properties
   * @return Producer factory for Kafka
   */
  @Bean
  public ProducerFactory<String, EmployeeMessage> producerFactory(
      KafkaConfigProperties kafkaConfigProperties) {

    Map<String, Object> configProps = new HashMap<>();
    configProps.put(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigProperties.getBootstrapServers());
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return new DefaultKafkaProducerFactory<>(configProps);
  }

  /**
   * Creates a Kafka template for employee upsert operations
   * 
   * @param producerFactory   Producer factory for Kafka
   * @param kafkaConfigProperties Kafka configuration properties
   * @return Kafka template for employee upsert operations
   */
  @Bean
  public KafkaTemplate<String, EmployeeMessage> employeeUpsertKafkaTemplate(
      ProducerFactory<String, EmployeeMessage> producerFactory,
      KafkaConfigProperties kafkaConfigProperties) {

    var kafkaTemplate = new KafkaTemplate<>(producerFactory);
    kafkaTemplate.setDefaultTopic(kafkaConfigProperties.getEmployeeUpsertTopic());

    return kafkaTemplate;
  }

  /**
   * Creates a Kafka template for employee deletion operations
   * 
   * @param producerFactory   Producer factory for Kafka
   * @param kafkaConfigProperties Kafka configuration properties
   * @return Kafka template for employee deletion operations
   */
  @Bean
  public KafkaTemplate<String, EmployeeMessage> employeeDeletionKafkaTemplate(
      ProducerFactory<String, EmployeeMessage> producerFactory,
      KafkaConfigProperties kafkaConfigProperties) {

    var kafkaTemplate = new KafkaTemplate<>(producerFactory);
    kafkaTemplate.setDefaultTopic(kafkaConfigProperties.getEmployeeDeletionTopic());

    return kafkaTemplate;
  }
}
