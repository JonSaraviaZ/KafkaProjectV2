package cl.duoc.emergencias.service;
import cl.duoc.emergencias.dto.CrearEmergenciaRequest;
import cl.duoc.emergencias.messaging.producer.EmergenciaProducer;
import cl.duoc.emergencias.model.EmergenciaEvento;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
@Service
public class EmergenciaService {
    private final EmergenciaProducer producer;
    public EmergenciaService(EmergenciaProducer producer) { this.producer = producer; }
    public CompletableFuture<EmergenciaEvento> registrar(CrearEmergenciaRequest request) {
        var evento = new EmergenciaEvento(UUID.randomUUID().toString(), request.comuna().trim(),
                request.tipo().trim(), request.descripcion().trim(), Instant.now());
        return producer.publicar(evento).thenApply(ignored -> evento);
    }
}
