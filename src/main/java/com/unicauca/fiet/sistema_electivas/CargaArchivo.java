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
@Table(name = "carga_archivos")
public class CargaArchivo {
    @Id
    @ColumnDefault("nextval('carga_archivos_id_seq')")
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "periodo_id", nullable = false)
    private com.unicauca.fiet.sistema_electivas.PeriodoAcademico periodo;

    @NotNull
    @Column(name = "tipo_archivo", nullable = false, length = Integer.MAX_VALUE)
    private String tipoArchivo;

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
    @Column(name = "estado", nullable = false, length = Integer.MAX_VALUE)
    private String estado;

}