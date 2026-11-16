package cl.duoc.emergencias.controller;
import cl.duoc.emergencias.dto.CrearEmergenciaRequest;
import cl.duoc.emergencias.model.EmergenciaEvento;
import cl.duoc.emergencias.service.EmergenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;
@RestController
@RequestMapping("/api/emergencias")
public class EmergenciaController {
    private final EmergenciaService service;
    public EmergenciaController(EmergenciaService service) { this.service = service; }
    @PostMapping
    public CompletableFuture<ResponseEntity<EmergenciaEvento>> crear(
            @Valid @RequestBody CrearEmergenciaRequest request) {
        return service.registrar(request).thenApply(evento -> ResponseEntity.accepted().body(evento));
    }
}
