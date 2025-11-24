package com.unicauca.fiet.sistema_electivas.asignacion.enums;

public enum ResultadoAsignacion {
    ASIGNADA,           // Se asignó cupo directo
    LISTA_ESPERA,       // Entró a lista de espera
    PROGRAMA_INCOMPATIBLE,
    SIN_CUPO,           // No tiene cupo y no hay lista de espera
    OPCION_DUPLICADA,
    ERROR               // Error inesperado en el proceso
}
