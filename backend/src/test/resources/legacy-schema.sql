-- Esquema legacy (solo DDL, sin datos) extraído del dump MySQL 5.7.33 de 2026-08-26.
-- Uso exclusivo en pruebas (Testcontainers). Sin filas ni datos personales (principio VI).
-- Se ha eliminado el valor AUTO_INCREMENT=N del dump.

CREATE TABLE `configuracion` (
  `config_key` varchar(50) NOT NULL,
  `config_value` varchar(1000) NOT NULL,
  `config_IdServicio` int(11) NOT NULL,
  UNIQUE KEY `IND_CONFIGURACION_01` (`config_key`,`config_IdServicio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `guardias_cambios` (
  `IdCambio` int(11) NOT NULL,
  `FechaIniCambio` varchar(50) NOT NULL,
  `FechaFinCambio` varchar(50) DEFAULT NULL,
  `IdMedicoSolicitante` int(11) NOT NULL,
  `FechaCreacion` varchar(50) NOT NULL,
  `FechaAprobacion` varchar(50) DEFAULT NULL,
  `UsuarioAprobacion` int(11) DEFAULT NULL,
  `Observaciones` varchar(500) DEFAULT NULL,
  `Estado` varchar(1000) DEFAULT NULL,
  `TipoCambio` varchar(1000) DEFAULT NULL,
  `IdMedicoDestino` int(11) DEFAULT NULL,
  `IdServicio` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `guardias_medicos` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `IdMedico` int(11) NOT NULL,
  `FGuardia` varchar(1000) NOT NULL,
  `Festivo` int(11) DEFAULT NULL,
  `Tipo` varchar(1000) DEFAULT NULL,
  `IdEventoGCalendar` varchar(1000) DEFAULT NULL,
  `IdServicio` int(11) DEFAULT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `guardias_servicios` (
  `IdServicio` int(11) NOT NULL,
  `Nombre` varchar(150) NOT NULL,
  `Descripcion` varchar(500) NOT NULL,
  `Activo` int(11) NOT NULL DEFAULT '1',
  `Visible` int(11) NOT NULL DEFAULT '1',
  `FechaCreacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `FechaModiificacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `IdMedicoOwner` int(11) NOT NULL,
  `CodigoInterno` varchar(50) NOT NULL,
  PRIMARY KEY (`IdServicio`),
  KEY `IdServicio` (`IdServicio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `guardias_servicios_medicos` (
  `IdServicio` int(11) NOT NULL,
  `IdMedico` int(11) NOT NULL,
  `IsAdministrator` int(11) NOT NULL DEFAULT '0',
  `ActivoServicio` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`IdServicio`,`IdMedico`),
  KEY `IdServicio_IdMedico` (`IdServicio`,`IdMedico`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `medicos` (
  `Nombre` varchar(1000) NOT NULL,
  `Apellidos` varchar(1000) NOT NULL,
  `IdMedico` int(11) NOT NULL,
  `MaxGuardias` int(11) NOT NULL,
  `GuardiasSolo` int(11) NOT NULL DEFAULT '0',
  `Orden` int(11) NOT NULL,
  `Tipo` varchar(1000) NOT NULL,
  `SubTipo` varchar(1000) DEFAULT NULL,
  `Activo` int(11) NOT NULL,
  `ID` int(11) DEFAULT NULL,
  `Email` varchar(1000) DEFAULT NULL,
  `Password` varchar(1000) DEFAULT NULL,
  `Administrator` int(11) DEFAULT NULL,
  `Confirmado` int(11) DEFAULT '0',
  `FechaCreacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `Origen` varchar(50) NOT NULL DEFAULT 'normal',
  UNIQUE KEY `IND_MEDICOS_01` (`ID`),
  UNIQUE KEY `Email` (`Email`),
  KEY `IND_MEDICOS_02` (`IdMedico`,`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `vacaciones_medicos` (
  `IdMedico` int(11) NOT NULL,
  `FDay` varchar(10) NOT NULL,
  UNIQUE KEY `IND_VACACIONES_01` (`IdMedico`,`FDay`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

