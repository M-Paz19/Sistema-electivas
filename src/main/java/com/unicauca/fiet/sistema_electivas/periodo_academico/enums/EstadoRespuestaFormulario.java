package com.unicauca.fiet.sistema_electivas.periodo_academico.enums;

import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;

/**
 * Enum que representa los posibles estados de una {@link RespuestasFormulario}.
 *
 * <p>Este estado refleja la etapa y resultado del procesamiento de la respuesta
 * enviada por un estudiante en el formulario de selección de electivas.</p>
 *
 * <ul>
 *   <li>SIN_PROCESAR: Respuesta recién cargada, aún no analizada.</li>
 *   <li>DUPLICADO: Respuesta descartada por existir otra más antigua del mismo estudiante.</li>
 *   <li>UNICO: Respuesta única (no hay duplicados para ese código).</li>
 *   <li>FORMATO_INVALIDO: Código estudiantil con formato desconocido o no estándar; requiere revisión manual.</li>
 *   <li>NO_CUMPLE: El estudiante no cumple el requisito de antigüedad (menos de 7 semestres cursados).</li>
 *   <li>CUMPLE: El estudiante cumple el requisito de antigüedad (7 o más semestres cursados).</li>
 *   <li>DESCARTADO: Estudiante descartado manualmente por el administrador.</li>
 *   <li>INCLUIDO: Estudiante incluido manualmente para validación en SIMCA.</li>
 *   <li>VALIDO: Respuesta verificada y aceptada para procesamiento final.</li>
 * </ul>
 */
public enum EstadoRespuestaFormulario {
    SIN_PROCESAR("sin procesar"),
    DUPLICADO("duplicado"),
    UNICO("sin duplicados"),
    FORMATO_INVALIDO("formato desconocido código estudiantil- revisión manual"),
    NO_CUMPLE("código estudiantil no valido por antigüedad"),
    CUMPLE("código estudiantil valido"),
    DESCARTADO("descartado manualmente"),
    INCLUIDO("incluido manualmente"),

    INCONSISTENTE_SIMCA("El codigo estudiantil no aparece existe o esta inactivo, sin información válida en SIMCA."),
    CORREGIDO("El código era INCONSISTENTE, pero fue corregido manualmente"),
    DATOS_NO_CARGADOS("El código de la respuesta no se encontro en el archivo de SIMCA cargado"),
    DESCARTADO_SIMCA("El código no existe, no se encontró o está inactivo según los datos de SIMCA."),
    FORZAR_INCLUSION("Se forzó la inclusión de un estudiante que era inconsistente"),
    PROGRAMA_NO_ENCONTRADO("No se encontró un programa registrado que coincida con los datos suministrados por SIMCA."),
    PLAN_NO_ENCONTRADO("Se encontró el programa, pero no existe un plan de estudios activo o apropiado para el año de ingreso."),
    DATOS_CARGADOS("Se cargaron sus datos academicos para la respuesta del formulario");


    private final String descripcion;

    EstadoRespuestaFormulario(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}