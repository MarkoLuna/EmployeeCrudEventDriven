package com.employee.config;

import com.common.employee.dto.EmployeeMessage;
import com.employee.config.properties.KafkaConfigProperties;
import com.employee.exceptions.RetryableMessagingException;
import com.fasterxml.jackson.databind.JsonDeserializer;
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
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.messaging.Message;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, EmployeeMessage> employeeUpsertConsumerFactory(
            KafkaConfigProperties kafkaConfigProperties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfigProperties.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConsumerFactory<String, EmployeeMessage> employeeDeletionConsumerFactory(
            KafkaConfigProperties kafkaConfigProperties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfigProperties.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EmployeeMessage>
    employeeUpsertKafkaListenerContainerFactory(
            ConsumerFactory<String, EmployeeMessage> employeeUpsertConsumerFactory,
            KafkaConfigProperties kafkaConfigProperties) {

        ConcurrentKafkaListenerContainerFactory<String, EmployeeMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConcurrency(kafkaConfigProperties.getConcurrency());
        factory.setConsumerFactory(employeeUpsertConsumerFactory);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EmployeeMessage>
    employeeDeletionKafkaListenerContainerFactory(
            ConsumerFactory<String, EmployeeMessage> employeeDeletionConsumerFactory,
            KafkaConfigProperties kafkaConfigProperties) {

        ConcurrentKafkaListenerContainerFactory<String, EmployeeMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConcurrency(kafkaConfigProperties.getConcurrency());
        factory.setConsumerFactory(employeeDeletionConsumerFactory);
        return factory;
    }

    /**
     * Configures a retry topic with the
     * {@link com.employee.services.KafkaConsumer#handleDltForEmployeeDeletion(Message)} handler.
     *
     * @param employeeKafkaTemplate the template to configure with retries.
     * @return the Retry configuration.
     */
    @Bean
    public RetryTopicConfiguration employeeDeletionRetryConfig(
            KafkaTemplate<String, Object> employeeKafkaTemplate,
            KafkaConfigProperties kafkaConfigProperties) {
        return RetryTopicConfigurationBuilder
                .newInstance()
                .fixedBackOff(kafkaConfigProperties.getEventNonBlockingRetry().getPeriod().toMillis())
                .maxAttempts(kafkaConfigProperties.getEventNonBlockingRetry().getMaxAttempts())
                .timeoutAfter(kafkaConfigProperties.getEventNonBlockingRetry().getMaxPeriod().toMillis())
                .includeTopic(kafkaConfigProperties.getEmployeeDeletionTopic())
                //Just retry this exception
                .retryOn(RetryableMessagingException.class)
                .doNotAutoCreateRetryTopics()
                .dltProcessingFailureStrategy(DltStrategy.FAIL_ON_ERROR)
                .dltHandlerMethod("kafkaConsumer", "handleDltForEmployeeDeletion")
                .create(employeeKafkaTemplate);
    }

    /**
     * Configures a retry topic with the
     * {@link com.employee.services.KafkaConsumer#handleDltForEmployeeUpsert(Message)} handler.
     *
     * @param employeeKafkaTemplate the template to configure with retries.
     * @return the Retry configuration.
     */
    @Bean
    public RetryTopicConfiguration employeeUpsertRetryConfig(
            KafkaTemplate<String, Object> employeeKafkaTemplate,
            KafkaConfigProperties kafkaConfigProperties) {
        return RetryTopicConfigurationBuilder
                .newInstance()
                .fixedBackOff(kafkaConfigProperties.getEventNonBlockingRetry().getPeriod().toMillis())
                .maxAttempts(kafkaConfigProperties.getEventNonBlockingRetry().getMaxAttempts())
                .timeoutAfter(kafkaConfigProperties.getEventNonBlockingRetry().getMaxPeriod().toMillis())
                .includeTopic(kafkaConfigProperties.getEmployeeUpsertTopic())
                //Just retry this exception
                .retryOn(RetryableMessagingException.class)
                .doNotAutoCreateRetryTopics()
                .dltProcessingFailureStrategy(DltStrategy.FAIL_ON_ERROR)
                .dltHandlerMethod("kafkaConsumer", "handleDltForEmployeeUpsert")
                .create(employeeKafkaTemplate);
    }

    @Bean
    public KafkaTemplate<String, Object> employeeKafkaTemplate(
            ProducerFactory<String, Object> employeeProducerFactory) {
        return new KafkaTemplate<>(employeeProducerFactory);
    }

    @Bean
    public ProducerFactory<String, Object> employeeProducerFactory(
            KafkaConfigProperties kafkaConfigProperties) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigProperties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(),
                //This to support non-blocking retries
                new DelegatingByTypeSerializer(Map.of(
                        byte[].class, new ByteArraySerializer(),
                        EmployeeMessage.class, new JsonSerializer<>())));
    }
}
