package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto;

import lombok.*;

/**
 * DTO utilizado para notificar un cambio de estado en un período académico.
 *
 * <p>Contiene información básica sobre el nuevo estado y un mensaje descriptivo.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambioEstadoValidacionResponse {
    private Long periodoId;
    private String semestre;
    private String nuevoEstado;
    private String mensaje;
}
