import { useEffect, useState } from 'react';
import type { DiaVacaciones, Schemas, VacacionesMedico } from '../../api/client';
import { mesDe, nombreMes, TIPOS } from '../cambios/fechas';
import { useVacaciones } from './useVacaciones';
import { aDate, aTexto, dias, fechaConAnio } from './dias';

interface Props {
  servicioId: number;
  hoy: string;
  /** Cambia cuando se guardan vacaciones, para volver a pedir el resumen. */
  version: number;
  onElegir: (medico: Schemas['MedicoResumen']) => void;
}

/** Seis meses, como la columna «Vacaciones» del listado legacy (FR-013). */
const MESES_PERIODO = 6;

function desplazarMeses(fecha: string, meses: number): string {
  const d = aDate(fecha);
  return aTexto(new Date(d.getFullYear(), d.getMonth() + meses, d.getDate(), 12));
}

const LISTA = new Intl.ListFormat('es', { style: 'long', type: 'conjunction' });
const CORTA = new Intl.DateTimeFormat('es-ES', {
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
});

/** «febrero de 2030: 4 y 5», un elemento por mes. */
function porMes(diasMedico: DiaVacaciones[]): string[] {
  const grupos = new Map<string, string[]>();
  for (const d of diasMedico) {
    const clave = d.fecha.slice(0, 7);
    grupos.set(clave, [...(grupos.get(clave) ?? []), String(Number(d.fecha.slice(8)))]);
  }
  return [...grupos].map(
    ([clave, numeros]) => `${nombreMes(mesDe(clave))}: ${LISTA.format(numeros)}`,
  );
}

function Fila({
  vacaciones,
  onElegir,
}: {
  vacaciones: VacacionesMedico;
  onElegir: Props['onElegir'];
}) {
  const { medico, activo, dias: diasMedico } = vacaciones;
  const conflictos = diasMedico.filter((d) => d.conflicto);
  return (
    <tr>
      <th scope="row">
        {medico.nombre}
        {!activo && <span className="etiqueta-inactivo"> Inactivo en el servicio</span>}
      </th>
      <td>
        {diasMedico.length === 0 ? (
          'Sin vacaciones en el periodo'
        ) : (
          <>
            <span className="total-dias">{dias(diasMedico.length)}</span>
            <ul className="meses">
              {porMes(diasMedico).map((texto) => (
                <li key={texto}>{texto}</li>
              ))}
            </ul>
          </>
        )}
      </td>
      <td>
        {conflictos.length > 0 && (
          <ul className="conflictos">
            {conflictos.map(({ fecha, conflicto }) => (
              <li key={fecha}>
                <span aria-hidden="true">⚠ </span>
                Conflicto: {CORTA.format(aDate(fecha))}, guardia de{' '}
                {conflicto ? TIPOS[conflicto.tipo].toLowerCase() : ''}
              </li>
            ))}
          </ul>
        )}
      </td>
      <td>
        <button type="button" onClick={() => onElegir(medico)}>
          Editar<span className="solo-lector"> vacaciones de {medico.nombre}</span>
        </button>
      </td>
    </tr>
  );
}

/**
 * Vacaciones de todos los médicos del servicio en un periodo de seis meses, navegable, con los días
 * en conflicto con una guardia señalados (002 US3, FR-012 a FR-014). Los conflictos se muestran,
 * no se corrigen (P7).
 */
export function ResumenVacaciones({ servicioId, hoy, version, onElegir }: Props) {
  const [desde, setDesde] = useState(hoy);
  const hasta = desplazarMeses(desde, MESES_PERIODO);
  const { datos, error, recargar } = useVacaciones(servicioId, desde, hasta);

  useEffect(() => {
    if (version > 0) void recargar();
  }, [version, recargar]);

  const titulo = `Vacaciones del servicio del ${fechaConAnio(aDate(desde))} al ${fechaConAnio(aDate(hasta))}`;

  return (
    <section className="resumen-vacaciones" aria-labelledby="resumen-vacaciones-titulo">
      <div className="navegacion-periodo">
        <button type="button" onClick={() => setDesde(desplazarMeses(desde, -MESES_PERIODO))}>
          6 meses anteriores
        </button>
        <button type="button" disabled={desde === hoy} onClick={() => setDesde(hoy)}>
          Volver a hoy
        </button>
        <button type="button" onClick={() => setDesde(desplazarMeses(desde, MESES_PERIODO))}>
          6 meses siguientes
        </button>
      </div>
      {error && <p role="alert">{error}</p>}
      {!datos && !error && <p>Cargando…</p>}
      {datos && (
        <div className="tabla-desplazable">
          <table>
            <caption id="resumen-vacaciones-titulo">{titulo}</caption>
            <thead>
              <tr>
                <th scope="col">Médico</th>
                <th scope="col">Días de vacaciones</th>
                <th scope="col">Conflictos con guardias</th>
                <th scope="col">
                  <span className="solo-lector">Acciones</span>
                </th>
              </tr>
            </thead>
            <tbody>
              {datos.map((v) => (
                <Fila key={v.medico.id} vacaciones={v} onElegir={onElegir} />
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
