package com.unicauca.fiet.sistema_electivas;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "plan_estudios")
public class PlanEstudio {
    @Id
    @ColumnDefault("nextval('plan_estudios_id_seq')")
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "nombre", nullable = false, length = Integer.MAX_VALUE)
    private String nombre;

    @NotNull
    @Column(name = "version", nullable = false, length = Integer.MAX_VALUE)
    private String version;

    @NotNull
    @Column(name = "estado", nullable = false, length = Integer.MAX_VALUE)
    private String estado;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programa_id", nullable = false)
    private com.unicauca.fiet.sistema_electivas.Programa programa;

    @NotNull
    @Column(name = "electivas_por_semestre", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> electivasPorSemestre;

    @NotNull
    @Column(name = "reglas_nivelacion", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> reglasNivelacion;

    @NotNull
    @Column(name = "electivas_requeridas", nullable = false)
    private Integer electivasRequeridas;

    @NotNull
    @Column(name = "creditos_totales_plan", nullable = false)
    private Integer creditosTotalesPlan;

    @NotNull
    @Column(name = "creditos_trabajo_grado", nullable = false)
    private Integer creditosTrabajoGrado;

}