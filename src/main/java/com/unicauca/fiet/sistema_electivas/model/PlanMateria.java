package com.unicauca.fiet.sistema_electivas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "plan_materia")
public class PlanMateria {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "plan_materia_seq")
    @SequenceGenerator(name = "plan_materia_seq", sequenceName = "plan_materia_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_estudios_id", nullable = false)
    private PlanEstudio planEstudios;

    @NotNull
    @Column(name = "nombre", nullable = false, length = Integer.MAX_VALUE)
    private String nombre;

    @NotNull
    @Column(name = "semestre", nullable = false)
    private Integer semestre;

    @NotNull
    @Column(name = "tipo", nullable = false, length = Integer.MAX_VALUE)
    private String tipo;// OBLIGATORIA | ELECTIVA | TRABAJO_GRADO

    @NotNull
    @Column(name = "creditos", nullable = false)
    private Integer creditos;

}