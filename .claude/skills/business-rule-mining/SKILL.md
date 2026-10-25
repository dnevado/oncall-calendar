---
name: business-rule-mining
description: Extrae candidatos a reglas de negocio desde el código y datos legacy, con evidencias, para su validación por negocio e inclusión en el catálogo versionado. Usar tras el descubrimiento legacy y antes de especificar comportamiento.
---

# Minería de reglas de negocio

## Objetivo
Identificar reglas implícitas en el código legacy y documentarlas como **candidatas** pendientes de validación por negocio.

## Entradas
- Informe de `legacy-discovery` del alcance.
- Código Java, JavaScript, JSP y sqlmaps de iBATIS.
- Catálogo de reglas existente, si lo hay.

## Procedimiento
1. Busca condiciones, validaciones, cálculos, restricciones de estado y filtros SQL que expresen decisiones del dominio.
2. Para cada candidata registra:
   - Identificador provisional (p. ej. `BR-CAND-001`).
   - Enunciado en lenguaje de negocio.
   - Evidencias (`ruta:línea`, consulta SQL, datos de prueba).
   - Capa donde se aplica (frontend, backend, SQL) y si se duplica.
   - Criticidad estimada y nivel de confianza.
   - Preguntas para negocio.
3. Contrasta con el catálogo: marca reglas nuevas, coincidentes o contradictorias.
4. Señala reglas aplicadas solo en el frontend como riesgo.

## Salida
Tabla de reglas candidatas con estado `pendiente de validación`, lista para revisión por negocio.

## Restricciones
- No inventar reglas: sin evidencia no hay regla.
- Una regla candidata no es fuente de verdad hasta que negocio la valide e incorpore al catálogo versionado.
- No trasladar reglas críticas exclusivamente al frontend.
