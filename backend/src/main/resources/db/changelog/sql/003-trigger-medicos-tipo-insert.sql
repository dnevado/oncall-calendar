CREATE TRIGGER trg_medicos_tipo_insert AFTER INSERT ON medicos
FOR EACH ROW
BEGIN
  -- Médicos dados de alta (también desde el legacy): su tipo vale «desde siempre».
  IF NEW.ID IS NOT NULL THEN
    INSERT INTO medicos_tipo_historico (IdMedico, Tipo, SubTipo, Desde, Hasta)
    VALUES (NEW.ID, NEW.Tipo, NEW.SubTipo, NULL, NULL);
  END IF;
END
