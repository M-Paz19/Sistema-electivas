package com.unicauca.fiet.sistema_electivas.controller;

import com.unicauca.fiet.sistema_electivas.dto.ActualizarElectivaDTO;
import com.unicauca.fiet.sistema_electivas.dto.CrearElectivaDTO;
import com.unicauca.fiet.sistema_electivas.dto.ElectivaResponseDTO;
import com.unicauca.fiet.sistema_electivas.exception.GlobalExceptionHandler;
import com.unicauca.fiet.sistema_electivas.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.model.Electiva;
import com.unicauca.fiet.sistema_electivas.service.ElectivaServiceImpl;
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
    private ElectivaServiceImpl electivaService;

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



    @GetMapping
    public ResponseEntity<List<Electiva>> buscarElectivas(
            @RequestParam(required = false, defaultValue = "false") boolean mostrarInactivas,
            @RequestParam(required = false) String query) {
        List<Electiva> electivas = electivaService.findElectivas(mostrarInactivas, query);
        if (electivas.isEmpty() && query != null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(electivas);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarElectiva(
            @PathVariable Long id,
            @RequestBody @Valid ActualizarElectivaDTO dto) {
        try {
            Electiva electivaActualizada = electivaService.actualizarElectiva(id, dto);
            return ResponseEntity.ok(electivaActualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar electiva: " + e.getMessage());
        }
    }


    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobarElectiva(@PathVariable Long id) {
        try {
            electivaService.aprobarElectiva(id);
            return ResponseEntity.ok("Electiva aprobada exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivarElectiva(@PathVariable Long id) {
        try {
            electivaService.desactivarElectiva(id);
            return ResponseEntity.ok("Electiva desactivada exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al desactivar electiva: " + e.getMessage());
        }
    }


    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<?> reactivarElectiva(@PathVariable Long id) {
        try {
            electivaService.reactivarElectiva(id);
            return ResponseEntity.ok("Electiva reactivada exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
