package com.unicauca.fiet.sistema_electivas.model;

import com.unicauca.fiet.sistema_electivas.enums.EstadoElectivaOfertada;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "electiva_ofertada")
public class ElectivaOfertada {
    @Id
    @ColumnDefault("nextval('electiva_ofertada_id_seq')")
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "electiva_id", nullable = false)
    private Electiva electiva;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "periodo_id", nullable = false)
    private PeriodoAcademico periodo;

    @NotNull
    @Column(name = "cupos_por_programa", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<Long, Integer> cuposPorPrograma;

    @NotNull
    @Column(name = "estado", nullable = false, length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private EstadoElectivaOfertada estado;

    @NotNull
    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @NotNull
    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion;

}