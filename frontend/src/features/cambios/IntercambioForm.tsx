import { useEffect, useState, type FormEvent } from 'react';
import { api, type Cambio, type Candidato, type Guardia, type Schemas } from '../../api/client';
import { etiquetaTipo, fechaLarga } from './fechas';
import { Rechazo } from './Rechazo';
import { useRegistrarCambio } from './useRegistrarCambio';

interface Props {
  servicioId: number;
  guardia: Guardia;
  onRegistrado: (cambio: Cambio) => void;
  onCancelar: () => void;
  /** Fecha y médico ya elegidos, p. ej. al registrar el cambio inverso (FR-028). */
  fechaDestinoInicial?: string;
  receptorInicial?: Schemas['MedicoResumen'];
}

const FECHA = /^\d{4}-\d{2}-\d{2}$/;

/**
 * Intercambio de la guardia elegida por la de otro médico en otra fecha (historia 2). Los
 * candidatos son los médicos con guardia ese día que cumplen las reglas; el servidor vuelve a
 * validarlo todo al registrar (FR-016, FR-017).
 */
export function IntercambioForm({
  servicioId,
  guardia,
  onRegistrado,
  onCancelar,
  fechaDestinoInicial = '',
  receptorInicial,
}: Props) {
  const [fechaDestino, setFechaDestino] = useState(fechaDestinoInicial);
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
        if (data) {
          setCandidatos(data);
          setElegido(data.find((c) => c.medico.id === receptorInicial?.id));
        } else {
          setErrorCandidatos(problema?.detail ?? 'No se han podido cargar los candidatos.');
        }
      });
    return () => {
      vigente = false;
    };
  }, [servicioId, guardia.id, fechaDestino, receptorInicial?.id]);

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
      <p>{guardia.medico.nombre} entrega su guardia y recibe a cambio la de otro médico.</p>
      <p>
        <strong>Inicio:</strong> {fechaLarga(guardia.fecha)} (
        {etiquetaTipo(guardia.tipo, guardia.festivo)})
      </p>

      <label>
        <strong>Fin:</strong> fecha de la guardia que recibe
        <input type="date" value={fechaDestino} onChange={(e) => setFechaDestino(e.target.value)} />
      </label>

      {FECHA.test(fechaDestino) && !candidatos && !errorCandidatos && <p>Cargando candidatos…</p>}
      {candidatos?.length === 0 && (
        <p>Ningún médico con guardia ese día cumple las reglas para el intercambio.</p>
      )}
      {receptorInicial &&
        candidatos &&
        !candidatos.some((c) => c.medico.id === receptorInicial.id) && (
          <p>{receptorInicial.nombre} no cumple ahora las reglas para este intercambio.</p>
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
