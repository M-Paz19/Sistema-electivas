package com.unicauca.fiet.sistema_electivas.controller;

import com.unicauca.fiet.sistema_electivas.model.Departamento;
import com.unicauca.fiet.sistema_electivas.service.DepartamentoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
public class DepartamentoController {

    @Autowired
    private DepartamentoServiceImpl departamentoService;

    @PostMapping
    public ResponseEntity<?> crearDepartamento(@RequestBody Departamento departamento) {
        try {
            Departamento nuevoDepto = departamentoService.crearDepartamento(departamento);
            return new ResponseEntity<>(nuevoDepto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Departamento>> buscarDepartamentos(
            @RequestParam(required = false, defaultValue = "TODOS") String estado,
            @RequestParam(required = false) String query) {
        List<Departamento> departamentos = departamentoService.findDepartamentos(estado, query);
        if (departamentos.isEmpty() && query != null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(departamentos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarDepartamento(
            @PathVariable Long id,
            @RequestBody Departamento departamento) {
        try {
            Departamento deptoActualizado = departamentoService.actualizarDepartamento(id, departamento);
            return ResponseEntity.ok(deptoActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar departamento: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deshabilitarDepartamento(@PathVariable Long id) {
        departamentoService.deshabilitarDepartamento(id);
        return ResponseEntity.ok("Departamento deshabilitado exitosamente");
    }
}