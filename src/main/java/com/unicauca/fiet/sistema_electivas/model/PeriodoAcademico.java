package com.unicauca.fiet.sistema_electivas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    @ColumnDefault("nextval('periodo_academico_id_seq')")
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
    @Column(name = "estado", nullable = false, length = Integer.MAX_VALUE)
    private String estado;

}