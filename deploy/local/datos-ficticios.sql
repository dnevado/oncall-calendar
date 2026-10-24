-- Datos ficticios para el entorno local (deploy/local). Nada procede de producción.
-- Las fechas son relativas al día en que se crea el contenedor.
-- Todos los médicos entran con la contraseña «prueba» (hash legacy: SHA-512 en Base64).
-- El histórico de tipo (medicos_tipo_historico) lo carga Liquibase al arrancar el backend.

SET NAMES utf8;

INSERT INTO guardias_servicios (IdServicio, Nombre, Descripcion, IdMedicoOwner, CodigoInterno)
VALUES (1, 'Cardiología (ficticio)', 'Servicio de prueba local', 1, 'LOCAL');

SET @pwd = 'BDlDTa6RwQw7wHOvHnat349Xowzgp94EOLOqrTS4UgDUHQEHjy7nhrMTC07U454+JgkNpdn4dCBFTf3RgnYczg==';

INSERT INTO medicos (Nombre, Apellidos, IdMedico, MaxGuardias, Orden, Tipo, SubTipo, Activo, ID, Email, Password, Confirmado) VALUES
  ('Admin', 'Local',     1, 5, 1, 'ADJUNTO',   NULL, 1, 1, 'admin@prueba.local', @pwd, 1),
  ('Ana',   'Adjunta',   2, 5, 2, 'ADJUNTO',   NULL, 1, 2, 'ana@prueba.local',   @pwd, 1),
  ('Bruno', 'Adjunto',   3, 5, 3, 'ADJUNTO',   NULL, 1, 3, 'bruno@prueba.local', @pwd, 1),
  ('Carla', 'Adjunta',   4, 5, 4, 'ADJUNTO',   NULL, 1, 4, 'carla@prueba.local', @pwd, 1),
  ('David', 'Adjunto',   5, 5, 5, 'ADJUNTO',   NULL, 1, 5, 'david@prueba.local', @pwd, 1),
  ('Elena', 'Inactiva',  6, 5, 6, 'ADJUNTO',   NULL, 0, 6, 'elena@prueba.local', @pwd, 1),
  ('Rita',  'Residente', 7, 5, 7, 'RESIDENTE', 'R2', 1, 7, 'rita@prueba.local',  @pwd, 1),
  ('Raúl',  'Residente', 8, 5, 8, 'RESIDENTE', 'R4', 1, 8, 'raul@prueba.local',  @pwd, 1);

-- Admin es el administrador del servicio; Ana entra pero recibe 403 (no administra).
INSERT INTO guardias_servicios_medicos (IdServicio, IdMedico, IsAdministrator, ActivoServicio) VALUES
  (1, 1, 1, 1), (1, 2, 0, 1), (1, 3, 0, 1), (1, 4, 0, 1),
  (1, 5, 0, 1), (1, 6, 0, 1), (1, 7, 0, 1), (1, 8, 0, 1);

-- Día D+5: guardia de Ana para ceder. Bruno es candidato; Carla está de vacaciones (CG-012),
-- David tiene presencia el D+6 (CG-013), Elena está inactiva (CG-015) y Rita es residente (CG-014).
INSERT INTO guardias_medicos (IdMedico, FGuardia, Festivo, Tipo, IdServicio) VALUES
  (2, DATE_FORMAT(CURDATE() - INTERVAL 2 DAY, '%Y-%m-%d'),  0, 'presencia',  1),
  (2, DATE_FORMAT(CURDATE() + INTERVAL 5 DAY, '%Y-%m-%d'),  0, 'presencia',  1),
  (7, DATE_FORMAT(CURDATE() + INTERVAL 5 DAY, '%Y-%m-%d'),  0, '',           1),
  (5, DATE_FORMAT(CURDATE() + INTERVAL 6 DAY, '%Y-%m-%d'),  0, 'presencia',  1),
  (3, DATE_FORMAT(CURDATE() + INTERVAL 8 DAY, '%Y-%m-%d'),  0, 'localizada', 1),
  (2, DATE_FORMAT(CURDATE() + INTERVAL 10 DAY, '%Y-%m-%d'), 0, 'refuerzo',   1),
  (8, DATE_FORMAT(CURDATE() + INTERVAL 10 DAY, '%Y-%m-%d'), 0, '',           1),
  (1, DATE_FORMAT(CURDATE() + INTERVAL 12 DAY, '%Y-%m-%d'), 1, 'presencia',  1),
  (4, DATE_FORMAT(CURDATE() + INTERVAL 20 DAY, '%Y-%m-%d'), 1, 'presencia',  1),
  (3, DATE_FORMAT(CURDATE() + INTERVAL 35 DAY, '%Y-%m-%d'), 0, 'presencia',  1);

INSERT INTO vacaciones_medicos (IdMedico, FDay) VALUES
  (4, DATE_FORMAT(CURDATE() + INTERVAL 5 DAY, '%Y-%m-%d'));
