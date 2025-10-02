package com.unicauca.fiet.sistema_electivas.model;

import com.unicauca.fiet.sistema_electivas.enums.EstadoPlanEstudio;
import com.unicauca.fiet.sistema_electivas.enums.EstadoPrograma;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "plan_estudios_seq")
    @SequenceGenerator(name = "plan_estudios_seq", sequenceName = "plan_estudios_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @NotNull
    @Column(name = "version", nullable = false)
    private String version;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, columnDefinition = "varchar default 'CONFIGURACION_PENDIENTE'")
    private EstadoPlanEstudio estado = EstadoPlanEstudio.CONFIGURACION_PENDIENTE;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programa_id", nullable = false)
    private Programa programa;

    // Campos configurables después
    @Column(name = "electivas_por_semestre")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> electivasPorSemestre;

    @Column(name = "reglas_nivelacion")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> reglasNivelacion;

    @Column(name = "electivas_requeridas")
    private Integer electivasRequeridas;

    @Column(name = "creditos_totales_plan")
    private Integer creditosTotalesPlan;

    @Column(name = "creditos_trabajo_grado")
    private Integer creditosTrabajoGrado;
}
