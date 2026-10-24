import { useEffect, useState, type FormEvent } from 'react';
import { api, type Cambio, type Candidato, type Guardia } from '../../api/client';
import { etiquetaTipo, fechaLarga } from './fechas';
import { Rechazo } from './Rechazo';
import { useRegistrarCambio } from './useRegistrarCambio';

interface Props {
  servicioId: number;
  guardia: Guardia;
  onRegistrada: (cambio: Cambio) => void;
  onCancelar: () => void;
}

/**
 * Cesión de una guardia a otro médico. Los candidatos son una ayuda: el servidor valida todas las
 * reglas al registrar y devuelve todas las que fallan (FR-016, FR-017).
 */
export function CesionForm({ servicioId, guardia, onRegistrada, onCancelar }: Props) {
  const [candidatos, setCandidatos] = useState<Candidato[]>();
  const [receptorId, setReceptorId] = useState<number>();
  const [errorCandidatos, setErrorCandidatos] = useState<string>();
  const { registrar, enviando, infracciones, error } = useRegistrarCambio(
    servicioId,
    onRegistrada,
    'No se ha podido registrar la cesión.',
  );

  useEffect(() => {
    let vigente = true;
    void api
      .GET('/servicios/{servicioId}/guardias/{guardiaId}/receptores-candidatos', {
        params: { path: { servicioId, guardiaId: guardia.id }, query: { tipo: 'CESION' } },
      })
      .then(({ data, error: problema }) => {
        if (!vigente) return;
        if (data) setCandidatos(data);
        else setErrorCandidatos(problema?.detail ?? 'No se han podido cargar los candidatos.');
      });
    return () => {
      vigente = false;
    };
  }, [servicioId, guardia.id]);

  function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (receptorId === undefined) return;
    void registrar({
      tipo: 'CESION',
      guardiaOrigenId: guardia.id,
      cedenteId: guardia.medico.id,
      receptorId,
    });
  }

  return (
    <form aria-label="Ceder guardia" onSubmit={enviar}>
      <h3>Ceder guardia</h3>
      <p>
        {guardia.medico.nombre} cede su guardia del {fechaLarga(guardia.fecha)} (
        {etiquetaTipo(guardia.tipo, guardia.festivo)}).
      </p>

      {!candidatos && !errorCandidatos && <p>Cargando candidatos…</p>}
      {candidatos?.length === 0 && (
        <p>Ningún médico cumple las reglas para recibir esta guardia.</p>
      )}
      {candidatos && candidatos.length > 0 && (
        <fieldset>
          <legend>Médico que la recibe</legend>
          {candidatos.map(({ medico }) => (
            <label key={medico.id}>
              <input
                type="radio"
                name="receptor"
                value={medico.id}
                checked={receptorId === medico.id}
                onChange={() => setReceptorId(medico.id)}
              />
              {medico.nombre}
            </label>
          ))}
        </fieldset>
      )}

      <Rechazo que="la cesión" infracciones={infracciones} error={error ?? errorCandidatos} />

      <button type="submit" disabled={enviando || receptorId === undefined}>
        Registrar cesión
      </button>
      <button type="button" onClick={onCancelar} disabled={enviando}>
        Cancelar
      </button>
    </form>
  );
}
