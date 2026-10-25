---
name: domain-ontology
description: Construye y mantiene el modelo de dominio de gestión de guardias (glosario, entidades, relaciones, estados e invariantes) a partir de reglas validadas y evidencias legacy. Usar al especificar, diseñar APIs o modelar entidades del nuevo sistema.
---

# Ontología del dominio

## Objetivo
Establecer un lenguaje ubicuo y un modelo conceptual compartido entre negocio, especificaciones, contratos OpenAPI y código.

## Entradas
- Reglas validadas del catálogo de negocio.
- Evidencias de `legacy-discovery` y `business-rule-mining`.
- Esquema MySQL y sqlmaps de iBATIS.
- Especificación activa en `specs/`.

## Procedimiento
1. Elabora el glosario: término, definición, sinónimos legacy (nombres de tablas, columnas, clases) y fuente.
2. Identifica entidades, agregados y objetos de valor, con sus atributos relevantes.
3. Define relaciones y cardinalidades, justificadas con evidencia.
4. Modela ciclos de vida y estados con sus transiciones permitidas.
5. Enumera invariantes, enlazando cada una a una regla del catálogo.
6. Mapea el modelo conceptual al legacy (tablas/clases) y al objetivo (DTOs, entidades Spring Boot, tipos TypeScript, esquemas OpenAPI).
7. Registra ambigüedades y conflictos terminológicos para aclarar con negocio.

## Salida
- Glosario del dominio.
- Diagrama/descripción de entidades y relaciones.
- Tabla de correspondencias legacy ↔ dominio ↔ objetivo.
- Lista de ambigüedades pendientes.

## Restricciones
- Las invariantes deben proceder de reglas validadas; las no validadas se marcan como provisionales.
- No cambiar contratos ni esquemas de datos fuera del alcance aprobado.
