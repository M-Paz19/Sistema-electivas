package com.unicauca.fiet.sistema_electivas;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "asignacion_electiva")
public class AsignacionElectiva {
    @Id
    @ColumnDefault("nextval('asignacion_electiva_id_seq')")
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "electiva_ofertada_id", nullable = false)
    private com.unicauca.fiet.sistema_electivas.ElectivaOfertada electivaOfertada;

    @NotNull
    @Column(name = "estudiante_codigo", nullable = false, length = Integer.MAX_VALUE)
    private String estudianteCodigo;

    @NotNull
    @Column(name = "numero_opcion", nullable = false)
    private Integer numeroOpcion;

    @NotNull
    @Column(name = "estado_asignacion", nullable = false, length = Integer.MAX_VALUE)
    private String estadoAsignacion;

    @NotNull
    @Column(name = "fecha_asignacion", nullable = false)
    private Instant fechaAsignacion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "archivo_cargado_id", nullable = false)
    private com.unicauca.fiet.sistema_electivas.CargaArchivo archivoCargado;

}