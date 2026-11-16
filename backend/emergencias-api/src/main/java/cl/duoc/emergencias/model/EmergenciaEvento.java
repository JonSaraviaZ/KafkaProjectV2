package cl.duoc.emergencias.model;
import java.time.Instant;
public record EmergenciaEvento(String id, String comuna, String tipo,
                               String descripcion, Instant fecha) {}
