import { useState } from 'react';
import { api, type Cambio, type Infraccion, type Schemas } from '../../api/client';

function nuevaClave(): string {
  return crypto.randomUUID();
}

/**
 * Envío de un cambio o una cesión. El servidor valida todas las reglas y, si alguna falla, devuelve
 * todas (FR-016); aquí solo se muestran.
 */
export function useRegistrarCambio(
  servicioId: number,
  onRegistrado: (cambio: Cambio) => void,
  textoError: string,
) {
  const [enviando, setEnviando] = useState(false);
  const [infracciones, setInfracciones] = useState<Infraccion[]>([]);
  const [error, setError] = useState<string>();
  // Una clave por envío: un reintento del mismo envío no aplica el cambio dos veces.
  const [clave, setClave] = useState(nuevaClave);

  async function registrar(cuerpo: Schemas['RegistrarCambioRequest']) {
    setEnviando(true);
    setInfracciones([]);
    setError(undefined);
    const {
      data,
      error: problema,
      response,
    } = await api.POST('/servicios/{servicioId}/cambios', {
      params: { path: { servicioId }, header: { 'Idempotency-Key': clave } },
      body: cuerpo,
    });
    setEnviando(false);
    if (data) {
      onRegistrado(data);
      return;
    }
    setClave(nuevaClave());
    if (response.status === 422 && problema && 'infracciones' in problema) {
      setInfracciones(problema.infracciones);
    } else {
      setError(problema?.detail ?? textoError);
    }
  }

  return { registrar, enviando, infracciones, error };
}
