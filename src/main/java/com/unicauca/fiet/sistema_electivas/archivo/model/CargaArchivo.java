package com.unicauca.fiet.sistema_electivas.archivo.model;

import com.unicauca.fiet.sistema_electivas.archivo.enums.EstadoArchivo;
import com.unicauca.fiet.sistema_electivas.archivo.enums.TipoArchivo;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "carga_archivos")
public class CargaArchivo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "carga_archivos_seq")
    @SequenceGenerator(name = "carga_archivos_seq", sequenceName = "carga_archivos_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;


    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "periodo_id", nullable = false)
    private PeriodoAcademico periodo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_archivo", nullable = false)
    private TipoArchivo tipoArchivo;

    @NotNull
    @Column(name = "nombre_archivo", nullable = false, length = Integer.MAX_VALUE)
    private String nombreArchivo;

    @NotNull
    @Column(name = "ruta_almacenamiento", nullable = false, length = Integer.MAX_VALUE)
    private String rutaAlmacenamiento;

    @NotNull
    @Column(name = "fecha_carga", nullable = false)
    private Instant fechaCarga;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoArchivo estado;
}