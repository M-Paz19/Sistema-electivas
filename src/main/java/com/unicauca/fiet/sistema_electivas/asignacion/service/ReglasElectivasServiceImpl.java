package com.unicauca.fiet.sistema_electivas.asignacion.service;

import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanEstudio;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReglasElectivasServiceImpl implements  ReglasElectivasService{

    /**
     * {@inheritDoc}
     */
    public int calcularCantidadElectivasAAsignar(DatosAcademico dato) {
        // Seguridad básica: si no hay plan o map, usar electivasRequeridas como fallback

        // ------------------------------------------------------------
        // CASO 1: NIVELADO
        // ------------------------------------------------------------
        if (Boolean.TRUE.equals(dato.getEsNivelado())) {

            return calcularElectivasParaNivelado(dato); // según pensum
        }
        Integer totalElectivas = dato.getPlanEstudios().getElectivasRequeridas();

        // Electivas faltantes = debeVer - aprobadas
        int faltantes = totalElectivas - dato.getAprobadas();
        // ------------------------------------------------------------
        // CASO 2: No nivelado con avance == 100%
        // ------------------------------------------------------------
        if (dato.getPorcentajeAvance() != null &&
                dato.getPorcentajeAvance().compareTo(BigDecimal.valueOf(100)) == 0) {
            // Nunca valores negativos
            return Math.max(0, faltantes);
        }

        // ------------------------------------------------------------
        // CASO 3: No nivelado, que le faltan menos de 18 creditos obligatorios
        // ------------------------------------------------------------
        // Créditos totales del plan
        int creditosTotales = dato.getPlanEstudios().getCreditosTotalesPlan();
        int creditosElectivas = dato.getPlanEstudios().getElectivasRequeridas()*3;
        int creditosTG = dato.getPlanEstudios().getCreditosTrabajoGrado();

        // Ajuste: créditos obligatorios del plan
        int creditosObligatorios = creditosTotales - creditosElectivas - creditosTG;

        // Ajuste: créditos aprobados del estudiante sin electivas
        int creditosEstudianteObligatorios =
                dato.getCreditosAprobados() - (dato.getAprobadas() * 3);

        // Créditos obligatorios faltantes
        int obligatoriosFaltantes = creditosObligatorios - creditosEstudianteObligatorios;
        if (18 > obligatoriosFaltantes) {

            // Cupo regular por semestre (18 créditos)
            int creditosLibres = 18 - obligatoriosFaltantes;

            // Electivas posibles dentro del cupo
            int electivasPorCreditos = creditosLibres / 3; // cada electiva = 3 créditos

            // No puede exceder las electivas que realmente le faltan
            int maximoSegunFaltantes = Math.min(electivasPorCreditos, faltantes);
            // Por norma siempre se intenta asignar al menos 2
            return Math.max(maximoSegunFaltantes, 2);
        }

        // ------------------------------------------------------------
        // CASO 4: No nivelado, avance bajo
        // ------------------------------------------------------------
        // 1. Asignar máximo 2 electivas directas
        // Retornar objeto de respuesta (o lo que uses)
        return Math.min(faltantes, 2);
    }

    /**
     * Determina cuántas electivas debe ver un estudiante **nivelado** según
     * la configuración {@code electivasPorSemestre} del {@link PlanEstudio}.
     *
     * <p>Reglas aplicadas:</p>
     * <ol>
     *   <li>Se calcula el semestre objetivo como {@code periodosMatriculados + 1}
     *       (ej: si periodosMatriculados == 7, el semestre objetivo es 8).</li>
     *   <li>Se busca la clave String del semestre en el mapa {@code electivasPorSemestre}
     *       (ej: "8", "9", "10").</li>
     *   <li>Si el semestre objetivo es menor que la menor clave disponible → se usa
     *       el valor de la menor clave.</li>
     *   <li>Si el semestre objetivo es mayor que la mayor clave disponible → se usa
     *       el valor de la mayor clave.</li>
     *   <li>El resultado se limita por:
     *       <ul>
     *         <li>No exceder {@code plan.getElectivasRequeridas()}</li>
     *         <li>No exceder {@code dato.getFaltan()} (electivas que le faltan al estudiante)</li>
     *         <li>No retornar valores negativos</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * @param dato entidad {@link DatosAcademico} (debe contener planEstudios, aprobadas, periodosMatriculados)
     * @return número de electivas que se deben intentar asignar al estudiante nivelado (>= 0)
     */
    private int calcularElectivasParaNivelado(DatosAcademico dato) {
        // Seguridad básica: si no hay plan o map, usar electivasRequeridas como fallback
        PlanEstudio plan = dato.getPlanEstudios();


        Map<String, Object> electivasMap = plan.getElectivasPorSemestre();
        Integer electivasRequeridas = plan.getElectivasRequeridas();

        // Semestre objetivo: si ha cursado N periodos, entra al semestre N+1
        int semestreObjetivo = (dato.getPeriodosMatriculados() != null ? dato.getPeriodosMatriculados() : 0) + 1;
        String claveObjetivo = String.valueOf(semestreObjetivo);

        Integer valorSeleccionado = null;

        // Si no existe el mapa o está vacío, fallback a electivasRequeridas
        if (electivasMap == null || electivasMap.isEmpty()) {
            valorSeleccionado = electivasRequeridas;
        } else {
            // Convertir claves a enteros ordenados para poder comparar rangos
            List<Integer> semestres = electivasMap.keySet().stream()
                    .map(key -> {
                        try {
                            return Integer.valueOf(key);
                        } catch (NumberFormatException ex) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .sorted()
                    .collect(Collectors.toList());

            if (semestres.isEmpty()) {
                // No hay claves numéricas válidas -> fallback
                valorSeleccionado = electivasRequeridas;
            } else {
                // Si existe la clave exacta (por ejemplo "8")
                if (electivasMap.containsKey(claveObjetivo)) {
                    valorSeleccionado = parseIntegerSafe(electivasMap.get(claveObjetivo));
                } else {
                    // Si no existe, seleccionar el valor de la clave más cercana por rango:
                    int menor = semestres.get(0);
                    int mayor = semestres.get(semestres.size() - 1);

                    if (semestreObjetivo <= menor) {
                        valorSeleccionado = parseIntegerSafe(electivasMap.get(String.valueOf(menor)));
                    } else if (semestreObjetivo >= mayor) {
                        valorSeleccionado = parseIntegerSafe(electivasMap.get(String.valueOf(mayor)));
                    } else {
                        // Está entre claves (ej: semestres = [8,9,10] y objetivo = 8.5 no posible, pero manejamos)
                        // Tomamos la clave menor inmediata (ej: si objetivo 8->8 existe, si 8 no -> ya manejado)
                        // Como no hay decimal, llegamos aquí si hay claves no contiguas; tomamos la menor mayor que objetivo
                        Optional<Integer> mayorInmediato = semestres.stream()
                                .filter(s -> s >= semestreObjetivo)
                                .findFirst();
                        if (mayorInmediato.isPresent()) {
                            valorSeleccionado = parseIntegerSafe(electivasMap.get(String.valueOf(mayorInmediato.get())));
                        } else {
                            valorSeleccionado = electivasRequeridas;
                        }
                    }
                }
            }
        }

        // Si no se pudo determinar valor seleccionado, fallback a electivasRequeridas o 0
        if (valorSeleccionado == null) {
            valorSeleccionado = electivasRequeridas != null ? electivasRequeridas : 0;
        }

        // Asegurarse no devolver negativos
        valorSeleccionado = Math.max(0, valorSeleccionado);

        // No asignar más de las electivas que realmente le faltan al estudiante
        Integer totalElectivas = dato.getPlanEstudios().getElectivasRequeridas();

        // Electivas faltantes = debeVer - aprobadas
        int faltantes = totalElectivas - dato.getAprobadas();
        int resultado = Math.min(valorSeleccionado, faltantes);

        // Además limitar por electivasRequeridas si está definido
        if (electivasRequeridas != null) {
            resultado = Math.min(resultado, electivasRequeridas);
        }

        // Resultado final (>= 0)
        return Math.max(0, resultado);
    }

    /**
     * Parsea de forma segura un Object a Integer.
     * Acepta Number, String que represente entero, y devuelve null si no puede parsear.
     */
    private Integer parseIntegerSafe(Object o) {
        if (o == null) return null;
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        if (o instanceof String) {
            try {
                return Integer.valueOf((String) o);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }
}
