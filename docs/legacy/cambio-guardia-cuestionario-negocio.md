# Cuestionario para negocio — Cambios y cesiones de guardia

- **Fecha:** 2026-10-24
- **Origen:** [descubrimiento legacy](cambio-guardia-discovery.md)
- **Destinatarios sugeridos:** jefe de servicio, administrador de guardias, tutor de residentes.
- **Instrucciones:** marque una opción por pregunta y añada comentarios si hace falta. «Hoy» describe lo que hace la aplicación actual, no necesariamente lo correcto. «Datos» resume lo observado en los cambios registrados entre 2017 y agosto de 2026 (5.484 cambios).

| Respondido por | Rol | Fecha |
|---|---|---|
| | | |

---

## P1. Cesión a alguien que ya tiene guardia ese día
La Dra. A cede su guardia del día 10 al Dr. B, que ese día ya tiene otra guardia (por ejemplo, localizada).
*Hoy:* se permite.
*Datos:* ha ocurrido 6 veces en 3.582 cesiones.

- [ ] Prohibir siempre
- [ ] Permitir si las guardias son de distinto tipo
- [ ] Permitir siempre
- [ ] Otra: ___

## P2. Requisitos para el receptor de una cesión
Para aprobar una cesión, el sistema exige que el Dr. B tenga guardia en la fecha a la que se arrastró la guardia. Si no la tiene, la cesión queda aprobada pero no se aplica.

- [ ] El receptor no necesita tener ninguna guardia
- [ ] Debe tener guardia en otro día concreto (¿cuál?): ___
- [ ] Otra: ___

## P3. Plazos
*Hoy:* la guardia que se entrega tiene que ser como pronto de mañana; la que se recibe puede ser de una fecha pasada.
*Datos:* 4 cambios con la guardia entregada ya pasada, 1 del mismo día y 18 con la guardia recibida pasada.

a) ¿Se puede ceder o cambiar una guardia del mismo día?
- [ ] Sí  - [ ] No  - [ ] Sí, antes de las __:__

b) ¿Se puede recibir una guardia de una fecha pasada?
- [ ] Sí  - [ ] No

c) ¿Hace falta una antelación mínima?
- [ ] No  - [ ] Sí: __ días

## P4. Presencias consecutivas
Tras el cambio, nadie debería tener presencia el día anterior o el posterior a una presencia.
*Datos:* en todo el calendario solo hay 3 casos de presencias en días consecutivos.

a) ¿Es correcta esta regla?
- [ ] Sí  - [ ] No  - [ ] Otra: ___

b) ¿A quién no se aplica cuando es residente simulado?
- [ ] Al que entrega la guardia  - [ ] Al que la recibe  - [ ] A ambos  - [ ] A ninguno

c) ¿Se aplica también a localizadas o refuerzos?
- [ ] No  - [ ] Sí: ___

## P5. Tipo de médico
La pantalla solo deja elegir a médicos activos del mismo tipo (adjunto con adjunto, residente con residente).
*Datos:* 627 cambios (11 %) se registraron entre médicos de distinto tipo, normalmente por un administrador.

- [ ] Es una regla obligatoria
- [ ] Es solo una sugerencia de la pantalla
- [ ] Depende del tipo de guardia: ___

## P6. Quién aprueba
*Hoy:* el cambio que solicita un médico solo lo puede aprobar un administrador; el que crea un administrador se aprueba solo. Existe el ajuste «Validar cambios por administrador», pero no tiene ningún efecto.
*Datos:* todos los cambios están aprobados (ninguno pendiente, cancelado ni rechazado). El 99,8 % se aprobó el mismo día en que se registró, siempre por uno de los dos administradores. En la práctica, los administradores registran los cambios ya acordados en nombre de los médicos.

d) ¿Deben los médicos poder solicitar cambios ellos mismos, o basta con que el administrador los registre?
- [ ] Solicitud por el médico + aprobación  - [ ] Solo registro por el administrador  - [ ] Ambos

a) ¿Debe aprobar el administrador?
- [ ] Siempre  - [ ] Según la configuración del servicio  - [ ] Nunca

b) ¿Debe aceptar también el médico que recibe la guardia?
- [ ] Sí  - [ ] No

c) ¿Puede el solicitante cancelar su propia solicitud pendiente?
- [ ] Sí  - [ ] No

## P7. Revalidación al aprobar
Entre la solicitud y la aprobación pueden cambiar las vacaciones o las guardias.
*Hoy:* se aprueba sin volver a comprobar nada.

- [ ] Revalidar y rechazar si ya no cumple
- [ ] Revalidar y avisar al administrador
- [ ] No revalidar

## P8. Ajuste automático con residentes R3/R4/R5
Cuando un cambio entre residentes hace que un día pase a tener o deje de tener un R3/R4/R5, la guardia del adjunto ese día cambia de refuerzo a localizada o al revés.

a) ¿Es correcta esta regla?
- [ ] Sí  - [ ] No

b) ¿Incluye a los R3?
- [ ] Sí  - [ ] No

## P9. Vacaciones en varios servicios
Un médico que trabaja en dos servicios y tiene vacaciones en uno, ¿está de vacaciones también en el otro?
*Hoy:* sí.
*Datos:* hoy solo hay un médico en más de un servicio, y no tiene vacaciones registradas.

- [ ] Sí, son globales
- [ ] No, son por servicio

---

## Comentarios adicionales

&nbsp;
