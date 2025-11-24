package com.unicauca.fiet.sistema_electivas.asignacion.model;

import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;
import com.unicauca.fiet.sistema_electivas.asignacion.enums.EstadoAsignacion;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.Oferta;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "asignacion_electiva")
public class AsignacionElectiva {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "asignacion_electiva_id_seq")
    @SequenceGenerator(
            name = "asignacion_electiva_id_seq",
            sequenceName = "asignacion_electiva_id_seq",
            allocationSize = 50
    )
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oferta_id", nullable = false)
    private Oferta oferta;

    @NotNull
    @Column(name = "estudiante_codigo", nullable = false, length = Integer.MAX_VALUE)
    private String estudianteCodigo;

    @NotNull
    @Column(name = "numero_opcion", nullable = false)
    private Integer numeroOpcion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_asignacion", nullable = false)
    private EstadoAsignacion estadoAsignacion;

    @NotNull
    @Column(name = "fecha_asignacion", nullable = false)
    private Instant fechaAsignacion;
}