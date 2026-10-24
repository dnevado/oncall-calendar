# Cuestionario para negocio — Cambios y cesiones de guardia

- **Fecha:** 2026-10-24
- **Origen:** [descubrimiento legacy](cambio-guardia-discovery.md)
- **Destinatarios sugeridos:** jefe de servicio, administrador de guardias, tutor de residentes.
- **Instrucciones:** marque una opción por pregunta y añada comentarios si hace falta. «Hoy» describe lo que hace la aplicación actual, no necesariamente lo correcto. «Datos» resume lo observado en los cambios registrados entre 2017 y agosto de 2026 (5.484 cambios). Parte de esos datos se insertó a mano por SQL sin registro de cuáles, así que los recuentos son orientativos y no prueban qué permitía la aplicación.

| Respondido por | Rol | Fecha |
|---|---|---|
| David | Administrador | 2026-10-24 |

---

## P1. Cesión a alguien que ya tiene guardia ese día
La Dra. A cede su guardia del día 10 al Dr. B, que ese día ya tiene otra guardia (por ejemplo, localizada).
*Hoy:* se permite.
*Datos:* ha ocurrido 6 veces en 3.582 cesiones.

- [x] Prohibir siempre
- [ ] Permitir si las guardias son de distinto tipo
- [ ] Permitir siempre
- [ ] Otra: ___

## P2. Requisitos para el receptor de una cesión
La Dra. A cede su guardia del día 10. Para elegir al receptor, arrastra la guardia a otro día del calendario (por ejemplo, el 15), y el sistema solo deja elegir entre los médicos que tienen guardia ese día 15. Al aplicarse la cesión, el receptor se queda con la guardia del día 10 y conserva la suya del día 15: ese segundo día no cambia.
*Datos:* todas las cesiones registradas (3.582) se hicieron así, entre dos días distintos.

¿Debe ser así?
- [ ] Sí, es intencionado. Motivo: ___
- [ ] No, el receptor puede ser cualquier médico disponible ese día 10
- [x] Otra: Si, porque la CESION y CAMBIO aplican en el mismo formulario, aunque los procesos y validaciones son distintas

> *Nota de fusión:* esta respuesta se dio sobre la redacción anterior de P2 y se conserva literal.
>
> *Resolución (2026-10-24):* la exigencia se debe a que CAMBIO y CESION comparten formulario, y el reparto la usa como atajo defectuoso para saber el tipo de guardia cedida (ver P10 y el informe, §6). Propuesta para el sistema nuevo: la cesión solo requiere fecha origen y receptor, y guarda el tipo y el festivo de la guardia cedida. Pendiente de confirmar en la especificación.

## P3. Plazos
*Hoy:* la guardia que se entrega tiene que ser como pronto de mañana; la que se recibe puede ser de una fecha pasada.
*Datos:* 4 cambios con la guardia entregada ya pasada, 1 del mismo día y 18 con la guardia recibida pasada.

a) ¿Se puede ceder o cambiar una guardia del mismo día?
- [ ] Sí  - [x] No  - [ ] Sí, antes de las __:__

b) ¿Se puede recibir una guardia de una fecha pasada?
- [ ] Sí  - [x] No

c) ¿Hace falta una antelación mínima?
- [x] No  - [ ] Sí: __ días

## P4. Presencias consecutivas
Tras el cambio, nadie debería tener presencia el día anterior o el posterior a una presencia.
*Datos:* en todo el calendario solo hay 3 casos de presencias en días consecutivos.

a) ¿Es correcta esta regla?
- [x] Sí  - [ ] No  - [ ] Otra: ___

b) ¿A quién no se aplica cuando es residente simulado?
- [ ] Al que entrega la guardia  - [ ] Al que la recibe  - [ ] A ambos  - [x] A ninguno

c) ¿Se aplica también a localizadas o refuerzos?
- [x] No  - [ ] Sí: ___

## P5. Tipo de médico
La pantalla solo deja elegir a médicos activos del mismo tipo (adjunto con adjunto, residente con residente).
*Datos:* 627 cambios (11 %) aparecen entre residente y adjunto, todos entre 2017 y 2023. Probablemente se trata de residentes que después pasaron a adjuntos, porque el sistema solo guarda el tipo actual de cada médico.

- [x] Es una regla obligatoria
- [ ] Es solo una sugerencia de la pantalla
- [ ] Depende del tipo de guardia: ___

## P6. Quién aprueba
*Hoy:* el cambio que solicita un médico solo lo puede aprobar un administrador; el que crea un administrador se aprueba solo. Existe el ajuste «Validar cambios por administrador», pero no tiene ningún efecto.
*Datos:* todos los cambios están aprobados (ninguno pendiente, cancelado ni rechazado). El 99,8 % se aprobó el mismo día en que se registró, siempre por uno de los dos administradores. En la práctica, los administradores registran los cambios ya acordados en nombre de los médicos.

d) ¿Deben los médicos poder solicitar cambios ellos mismos, o basta con que el administrador los registre?
- [ ] Solicitud por el médico + aprobación  - [x] Solo registro por el administrador  - [ ] Ambos

a) ¿Debe aprobar el administrador?
- [x] Siempre  - [ ] Según la configuración del servicio  - [ ] Nunca

b) ¿Debe aceptar también el médico que recibe la guardia?
- [ ] Sí  - [x] No

c) ¿Puede el solicitante cancelar su propia solicitud pendiente?
- [ ] Sí  - [x] No

## P7. Revalidación al aprobar
Entre la solicitud y la aprobación pueden cambiar las vacaciones o las guardias.
*Hoy:* las vacaciones y las presencias no se vuelven a comprobar. Si el médico que recibe la guardia ya no tiene guardia en el día elegido (porque otro cambio la movió antes), el cambio queda marcado como aprobado y se avisa por correo, pero el calendario no cambia.
*Datos:* hay 8 cambios marcados como aprobados sin que conste quién los aprobó; muy probablemente son inserciones manuales.

- [ ] Revalidar y rechazar si ya no cumple
- [ ] Revalidar y avisar al administrador
- [x] No revalidar

> *Nota de fusión:* esta respuesta se dio sobre la redacción anterior de P7, que no mencionaba el caso «aprobado sin aplicar».

## P8. Ajuste automático con residentes R3/R4/R5
Cuando un cambio entre residentes hace que un día pase a tener o deje de tener un R3/R4/R5, la guardia del adjunto ese día cambia de refuerzo a localizada o al revés.

a) ¿Es correcta esta regla?
- [x] Sí  - [ ] No

b) ¿Incluye a los R3?
- [x] Sí  - [ ] No

## P9. Vacaciones en varios servicios
Un médico que trabaja en dos servicios y tiene vacaciones en uno, ¿está de vacaciones también en el otro?
*Hoy:* sí.
*Datos:* hoy solo hay un médico en más de un servicio, y no tiene vacaciones registradas.

- [x] Sí, son globales
- [ ] No, son por servicio

## P10. Cesiones en el equilibrio del reparto
Al calcular la agenda, el sistema equilibra el número de guardias de cada tipo entre médicos. Cuando la Dra. A cede al Dr. B su guardia del día 10, ¿para quién cuenta esa guardia en el equilibrio?
*Hoy:* la intención del código es que cuente para la Dra. A, pero el cálculo falla a menudo: toma el tipo de guardia equivocado en aproximadamente la mitad de las cesiones y casi nunca descuenta la cesión al Dr. B.

- [ ] Para quien la hace realmente (el receptor)
- [x] Para quien la tenía asignada (el cedente)
- [ ] No cuenta para el equilibrio
- [ ] Otra: ___

---

## Comentarios adicionales

&nbsp;
