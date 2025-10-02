package com.unicauca.fiet.sistema_electivas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "programa_electiva")
public class ProgramaElectiva {
    @EmbeddedId
    private ProgramaElectivaId id;

    @MapsId("programaId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programa_id", nullable = false)
    private Programa programa;

    @MapsId("electivaId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "electiva_id", nullable = false)
    private Electiva electiva;

}