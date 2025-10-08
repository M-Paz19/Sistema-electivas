package com.unicauca.fiet.sistema_electivas.departamento.controller;

import com.unicauca.fiet.sistema_electivas.common.dto.MensajeResponse;
import com.unicauca.fiet.sistema_electivas.common.exception.GlobalExceptionHandler;
import com.unicauca.fiet.sistema_electivas.departamento.dto.DepartamentoRequestDTO;
import com.unicauca.fiet.sistema_electivas.departamento.dto.DepartamentoResponse;
import com.unicauca.fiet.sistema_electivas.departamento.service.DepartamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * Controlador REST para gestionar los departamentos académicos.
 *
 * Expone operaciones CRUD (crear, consultar, actualizar, deshabilitar).
 * Las excepciones son manejadas globalmente en {@link GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/departamentos")
@RequiredArgsConstructor
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    /**
     * Crea un nuevo departamento.
     *
     * @param dto datos del nuevo departamento (validados con {@link Valid})
     * @return el departamento creado
     */
    @PostMapping
    public ResponseEntity<DepartamentoResponse> crearDepartamento(@Valid @RequestBody DepartamentoRequestDTO dto) {
        DepartamentoResponse nuevoDepto = departamentoService.crearDepartamento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoDepto);
    }

    /**
     * Endpoint que permite listar departamentos según filtros opcionales.
     *
     * @param estado filtro de estado (por defecto: TODOS)
     * @param query  búsqueda parcial por nombre o código
     * @return lista de departamentos filtrados o 204 si no hay resultados
     */
    @GetMapping
    public ResponseEntity<List<DepartamentoResponse>> buscarDepartamentos(
            @RequestParam(defaultValue = "TODOS") String estado,
            @RequestParam(required = false) String query) {

        List<DepartamentoResponse> departamentos = departamentoService.findDepartamentos(estado, query);

        if (departamentos.isEmpty() && query != null && !query.isBlank()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(departamentos);
    }

    /**
     * Endpoint para obtener un departamento por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DepartamentoResponse> obtenerDepartamentoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(departamentoService.buscarPorId(id));
    }

    /**
     * Actualiza los datos de un departamento existente.
     *
     * @param id identificador del departamento
     * @param dto nuevos datos del departamento
     * @return el departamento actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<DepartamentoResponse> actualizarDepartamento(
            @PathVariable Long id,
            @Valid @RequestBody DepartamentoRequestDTO dto) {
        DepartamentoResponse deptoActualizado = departamentoService.actualizarDepartamento(id, dto);
        return ResponseEntity.ok(deptoActualizado);
    }

    /**
     * Deshabilita un departamento (soft delete).
     *
     * @param id identificador del departamento a deshabilitar
     * @return mensaje de confirmación
     */
    @PatchMapping("/{id}/deshabilitar")
    public ResponseEntity<MensajeResponse> deshabilitarDepartamento(@PathVariable Long id) {
        departamentoService.deshabilitarDepartamento(id);
        return ResponseEntity.ok(new MensajeResponse("Departamento deshabilitado exitosamente"));
    }

}