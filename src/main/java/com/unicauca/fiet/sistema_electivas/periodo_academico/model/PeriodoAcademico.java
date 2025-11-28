package com.unicauca.fiet.sistema_electivas.periodo_academico.model;

import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "periodo_academico")
public class PeriodoAcademico {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "periodo_academico_seq")
    @SequenceGenerator(name = "periodo_academico_seq", sequenceName = "periodo_academico_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "semestre", nullable = false, length = 6, unique = true)
    private String semestre;

    @NotNull
    @Column(name = "fecha_apertura", nullable = false)
    private Instant fechaApertura;

    @NotNull
    @Column(name = "fecha_cierre", nullable = false)
    private Instant fechaCierre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    private EstadoPeriodoAcademico estado;

    @NotNull
    @Column(name = "opciones_por_programa", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<Long, Integer> opcionesPorPrograma = new HashMap<>();

    @Column(name = "url_formulario")
    private String urlFormulario;

    @Column(name = "form_id")
    private String formId;

}