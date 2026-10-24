CREATE TRIGGER trg_medicos_tipo_update AFTER UPDATE ON medicos
FOR EACH ROW
BEGIN
  -- Cierra el registro vigente y abre otro cuando cambia el tipo o el subtipo.
  -- También crea el registro si el médico aún no tenía histórico (p. ej. ID asignado tarde).
  IF NEW.ID IS NOT NULL AND (
       NOT (OLD.Tipo <=> NEW.Tipo)
       OR NOT (OLD.SubTipo <=> NEW.SubTipo)
       OR NOT EXISTS (SELECT 1 FROM medicos_tipo_historico h WHERE h.IdMedico = NEW.ID AND h.Hasta IS NULL)
     ) THEN
    UPDATE medicos_tipo_historico
       SET Hasta = CURDATE()
     WHERE IdMedico = NEW.ID AND Hasta IS NULL;
    INSERT INTO medicos_tipo_historico (IdMedico, Tipo, SubTipo, Desde, Hasta)
    VALUES (NEW.ID, NEW.Tipo, NEW.SubTipo, CURDATE(), NULL);
  END IF;
END
