
package cl.duoc.emergencias.messaging.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import cl.duoc.emergencias.model.EmergenciaEvento;

@Component
public class EmergenciaConsumer {

    private final ObjectMapper objectMapper;

    public EmergenciaConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
        topics = "${app.kafka.topic.emergencias}",
        groupId = "grupo-emergencias"
    )
    public void consumir(String mensaje) throws Exception {

        System.out.println("Mensaje recibido: " + mensaje);

        EmergenciaEvento evento = objectMapper.readValue(
            mensaje,
            EmergenciaEvento.class
        );

        // Validar datos obligatorios del evento
        if (evento.comuna() == null || evento.comuna().isBlank()
            || evento.tipo() == null || evento.tipo().isBlank()
            || evento.descripcion() == null || evento.descripcion().isBlank()) {

            throw new IllegalArgumentException(
                "Evento inválido. Faltan datos obligatorios"
            );
        }

        System.out.println(
            "Emergencia procesada: " + evento.tipo()
            + " - " + evento.comuna()
        );
    }
}
