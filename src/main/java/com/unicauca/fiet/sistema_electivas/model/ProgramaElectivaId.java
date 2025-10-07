package com.unicauca.fiet.sistema_electivas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@Embeddable
public class ProgramaElectivaId implements Serializable {
    private static final long serialVersionUID = -5716607302308062657L;
    @NotNull
    @Column(name = "programa_id", nullable = false)
    private Long programaId;

    @NotNull
    @Column(name = "electiva_id", nullable = false)
    private Long electivaId;

    public ProgramaElectivaId(Long programaId, Long electivaId) {
        this.programaId = programaId;
        this.electivaId = electivaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ProgramaElectivaId entity = (ProgramaElectivaId) o;
        return Objects.equals(this.electivaId, entity.electivaId) &&
                Objects.equals(this.programaId, entity.programaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(electivaId, programaId);
    }

}