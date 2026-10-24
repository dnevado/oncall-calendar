import type { Infraccion } from '../../api/client';

interface Props {
  /** Qué no se ha registrado, p. ej. «la cesión». */
  que: string;
  infracciones: Infraccion[];
  error?: string;
}

/** Reglas incumplidas o error del servidor tras intentar registrar un cambio. */
export function Rechazo({ que, infracciones, error }: Props) {
  return (
    <>
      {infracciones.length > 0 && (
        <div role="alert">
          <p>No se ha registrado {que}:</p>
          <ul>
            {infracciones.map((i, indice) => (
              <li key={`${i.regla}-${indice}`}>{i.mensaje}</li>
            ))}
          </ul>
        </div>
      )}
      {error && <p role="alert">{error}</p>}
    </>
  );
}
