package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model;

import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;
import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanEstudio;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.enums.EstadoAptitud;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "datos_academicos")
public class DatosAcademico {
    @Id
    @ColumnDefault("nextval('datos_academicos_id_seq')")
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "codigo_estudiante", nullable = false, length = Integer.MAX_VALUE)
    private String codigoEstudiante;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_estudios_id", nullable = false)
    private PlanEstudio planEstudios;

    @NotNull
    @Column(name = "nombres", nullable = false, length = Integer.MAX_VALUE)
    private String nombres;

    @NotNull
    @Column(name = "apellidos", nullable = false, length = Integer.MAX_VALUE)
    private String apellidos;

    @NotNull
    @Column(name = "usuario", nullable = false, length = Integer.MAX_VALUE)
    private String usuario;

    @NotNull
    @Column(name = "programa", nullable = false, length = Integer.MAX_VALUE)
    private String programa;

    @NotNull
    @Column(name = "creditos_aprobados", nullable = false)
    private Integer creditosAprobados;

    @NotNull
    @Column(name = "periodos_matriculados", nullable = false)
    private Integer periodosMatriculados;

    @NotNull
    @Column(name = "promedio_carrera", nullable = false, precision = 5, scale = 2)
    private BigDecimal promedioCarrera;

    @NotNull
    @Column(name = "aprobadas", nullable = false)
    private Integer aprobadas;

    @NotNull
    @Column(name = "es_nivelado", nullable = false)
    private Boolean esNivelado = false;

    @NotNull
    @Column(name = "porcentaje_avance", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeAvance;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_aptitud", nullable = false, length = Integer.MAX_VALUE)
    private EstadoAptitud estadoAptitud;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "archivo_cargado_id", nullable = false)
    private CargaArchivo archivoCargado;

}