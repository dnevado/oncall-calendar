import { useCallback, useEffect, useRef, useState } from 'react';
import { api, type VacacionesMedico } from '../../api/client';

/**
 * Vacaciones de los médicos del servicio en un rango (todos o uno). El servidor comprueba el rol y
 * marca los días en conflicto con una guardia (002 FR-001, FR-014).
 */
export function useVacaciones(servicioId: number, desde: string, hasta: string, medicoId?: number) {
  const [datos, setDatos] = useState<VacacionesMedico[]>();
  const [error, setError] = useState<string>();
  const ultimaPeticion = useRef(0);

  const recargar = useCallback(async () => {
    const peticion = ++ultimaPeticion.current;
    const { data, error: problema } = await api.GET('/servicios/{servicioId}/vacaciones', {
      params: { path: { servicioId }, query: { desde, hasta, medicoId } },
    });
    if (peticion !== ultimaPeticion.current) return;
    if (data) {
      setDatos(data);
      setError(undefined);
    } else {
      setError(problema?.detail ?? 'No se han podido cargar las vacaciones.');
    }
  }, [servicioId, desde, hasta, medicoId]);

  useEffect(() => {
    setDatos(undefined);
    void recargar();
  }, [recargar]);

  return { datos, error, recargar };
}
