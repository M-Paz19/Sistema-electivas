package com.unicauca.fiet.sistema_electivas.electiva.controller;

import com.unicauca.fiet.sistema_electivas.common.dto.MensajeResponse;
import com.unicauca.fiet.sistema_electivas.electiva.dto.ActualizarElectivaDTO;
import com.unicauca.fiet.sistema_electivas.electiva.dto.CrearElectivaDTO;
import com.unicauca.fiet.sistema_electivas.electiva.dto.ElectivaResponseDTO;
import com.unicauca.fiet.sistema_electivas.common.exception.GlobalExceptionHandler;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.electiva.model.Electiva;
import com.unicauca.fiet.sistema_electivas.electiva.service.ElectivaService;
import com.unicauca.fiet.sistema_electivas.electiva.service.ElectivaServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/electivas")
public class ElectivaController {

    @Autowired
    private ElectivaService electivaService;

    /**
     * Crea una nueva electiva en el sistema.
     *
     * <p>Valida los datos de entrada y delega la lógica de creación al servicio correspondiente.
     * Las excepciones de validación, duplicados o recursos no encontrados se manejan
     * automáticamente por el {@link GlobalExceptionHandler}.
     *
     * @param dto Datos necesarios para crear la electiva
     * @return Detalle de la electiva creada
     */
    @PostMapping
    public ResponseEntity<ElectivaResponseDTO> crearElectiva(@Valid @RequestBody CrearElectivaDTO dto) {
        ElectivaResponseDTO response = electivaService.crearElectiva(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    /**
     * Endpoint para listar todas las electivas, con filtros opcionales.
     */
    @GetMapping
    public ResponseEntity<List<ElectivaResponseDTO>> listarElectivas(
            @RequestParam(defaultValue = "false") boolean mostrarInactivas,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(electivaService.findElectivas(mostrarInactivas, query));
    }


    /**
     * Endpoint para actualizar la información de una electiva.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ElectivaResponseDTO> actualizarElectiva(@PathVariable Long id, @Valid @RequestBody ActualizarElectivaDTO request) {
        return ResponseEntity.ok(electivaService.actualizarElectiva(id, request));
    }

    /**
     * Endpoint para obtener una electiva por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ElectivaResponseDTO> obtenerElectivaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(electivaService.buscarPorId(id));
    }

    /**
     * Endpoint para aprobar una electiva (BORRADOR -> APROBADA).
     */
    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<MensajeResponse> aprobarElectiva(@PathVariable Long id) {
        electivaService.aprobarElectiva(id);
        return ResponseEntity.ok(new MensajeResponse("Electiva aprobada exitosamente"));
    }

    /**
     * Endpoint para desactivar una electiva (APROBADA -> INACTIVA).
     */
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<MensajeResponse> desactivarElectiva(@PathVariable Long id) {
        electivaService.desactivarElectiva(id);
        return ResponseEntity.ok(new MensajeResponse("Electiva desactivada exitosamente"));
    }

    /**
     * Endpoint para reactivar una electiva (INACTIVA -> APROBADA).
     */
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<MensajeResponse> reactivarElectiva(@PathVariable Long id) {
        electivaService.reactivarElectiva(id);
        return ResponseEntity.ok(new MensajeResponse("Electiva reactivada exitosamente"));
    }
}
