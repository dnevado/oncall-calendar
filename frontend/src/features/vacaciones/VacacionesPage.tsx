import { useState } from 'react';
import { hoyEnServicio } from '../cambios/fechas';
import { EditorVacaciones } from './EditorVacaciones';
import { useVacaciones } from './useVacaciones';
import { aDate, aTexto } from './dias';
import './vacaciones.css';

interface Props {
  servicioId: number;
  /** Solo para pruebas; por defecto, hoy en la zona del servicio. */
  hoy?: string;
}

function seisMesesDespues(hoy: string): string {
  const fecha = aDate(hoy);
  return aTexto(new Date(fecha.getFullYear(), fecha.getMonth() + 6, fecha.getDate(), 12));
}

/**
 * Vacaciones de los médicos del servicio (002). Sustituye al selector de vacaciones de la ficha
 * legacy (`web/medico/detallemedico.jsp`); el resto de la ficha sigue en el legacy.
 */
export function VacacionesPage({ servicioId, hoy = hoyEnServicio() }: Props) {
  const { datos, error } = useVacaciones(servicioId, hoy, seisMesesDespues(hoy));
  const [medicoId, setMedicoId] = useState<number>();
  const medico = datos?.find((v) => v.medico.id === medicoId)?.medico;

  return (
    <main className="vacaciones">
      <h1>Vacaciones</h1>
      {error && <p role="alert">{error}</p>}
      {!datos && !error && <p>Cargando…</p>}
      {datos && (
        <div className="selector-medico">
          <label htmlFor="vacaciones-medico">Médico</label>
          <select
            id="vacaciones-medico"
            value={medicoId ?? ''}
            onChange={(e) => setMedicoId(e.target.value ? Number(e.target.value) : undefined)}
          >
            <option value="">Elige un médico…</option>
            {datos.map((v) => (
              <option key={v.medico.id} value={v.medico.id}>
                {v.activo ? v.medico.nombre : `${v.medico.nombre} (inactivo en el servicio)`}
              </option>
            ))}
          </select>
        </div>
      )}
      {medico && (
        <EditorVacaciones key={medico.id} servicioId={servicioId} medico={medico} hoy={hoy} />
      )}
    </main>
  );
}
