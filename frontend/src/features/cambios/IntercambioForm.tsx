import { useEffect, useState, type FormEvent } from 'react';
import { api, type Cambio, type Candidato, type Guardia } from '../../api/client';
import { etiquetaTipo, fechaLarga } from './fechas';
import { Rechazo } from './Rechazo';
import { useRegistrarCambio } from './useRegistrarCambio';

interface Props {
  servicioId: number;
  guardia: Guardia;
  onRegistrado: (cambio: Cambio) => void;
  onCancelar: () => void;
}

const FECHA = /^\d{4}-\d{2}-\d{2}$/;

/**
 * Intercambio de la guardia elegida por la de otro médico en otra fecha (historia 2). Los
 * candidatos son los médicos con guardia ese día que cumplen las reglas; el servidor vuelve a
 * validarlo todo al registrar (FR-016, FR-017).
 */
export function IntercambioForm({ servicioId, guardia, onRegistrado, onCancelar }: Props) {
  const [fechaDestino, setFechaDestino] = useState('');
  const [candidatos, setCandidatos] = useState<Candidato[]>();
  const [elegido, setElegido] = useState<Candidato>();
  const [errorCandidatos, setErrorCandidatos] = useState<string>();
  const { registrar, enviando, infracciones, error } = useRegistrarCambio(
    servicioId,
    onRegistrado,
    'No se ha podido registrar el intercambio.',
  );

  useEffect(() => {
    setCandidatos(undefined);
    setElegido(undefined);
    setErrorCandidatos(undefined);
    if (!FECHA.test(fechaDestino)) return;
    let vigente = true;
    void api
      .GET('/servicios/{servicioId}/guardias/{guardiaId}/receptores-candidatos', {
        params: {
          path: { servicioId, guardiaId: guardia.id },
          query: { tipo: 'CAMBIO', fechaDestino },
        },
      })
      .then(({ data, error: problema }) => {
        if (!vigente) return;
        if (data) setCandidatos(data);
        else setErrorCandidatos(problema?.detail ?? 'No se han podido cargar los candidatos.');
      });
    return () => {
      vigente = false;
    };
  }, [servicioId, guardia.id, fechaDestino]);

  function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (elegido?.guardiaDestinoId === undefined) return;
    void registrar({
      tipo: 'CAMBIO',
      guardiaOrigenId: guardia.id,
      cedenteId: guardia.medico.id,
      receptorId: elegido.medico.id,
      guardiaDestinoId: elegido.guardiaDestinoId,
    });
  }

  return (
    <form aria-label="Intercambiar guardia" onSubmit={enviar}>
      <h3>Intercambiar guardia</h3>
      <p>
        {guardia.medico.nombre} entrega su guardia del {fechaLarga(guardia.fecha)} (
        {etiquetaTipo(guardia.tipo, guardia.festivo)}) y recibe a cambio la de otro médico.
      </p>

      <label>
        Fecha de la guardia que recibe
        <input type="date" value={fechaDestino} onChange={(e) => setFechaDestino(e.target.value)} />
      </label>

      {FECHA.test(fechaDestino) && !candidatos && !errorCandidatos && <p>Cargando candidatos…</p>}
      {candidatos?.length === 0 && (
        <p>Ningún médico con guardia ese día cumple las reglas para el intercambio.</p>
      )}
      {candidatos && candidatos.length > 0 && (
        <fieldset>
          <legend>Médico con el que intercambia</legend>
          {candidatos.map((c) => (
            <label key={c.guardiaDestinoId ?? c.medico.id}>
              <input
                type="radio"
                name="receptor"
                value={c.medico.id}
                checked={elegido?.guardiaDestinoId === c.guardiaDestinoId}
                onChange={() => setElegido(c)}
              />
              {c.medico.nombre}
            </label>
          ))}
        </fieldset>
      )}

      <Rechazo que="el intercambio" infracciones={infracciones} error={error ?? errorCandidatos} />

      <button type="submit" disabled={enviando || elegido === undefined}>
        Registrar intercambio
      </button>
      <button type="button" onClick={onCancelar} disabled={enviando}>
        Cancelar
      </button>
    </form>
  );
}
