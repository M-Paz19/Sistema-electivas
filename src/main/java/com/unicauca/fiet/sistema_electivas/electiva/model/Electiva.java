package com.unicauca.fiet.sistema_electivas.electiva.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.unicauca.fiet.sistema_electivas.departamento.model.Departamento;
import com.unicauca.fiet.sistema_electivas.electiva.enums.EstadoElectiva;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "electiva")
public class Electiva {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "electiva_id_seq")
    @SequenceGenerator(name = "electiva_id_seq", sequenceName = "electiva_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "codigo", nullable = false, length = Integer.MAX_VALUE, unique = true)
    private String codigo;

    @NotNull
    @Column(name = "nombre", nullable = false, length = Integer.MAX_VALUE, unique = true)
    private String nombre;

    @Column(name = "descripcion", length = Integer.MAX_VALUE)
    private String descripcion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoElectiva estado;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "departamento_id", nullable = false)
    @JsonIgnoreProperties({"electivas", "hibernateLazyInitializer", "handler"})
    private Departamento departamento;
}