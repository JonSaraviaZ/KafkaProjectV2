package cl.duoc.emergencias.dto;
import jakarta.validation.constraints.NotBlank;
public record CrearEmergenciaRequest(@NotBlank String comuna, @NotBlank String tipo,
                                     @NotBlank String descripcion) {}
