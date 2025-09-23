package com.unicauca.fiet.sistema_electivas;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "respuestas_formulario")
public class RespuestasFormulario {
    @Id
    @ColumnDefault("nextval('respuestas_formulario_id_seq')")
    @Column(name = "id", nullable = false)
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
    @Column(name = "codigo_estudiante", nullable = false, length = Integer.MAX_VALUE)
    private String codigoEstudiante;

    @NotNull
    @Column(name = "timestamp_respuesta", nullable = false)
    private Instant timestampRespuesta;

    @NotNull
    @Column(name = "correo_estudiante", nullable = false, length = Integer.MAX_VALUE)
    private String correoEstudiante;

    @NotNull
    @Column(name = "nombre_estudiante", nullable = false, length = Integer.MAX_VALUE)
    private String nombreEstudiante;

    @NotNull
    @Column(name = "apellidos_estudiante", nullable = false, length = Integer.MAX_VALUE)
    private String apellidosEstudiante;

    @NotNull
    @Column(name = "programa_estudiante", nullable = false, length = Integer.MAX_VALUE)
    private String programaEstudiante;

    @NotNull
    @Column(name = "electiva_opcion_1", nullable = false, length = Integer.MAX_VALUE)
    private String electivaOpcion1;

    @NotNull
    @Column(name = "electiva_opcion_2", nullable = false, length = Integer.MAX_VALUE)
    private String electivaOpcion2;

    @NotNull
    @Column(name = "electiva_opcion_3", nullable = false, length = Integer.MAX_VALUE)
    private String electivaOpcion3;

    @NotNull
    @Column(name = "electiva_opcion_4", nullable = false, length = Integer.MAX_VALUE)
    private String electivaOpcion4;

    @NotNull
    @Column(name = "electiva_opcion_5", nullable = false, length = Integer.MAX_VALUE)
    private String electivaOpcion5;

    @NotNull
    @Column(name = "electiva_opcion_6", nullable = false, length = Integer.MAX_VALUE)
    private String electivaOpcion6;

    @NotNull
    @Column(name = "electiva_opcion_7", nullable = false, length = Integer.MAX_VALUE)
    private String electivaOpcion7;

}