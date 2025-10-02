package com.unicauca.fiet.sistema_electivas.model;

import com.unicauca.fiet.sistema_electivas.enums.EstadoPrograma;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "programa")
public class Programa {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "programa_seq")
    @SequenceGenerator(name = "programa_seq", sequenceName = "programa_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "codigo", nullable = false, length = Integer.MAX_VALUE)
    private String codigo;

    @NotNull
    @Column(name = "nombre", nullable = false, length = Integer.MAX_VALUE)
    private String nombre;

    @NotNull
    @Column(name = "estado", nullable = false, length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private EstadoPrograma estado;

}