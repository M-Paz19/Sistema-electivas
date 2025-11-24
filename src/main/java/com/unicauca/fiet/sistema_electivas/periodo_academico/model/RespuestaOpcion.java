package com.unicauca.fiet.sistema_electivas.periodo_academico.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "respuesta_opcion",
       uniqueConstraints = @UniqueConstraint(columnNames = {"respuesta_id", "opcion_num"}))
public class RespuestaOpcion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "respuesta_opcion_seq")
    @SequenceGenerator(name = "respuesta_opcion_seq", sequenceName = "respuesta_opcion_seq", allocationSize = 1)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "respuesta_id", nullable = false)
    private RespuestasFormulario respuesta;

    @NotNull
    @Column(name = "opcion_num", nullable = false)
    private Integer opcionNum; // Ej: 1, 2, 3...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_id")
    private Oferta oferta;
}
