package com.unicauca.fiet.sistema_electivas.model;

import com.unicauca.fiet.sistema_electivas.enums.EstadoPeriodoAcademico;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

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
    @Column(name = "semestre", nullable = false, length = Integer.MAX_VALUE)
    private String semestre;

    @NotNull
    @Column(name = "fecha_apertura", nullable = false)
    private Instant fechaApertura;

    @NotNull
    @Column(name = "fecha_cierre", nullable = false)
    private Instant fechaCierre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoPeriodoAcademico estado;

}