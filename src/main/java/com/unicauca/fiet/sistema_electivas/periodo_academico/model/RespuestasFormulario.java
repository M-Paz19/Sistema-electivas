package com.unicauca.fiet.sistema_electivas.periodo_academico.model;

import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;
import com.unicauca.fiet.sistema_electivas.programa.model.Programa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "respuestas_formulario")
public class RespuestasFormulario {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "respuestas_formulario_seq")
    @SequenceGenerator(name = "respuestas_formulario_seq", sequenceName = "respuestas_formulario_seq", allocationSize = 1)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "periodo_id", nullable = false)
    private PeriodoAcademico periodo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "archivo_cargado_id", nullable = false)
    private CargaArchivo archivoCargado;

    @NotNull
    @Column(name = "codigo_estudiante", nullable = false)
    private String codigoEstudiante;

    @NotNull
    @Column(name = "timestamp_respuesta", nullable = false)
    private Instant timestampRespuesta = Instant.now();

    @NotNull
    @Column(name = "correo_estudiante", nullable = false)
    private String correoEstudiante;

    @NotNull
    @Column(name = "nombre_estudiante", nullable = false)
    private String nombreEstudiante;

    @NotNull
    @Column(name = "apellidos_estudiante", nullable = false)
    private String apellidosEstudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id")
    private Programa programa;

    @OneToMany(mappedBy = "respuesta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RespuestaOpcion> opciones = new ArrayList<>();

}