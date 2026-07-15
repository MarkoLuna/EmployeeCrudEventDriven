package com.employee.config;

import com.common.employee.dto.EmployeeMessage;
import com.employee.config.properties.KafkaConfigProperties;
import com.employee.exceptions.RetryableMessagingException;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * Configuration for Kafka consumer
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

  /**
   * Creates a consumer factory for Kafka
   * 
   * @param kafkaConfigProperties Kafka configuration properties
   * @return Consumer factory for Kafka
   */
  @Bean
  public ConsumerFactory<String, EmployeeMessage> employeeConsumerFactory(
      KafkaConfigProperties kafkaConfigProperties) {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigProperties.getBootstrapServers());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfigProperties.getGroupId());

    return new DefaultKafkaConsumerFactory<String, EmployeeMessage>(
        props, new StringDeserializer(), new JsonDeserializer<>(EmployeeMessage.class));
  }

  /**
   * Creates a concurrent Kafka listener container factory for employee upsert operations
   * 
   * @param employeeConsumerFactory Consumer factory for Kafka
   * @param kafkaConfigProperties Kafka configuration properties
   * @return Concurrent Kafka listener container factory for employee upsert operations
   */
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, EmployeeMessage>
      employeeKafkaListenerContainerFactory(
          ConsumerFactory<String, EmployeeMessage> employeeConsumerFactory,
          KafkaConfigProperties kafkaConfigProperties) {

    ConcurrentKafkaListenerContainerFactory<String, EmployeeMessage> factory =
        new ConcurrentKafkaListenerContainerFactory<>();

    factory.setConcurrency(kafkaConfigProperties.getConcurrency());
    factory.setConsumerFactory(employeeConsumerFactory);
    return factory;
  }

  /**
   * Configures a retry topic with the deletion DLT handler.
   *
   * @param employeeKafkaTemplate the template to configure with retries.
   * @return the Retry configuration.
   */
  @Bean
  public RetryTopicConfiguration employeeDeletionRetryConfig(
      KafkaTemplate<String, Object> employeeKafkaTemplate,
      KafkaConfigProperties kafkaConfigProperties) {
    return buildRetryConfig(
        employeeKafkaTemplate,
        kafkaConfigProperties,
        kafkaConfigProperties.getEmployeeDeletionTopic(),
        "handleDltForEmployeeDeletion");
  }

  /**
   * Configures a retry topic with the upsert DLT handler.
   *
   * @param employeeKafkaTemplate the template to configure with retries.
   * @return the Retry configuration.
   */
  @Bean
  public RetryTopicConfiguration employeeUpsertRetryConfig(
      KafkaTemplate<String, Object> employeeKafkaTemplate,
      KafkaConfigProperties kafkaConfigProperties) {
    return buildRetryConfig(
        employeeKafkaTemplate,
        kafkaConfigProperties,
        kafkaConfigProperties.getEmployeeUpsertTopic(),
        "handleDltForEmployeeUpsert");
  }

  /**
   * Builds a retry configuration for Kafka
   * 
   * @param employeeKafkaTemplate Kafka template for employee upsert operations
   * @param kafkaConfigProperties Kafka configuration properties
   * @param topic                 Topic to retry
   * @param dltHandler            DLT handler method
   * @return Retry configuration for Kafka
   */
  private RetryTopicConfiguration buildRetryConfig(
      KafkaTemplate<String, Object> employeeKafkaTemplate,
      KafkaConfigProperties kafkaConfigProperties,
      String topic,
      String dltHandler) {
    var retry = kafkaConfigProperties.getEventNonBlockingRetry();
    return RetryTopicConfigurationBuilder.newInstance()
        .fixedBackOff(retry.getPeriod().toMillis())
        .maxAttempts(retry.getMaxAttempts())
        .timeoutAfter(retry.getMaxPeriod().toMillis())
        .includeTopic(topic)
        .retryOn(RetryableMessagingException.class)
        .dltProcessingFailureStrategy(DltStrategy.FAIL_ON_ERROR)
        .dltHandlerMethod("kafkaConsumer", dltHandler)
        .create(employeeKafkaTemplate);
  }

  /**
   * Creates a Kafka template for employee upsert operations
   * 
   * @param employeeProducerFactory Producer factory for Kafka
   * @return Kafka template for employee upsert operations
   */
  @Bean
  public KafkaTemplate<String, Object> employeeKafkaTemplate(
      ProducerFactory<String, Object> employeeProducerFactory) {
    return new KafkaTemplate<>(employeeProducerFactory);
  }

  /**
   * Creates a producer factory for Kafka
   * 
   * @param kafkaConfigProperties Kafka configuration properties
   * @return Producer factory for Kafka
   */
  @Bean
  public ProducerFactory<String, Object> employeeProducerFactory(
      KafkaConfigProperties kafkaConfigProperties) {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigProperties.getBootstrapServers());
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    return new DefaultKafkaProducerFactory<>(
        configProps,
        new StringSerializer(),
        // This to support non-blocking retries
        new DelegatingByTypeSerializer(
            Map.of(
                byte[].class, new ByteArraySerializer(),
                EmployeeMessage.class, new JsonSerializer<>())));
  }
}
