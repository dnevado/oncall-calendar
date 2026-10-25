---
name: legacy-discovery
description: Inventaria y analiza la aplicación legacy (JSP, jQuery, Java, iBATIS, MySQL) para producir evidencias trazables antes de especificar o migrar. Usar al iniciar el análisis de un módulo, pantalla o flujo legacy.
---

# Descubrimiento del sistema legacy

## Objetivo
Obtener una visión verificable de cómo funciona hoy un módulo legacy, sin modificarlo, para alimentar la especificación de Spec Kit.

## Entradas
- Alcance a analizar (pantalla, flujo, caso de uso o módulo).
- Código fuente en `src/`, `web/` y configuración iBATIS.
- Inventarios existentes (`jsp_inventory.csv`, `jsp_inventory_enriched.csv`).

## Procedimiento
1. Localiza los puntos de entrada: JSP, acciones/servlets y llamadas AJAX de jQuery.
2. Traza el flujo extremo a extremo: JSP → JavaScript → controlador Java → servicio/DAO → mapa SQL de iBATIS → tablas MySQL.
3. Registra dependencias: clases, sqlmaps, propiedades (`base.properties`), librerías y recursos estáticos.
4. Identifica validaciones, cálculos y decisiones en cada capa, indicando si están en cliente, servidor o SQL.
5. Inventaría los widgets y la interacción de cada pantalla: componente y versión (FullCalendar, datepicker, etc.), opciones de configuración, vistas, eventos (clic, arrastrar y soltar), estilos y colores (`web/css/custom.css`), y la capacidad que aporta cada uno al usuario.
6. Señala código muerto, duplicado o acoplamientos riesgosos.
7. Anota dudas abiertas en lugar de suponer comportamiento.

## Salida
Un informe con:
- Mapa del flujo con referencias `ruta:línea`.
- Tabla de artefactos (tipo, ruta, responsabilidad, dependencias).
- Inventario de widgets e interacción de la interfaz.
- Lista de consultas SQL y tablas afectadas (lectura/escritura).
- Riesgos y preguntas abiertas.

## Restricciones
- Solo lectura: no modificar código legacy ni configuración.
- No usar credenciales ni datos productivos; solo datos de prueba autorizados.
- Toda afirmación debe apoyarse en una evidencia citada.
