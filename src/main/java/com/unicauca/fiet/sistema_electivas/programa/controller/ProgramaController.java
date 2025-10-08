package com.unicauca.fiet.sistema_electivas.programa.controller;

import com.unicauca.fiet.sistema_electivas.programa.dto.ProgramaDisableResponse;
import com.unicauca.fiet.sistema_electivas.programa.dto.ProgramaRequest;
import com.unicauca.fiet.sistema_electivas.programa.dto.ProgramaResponse;
import com.unicauca.fiet.sistema_electivas.programa.dto.ProgramaUpdateRequest;
import com.unicauca.fiet.sistema_electivas.programa.enums.EstadoPrograma;
import com.unicauca.fiet.sistema_electivas.programa.service.ProgramaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de Programas Académicos.
 * Expone los endpoints para crear, editar y deshabilitar programas.
 */
@RestController
@RequestMapping("/api/programas")
@RequiredArgsConstructor
public class ProgramaController {

    private final ProgramaService programaService;

    /**
     * Crea un nuevo programa académico.
     *
     * @param request Datos del programa a crear.
     * @return ProgramaResponse con la información del programa creado.
     */
    @PostMapping
    public ResponseEntity<ProgramaResponse> crearPrograma(
            @Valid @RequestBody ProgramaRequest request) {
        ProgramaResponse response = programaService.crearPrograma(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Edita la información de un programa existente.
     *
     * @param id      Identificador del programa.
     * @param request Datos a actualizar (principalmente el nombre).
     * @return ProgramaResponse con la información actualizada.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProgramaResponse> editarPrograma(
            @PathVariable Long id,
            @Valid @RequestBody ProgramaUpdateRequest request) {
        ProgramaResponse response = programaService.editarPrograma(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deshabilita un programa académico (cambia su estado a DESHABILITADO).
     *
     * @param id Identificador del programa.
     * @return ProgramaDisableResponse con la confirmación de deshabilitación.
     */
    @PatchMapping("/{id}/deshabilitar")
    public ResponseEntity<ProgramaDisableResponse> deshabilitar(@PathVariable Long id) {
        ProgramaDisableResponse response = programaService.deshabilitarPrograma(id);
        return ResponseEntity.ok(response);
    }
    /**
     * Obtiene la lista de todos los programas académicos registrados.
     *
     * @return Lista de ProgramaResponse con la información de cada programa.
     */
    @GetMapping
    public ResponseEntity<List<ProgramaResponse>> listarProgramas() {
        return ResponseEntity.ok(programaService.listarProgramas());
    }

    /**
     * Busca programas académicos filtrados por su estado.
     *
     * @param estado Estado del programa (ejemplo: APROBADO, PENDIENTE_PLAN, DESHABILITADO).
     * @return Lista de ProgramaResponse con los programas que cumplen el criterio.
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ProgramaResponse>> buscarPorEstado(@PathVariable EstadoPrograma estado) {
        return ResponseEntity.ok(programaService.buscarPorEstado(estado));
    }

    /**
     * Busca programas académicos cuyo nombre coincida parcial o totalmente
     * con el texto ingresado.
     *
     * @param nombre Texto a buscar dentro del nombre de los programas.
     * @return Lista de ProgramaResponse con los programas encontrados.
     */
    @GetMapping("/buscar/nombre")
    public ResponseEntity<List<ProgramaResponse>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(programaService.buscarPorNombre(nombre));
    }

    /**
     * Busca programas académicos cuyo código coincida parcial o totalmente
     * con el texto ingresado.
     *
     * @param codigo Texto a buscar dentro del código de los programas.
     * @return Lista de ProgramaResponse con los programas encontrados.
     */
    @GetMapping("/buscar/codigo")
    public ResponseEntity<List<ProgramaResponse>> buscarPorCodigo(@RequestParam String codigo) {
        return ResponseEntity.ok(programaService.buscarPorCodigo(codigo));
    }
}