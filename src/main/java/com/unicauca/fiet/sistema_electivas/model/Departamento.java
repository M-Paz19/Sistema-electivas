package com.unicauca.fiet.sistema_electivas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "departamento")
public class Departamento {
    public enum EstadoDepartamento {
        ACTIVO,
        INACTIVO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "departamento_id_seq")
    @SequenceGenerator(name = "departamento_id_seq", sequenceName = "departamento_id_seq", allocationSize = 1)
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
    private EstadoDepartamento estado;

    @OneToMany(mappedBy = "departamento")
    @JsonIgnore
    private List<Electiva> electivas;
}