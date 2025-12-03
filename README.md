# Sistema de Gestión Académica - Backend

API REST desarrollada con Spring Boot para la gestión de estudiantes, docentes, asignaturas y procesos académicos del sistema.

## Requisitos
- Java JDK 17+
- Maven
- MySQL

## Instalación
git clone https://github.com/M-Paz19/sistema-backend.git
cd sistema-backend
mvn clean install

## Configuración
Editar src/main/resources/application.properties:

spring.datasource.url=jdbc:postgresql://localhost:5432/electivas_demo?rewriteBatchedInserts=true
spring.datasource.username=tu_username
spring.datasource.password=tu_clave

## Ejecutar
mvn spring-boot:run

## Frontend relacionado
Repositorio: https://github.com/M-Paz19/sistema-frontend
