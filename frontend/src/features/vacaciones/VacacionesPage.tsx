import { useState } from 'react';
import type { Schemas } from '../../api/client';
import { hoyEnServicio } from '../cambios/fechas';
import { EditorVacaciones } from './EditorVacaciones';
import { ResumenVacaciones } from './ResumenVacaciones';
import './vacaciones.css';

interface Props {
  servicioId: number;
  /** Solo para pruebas; por defecto, hoy en la zona del servicio. */
  hoy?: string;
}

/**
 * Vacaciones de los médicos del servicio (002): resumen del servicio y editor del médico elegido.
 * Sustituye al selector de vacaciones de la ficha legacy (`web/medico/detallemedico.jsp`) y a la
 * columna «Vacaciones» del listado (`web/medico/lista_medicos.jsp`); el resto sigue en el legacy.
 */
export default function VacacionesPage({ servicioId, hoy = hoyEnServicio() }: Props) {
  const [medico, setMedico] = useState<Schemas['MedicoResumen']>();
  const [version, setVersion] = useState(0);

  return (
    <main className="vacaciones">
      <h1>Vacaciones</h1>
      <div className="vacaciones-paneles">
        <ResumenVacaciones
          servicioId={servicioId}
          hoy={hoy}
          version={version}
          onElegir={setMedico}
        />
        {medico && (
          <EditorVacaciones
            key={medico.id}
            servicioId={servicioId}
            medico={medico}
            hoy={hoy}
            onGuardado={() => setVersion((v) => v + 1)}
          />
        )}
      </div>
    </main>
  );
}

export { VacacionesPage };
