import { useEffect, useState, type FormEvent } from 'react';
import { api, type Cambio, type Candidato, type Guardia, type Infraccion } from '../../api/client';
import { etiquetaTipo, fechaLarga } from './fechas';

interface Props {
  servicioId: number;
  guardia: Guardia;
  onRegistrada: (cambio: Cambio) => void;
  onCancelar: () => void;
}

function nuevaClave(): string {
  return crypto.randomUUID();
}

/**
 * Cesión de una guardia a otro médico. Los candidatos son una ayuda: el servidor valida todas las
 * reglas al registrar y devuelve todas las que fallan (FR-016, FR-017).
 */
export function CesionForm({ servicioId, guardia, onRegistrada, onCancelar }: Props) {
  const [candidatos, setCandidatos] = useState<Candidato[]>();
  const [receptorId, setReceptorId] = useState<number>();
  const [enviando, setEnviando] = useState(false);
  const [infracciones, setInfracciones] = useState<Infraccion[]>([]);
  const [error, setError] = useState<string>();
  // Una clave por envío: un reintento del mismo envío no aplica la cesión dos veces.
  const [clave, setClave] = useState(nuevaClave);

  useEffect(() => {
    let vigente = true;
    void api
      .GET('/servicios/{servicioId}/guardias/{guardiaId}/receptores-candidatos', {
        params: { path: { servicioId, guardiaId: guardia.id }, query: { tipo: 'CESION' } },
      })
      .then(({ data, error: problema }) => {
        if (!vigente) return;
        if (data) setCandidatos(data);
        else setError(problema?.detail ?? 'No se han podido cargar los candidatos.');
      });
    return () => {
      vigente = false;
    };
  }, [servicioId, guardia.id]);

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (receptorId === undefined) return;
    setEnviando(true);
    setInfracciones([]);
    setError(undefined);
    const { data, error: problema, response } = await api.POST('/servicios/{servicioId}/cambios', {
      params: { path: { servicioId }, header: { 'Idempotency-Key': clave } },
      body: { tipo: 'CESION', guardiaOrigenId: guardia.id, cedenteId: guardia.medico.id, receptorId },
    });
    setEnviando(false);
    if (data) {
      onRegistrada(data);
      return;
    }
    setClave(nuevaClave());
    if (response.status === 422 && problema && 'infracciones' in problema) {
      setInfracciones(problema.infracciones);
    } else {
      setError(problema?.detail ?? 'No se ha podido registrar la cesión.');
    }
  }

  return (
    <form aria-label="Ceder guardia" onSubmit={enviar}>
      <h3>Ceder guardia</h3>
      <p>
        {guardia.medico.nombre} cede su guardia del {fechaLarga(guardia.fecha)} ({etiquetaTipo(guardia.tipo, guardia.festivo)}).
      </p>

      {!candidatos && !error && <p>Cargando candidatos…</p>}
      {candidatos?.length === 0 && <p>Ningún médico cumple las reglas para recibir esta guardia.</p>}
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

      {infracciones.length > 0 && (
        <div role="alert">
          <p>No se ha registrado la cesión:</p>
          <ul>
            {infracciones.map((i, indice) => (
              <li key={`${i.regla}-${indice}`}>{i.mensaje}</li>
            ))}
          </ul>
        </div>
      )}
      {error && <p role="alert">{error}</p>}

      <button type="submit" disabled={enviando || receptorId === undefined}>
        Registrar cesión
      </button>
      <button type="button" onClick={onCancelar} disabled={enviando}>
        Cancelar
      </button>
    </form>
  );
}
