package com.unicauca.fiet.sistema_electivas.periodo_academico.dto;

import lombok.*;

/**

 DTO de respuesta utilizado para notificar de un cambio de estado.

 <p>Contiene la información mínima necesaria para saber acerca de cambio de estado de un objeto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CambioEstadoResponse {
    private Long periodoId;
    private String semestre;
    private String nuevoEstado;
    private String mensaje;
    private String urlFormulario;
}

