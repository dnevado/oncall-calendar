import { useCallback, useEffect, useRef, useState } from 'react';
import {
  api,
  type Infraccion,
  type ModificarVacacionesRequest,
  type ResultadoVacaciones,
  type VacacionesMedico,
} from '../../api/client';

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

export type ResultadoModificar =
  | { ok: true; resultado: ResultadoVacaciones }
  | { ok: false; infracciones: Infraccion[]; error?: string };

/**
 * Añade y quita días de un médico en una sola operación. El servidor valida las reglas (VAC-003,
 * VAC-008) y, si alguna falla, no cambia nada y devuelve todas (FR-006, FR-011, FR-020).
 */
export async function modificarVacaciones(
  servicioId: number,
  medicoId: number,
  cuerpo: ModificarVacacionesRequest,
): Promise<ResultadoModificar> {
  const {
    data,
    error: problema,
    response,
  } = await api.PATCH('/servicios/{servicioId}/medicos/{medicoId}/vacaciones', {
    params: { path: { servicioId, medicoId } },
    body: cuerpo,
  });
  if (data) {
    return { ok: true, resultado: data };
  }
  if (response.status === 422 && problema && 'infracciones' in problema) {
    return { ok: false, infracciones: problema.infracciones };
  }
  return {
    ok: false,
    infracciones: [],
    error: problema?.detail ?? 'No se han podido guardar las vacaciones.',
  };
}
