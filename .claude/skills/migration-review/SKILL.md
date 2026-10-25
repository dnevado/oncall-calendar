---
name: migration-review
description: Revisa un incremento de migración (React + TypeScript, Spring Boot) frente a la especificación, las reglas validadas, el contrato OpenAPI y el comportamiento legacy. Usar antes de dar por cerrado un cambio o abrir un PR de migración.
---

# Revisión de migración

## Objetivo
Verificar que el incremento migrado preserva el comportamiento acordado, respeta las restricciones del proyecto y está respaldado por pruebas ejecutadas.

## Entradas
- Diff o rama a revisar.
- Especificación activa en `specs/` y constitución de Spec Kit.
- Catálogo de reglas validadas y contrato OpenAPI versionado.
- Evidencias legacy del alcance.

## Lista de comprobación
1. **Alcance**: los cambios se limitan a lo aprobado; sin cambios no autorizados en contratos, datos o dependencias.
2. **Equivalencia funcional**: cada regla del alcance está implementada en backend y trazada a su evidencia legacy.
3. **Reglas críticas**: ninguna regla crítica reside exclusivamente en el frontend.
4. **Contrato**: la implementación coincide con el OpenAPI versionado; cambios de contrato justificados y versionados.
5. **Persistencia**: uso de iBATIS coherente con la estrategia del piloto (sin sustitución masiva); SQL equivalente al legacy cuando aplique.
6. **Pruebas**: existen pruebas unitarias, de integración y/o de contrato para el comportamiento migrado, y se han ejecutado; registrar comandos y resultados reales.
7. **Seguridad y configuración**: sin credenciales productivas, secretos ni accesos a producción.
8. **Capacidades de interfaz**: la pantalla conserva lo que permitía el legacy (vistas del calendario, mover guardias, selección de fechas), aunque use widgets y aspecto modernos; toda capacidad eliminada está aprobada en la especificación.
9. **Calidad**: tipado TypeScript estricto, manejo de errores, accesibilidad y consistencia con el código circundante.

## Salida
Informe con hallazgos ordenados por severidad (bloqueante, importante, menor), cada uno con `ruta:línea`, evidencia y recomendación, más:
- Pruebas ejecutadas y resultados.
- Desviaciones respecto a la especificación.
- Riesgos y decisiones pendientes.

## Restricciones
- No declarar pruebas superadas si no se han ejecutado.
- No aprobar cambios sin trazabilidad a especificación y reglas validadas.
