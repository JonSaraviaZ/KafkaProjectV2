package cl.duoc.emergencias.config.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;

import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorConfig {

  @Bean
  public DefaultErrorHandler kafkaErrorHandler(
      @Value("${spring.kafka.bootstrap-servers}")
      String bootstrapServers) {

    Map<String, Object> props = new HashMap<>();

    props.put(
      ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
      bootstrapServers
    );

    props.put(
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
      StringSerializer.class
    );

    props.put(
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
      StringSerializer.class
    );

    var producerFactory =
      new DefaultKafkaProducerFactory<String, String>(props);

    var dltTemplate =
      new KafkaTemplate<String, String>(producerFactory);

    var recoverer = new DeadLetterPublishingRecoverer(
      dltTemplate,
      (record, exception) -> new TopicPartition(
        record.topic() + ".DLT",
        record.partition()
      )
    );

    // Dos reintentos adicionales, separados por 1 segundo.
    var backOff = new FixedBackOff(1000L, 2L);

    var errorHandler =
      new DefaultErrorHandler(recoverer, backOff);

    // JSON mal formado y datos inválidos no se corrigen
    // repitiendo el mismo procesamiento.
    errorHandler.addNotRetryableExceptions(
      com.fasterxml.jackson.core.JsonProcessingException.class,
      IllegalArgumentException.class
    );

    return errorHandler;
  }
}