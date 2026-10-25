import { useCallback, useEffect, useRef, useState } from 'react';
import { api, type Cambio, type Guardia } from '../../api/client';
import { CesionForm } from './CesionForm';
import { IntercambioForm } from './IntercambioForm';
import {
  desplazarMes,
  fechaLarga,
  hoyEnServicio,
  mesDe,
  nombreMes,
  rangoDelMes,
  ZONA_SERVICIO,
  type Mes,
} from './fechas';

interface Props {
  servicioId: number;
  /** Solo para pruebas; por defecto, hoy en la zona del servicio. */
  hoy?: string;
}

/** El cambio inverso y la guardia que entrega quien recibió en el original. */
interface Inverso {
  original: Cambio;
  guardia: Guardia;
}

const REGISTRO = new Intl.DateTimeFormat('es-ES', {
  dateStyle: 'short',
  timeStyle: 'short',
  timeZone: ZONA_SERVICIO,
});

/** Las fechas de un cambio que tienen que ser futuras para poder deshacerlo (FR-008, FR-009). */
function fechasDe(cambio: Cambio): string[] {
  return cambio.fechaDestino ? [cambio.fechaOrigen, cambio.fechaDestino] : [cambio.fechaOrigen];
}

function descripcion(cambio: Cambio): string {
  return cambio.tipo === 'CAMBIO' && cambio.fechaDestino
    ? `Intercambio: ${cambio.cedente.nombre} (${fechaLarga(cambio.fechaOrigen)}) con ${cambio.receptor.nombre} (${fechaLarga(cambio.fechaDestino)})`
    : `Cesión del ${fechaLarga(cambio.fechaOrigen)}: de ${cambio.cedente.nombre} a ${cambio.receptor.nombre}`;
}

/**
 * Cambios registrados en el mes. Un cambio no se edita ni se anula: se corrige registrando el
 * inverso como un cambio normal, sujeto a todas las reglas (FR-028).
 */
export function CambiosListPage({ servicioId, hoy = hoyEnServicio() }: Props) {
  const [mes, setMes] = useState<Mes>(() => mesDe(hoy));
  const [cambios, setCambios] = useState<Cambio[]>();
  const [error, setError] = useState<string>();
  const [inverso, setInverso] = useState<Inverso>();
  const [aviso, setAviso] = useState<string>();
  const [confirmacion, setConfirmacion] = useState<string>();
  const ultimaPeticion = useRef(0);

  const cargar = useCallback(async () => {
    const peticion = ++ultimaPeticion.current;
    const { desde, hasta } = rangoDelMes(mes);
    const { data, error: problema } = await api.GET('/servicios/{servicioId}/cambios', {
      params: { path: { servicioId }, query: { desde, hasta } },
    });
    if (peticion !== ultimaPeticion.current) return;
    if (data) {
      setCambios(data);
      setError(undefined);
    } else {
      setError(problema?.detail ?? 'No se han podido cargar los cambios.');
    }
  }, [mes, servicioId]);

  useEffect(() => {
    setInverso(undefined);
    void cargar();
  }, [cargar]);

  function cambiarMes(meses: number) {
    setMes((m) => desplazarMes(m, meses));
    setCambios(undefined);
    setError(undefined);
    setAviso(undefined);
    setConfirmacion(undefined);
  }

  /** Busca la guardia del día origen, que ahora es de quien la recibió. */
  async function prepararInverso(original: Cambio) {
    setAviso(undefined);
    setConfirmacion(undefined);
    const fecha = original.fechaOrigen;
    const { data } = await api.GET('/servicios/{servicioId}/guardias', {
      params: { path: { servicioId }, query: { desde: fecha, hasta: fecha } },
    });
    const guardia = data?.find((g) => g.medico.id === original.receptor.id);
    if (guardia) {
      setInverso({ original, guardia });
    } else {
      setAviso(
        `La guardia del ${fechaLarga(fecha)} ya no es de ${original.receptor.nombre}: no se puede preparar el cambio inverso.`,
      );
    }
  }

  function inversoRegistrado() {
    setInverso(undefined);
    setConfirmacion('Cambio inverso registrado.');
    void cargar();
  }

  return (
    <main className="cambios">
      <header>
        <button type="button" onClick={() => cambiarMes(-1)} aria-label="Mes anterior">
          ‹
        </button>
        <h2>Cambios de {nombreMes(mes)}</h2>
        <button type="button" onClick={() => cambiarMes(1)} aria-label="Mes siguiente">
          ›
        </button>
      </header>

      {confirmacion && <p role="status">{confirmacion}</p>}
      {aviso && <p role="alert">{aviso}</p>}
      {error && <p role="alert">{error}</p>}
      {!cambios && !error && <p>Cargando…</p>}
      {cambios?.length === 0 && <p>No hay cambios registrados este mes.</p>}

      {cambios && cambios.length > 0 && (
        <ul>
          {cambios.map((cambio) => {
            const corregible = fechasDe(cambio).every((f) => f > hoy);
            return (
              <li key={cambio.id}>
                <p>{descripcion(cambio)}</p>
                <p>
                  Registrado por {cambio.registradoPor.nombre} el{' '}
                  {REGISTRO.format(new Date(cambio.registradoEn))}
                </p>
                <button
                  type="button"
                  disabled={!corregible || inverso !== undefined}
                  onClick={() => void prepararInverso(cambio)}
                  aria-label={`Registrar cambio inverso: ${descripcion(cambio)}`}
                >
                  Registrar cambio inverso
                </button>
                {!corregible && <p>No se puede corregir: alguna de sus fechas ya no es futura.</p>}
              </li>
            );
          })}
        </ul>
      )}

      {inverso?.original.tipo === 'CESION' && (
        <CesionForm
          servicioId={servicioId}
          guardia={inverso.guardia}
          receptorInicial={inverso.original.cedente}
          onRegistrada={inversoRegistrado}
          onCancelar={() => setInverso(undefined)}
        />
      )}
      {inverso?.original.tipo === 'CAMBIO' && (
        <IntercambioForm
          servicioId={servicioId}
          guardia={inverso.guardia}
          fechaDestinoInicial={inverso.original.fechaDestino}
          receptorInicial={inverso.original.cedente}
          onRegistrado={inversoRegistrado}
          onCancelar={() => setInverso(undefined)}
        />
      )}
    </main>
  );
}
