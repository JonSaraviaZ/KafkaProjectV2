package cl.duoc.emergencias.config.kafka;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic emergenciasTopic(@Value("${app.kafka.topic.emergencias}") String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1)
                .config("retention.ms", "604800000")
                .config("cleanup.policy", "delete").build();
    }
    @Bean
    public NewTopic auditoriaTopic(@Value("${app.kafka.topic.auditoria}") String name) {
        return TopicBuilder.name(name).partitions(2).replicas(1)
                .config("retention.ms", "2592000000")
                .config("cleanup.policy", "delete").build();
    }
}
