package com.unicauca.fiet.sistema_electivas.programa.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProgramaSiglaUtil {

    public static String generarSiglasProgramas(List<String> programas) {
        if (programas == null || programas.isEmpty()) return "[]";

        String siglas = programas.stream()
                .map(ProgramaSiglaUtil::obtenerSigla)
                .collect(Collectors.joining("-"));

        return "[" + siglas + "]";
    }

    public static String obtenerSigla(String nombre) {
        if (nombre == null || nombre.isEmpty()) return "";

        StringBuilder sb = new StringBuilder("P");

        Arrays.stream(nombre.split("\\s+"))
                .filter(p -> !p.equalsIgnoreCase("de")
                        && !p.equalsIgnoreCase("y")
                        && !p.equalsIgnoreCase("en"))
                .forEach(p -> sb.append(Character.toUpperCase(p.charAt(0))));

        return sb.toString();
    }
}
