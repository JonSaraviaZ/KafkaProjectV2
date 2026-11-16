package cl.duoc.emergencias.messaging.producer;
import cl.duoc.emergencias.model.EmergenciaEvento;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;
@Component
public class EmergenciaProducer {
    private final KafkaTemplate<String, EmergenciaEvento> kafkaTemplate;
    private final String topic;
    public EmergenciaProducer(KafkaTemplate<String, EmergenciaEvento> kafkaTemplate,
                              @Value("${app.kafka.topic.emergencias}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }
    public CompletableFuture<Void> publicar(EmergenciaEvento evento) {
        String key = evento.comuna().trim().toLowerCase(java.util.Locale.ROOT);
        return kafkaTemplate.send(topic, key, evento).thenAccept(resultado -> {
            var metadata = resultado.getRecordMetadata();
            System.out.printf("Evento %s publicado: particion=%d, offset=%d%n",
                    evento.id(), metadata.partition(), metadata.offset());
        });
    }
}
