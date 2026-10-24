import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { api, type Cambio, type Guardia } from '../../api/client';
import { CesionForm } from './CesionForm';
import { desplazarMes, etiquetaTipo, fechaLarga, hoyEnServicio, mesDe, nombreMes, rangoDelMes, type Mes } from './fechas';

interface Props {
  servicioId: number;
  /** Solo para pruebas; por defecto, hoy en la zona del servicio. */
  hoy?: string;
}

/**
 * Calendario mensual del servicio. Al elegir una guardia futura se ofrecen las acciones.
 * Qué guardia es futura se calcula aquí solo para la interfaz: el servidor vuelve a validar (FR-008).
 */
export function CalendarioPage({ servicioId, hoy = hoyEnServicio() }: Props) {
  const [mes, setMes] = useState<Mes>(() => mesDe(hoy));
  const [guardias, setGuardias] = useState<Guardia[]>();
  const [error, setError] = useState<string>();
  const [seleccionada, setSeleccionada] = useState<Guardia>();
  const [cediendo, setCediendo] = useState(false);
  const [confirmacion, setConfirmacion] = useState<string>();
  // Solo cuenta la respuesta de la última petición: una tardía de otro mes se descarta.
  const ultimaPeticion = useRef(0);

  const cargar = useCallback(async () => {
    const peticion = ++ultimaPeticion.current;
    const { desde, hasta } = rangoDelMes(mes);
    const { data, error: problema } = await api.GET('/servicios/{servicioId}/guardias', {
      params: { path: { servicioId }, query: { desde, hasta } },
    });
    if (peticion !== ultimaPeticion.current) return;
    if (data) {
      setGuardias(data);
      setError(undefined);
    } else {
      setError(problema?.detail ?? 'No se ha podido cargar el calendario.');
    }
  }, [mes, servicioId]);

  useEffect(() => {
    setSeleccionada(undefined);
    setCediendo(false);
    void cargar();
  }, [cargar]);

  function cambiarMes(meses: number) {
    setMes((m) => desplazarMes(m, meses));
    setGuardias(undefined);
    setError(undefined);
    setConfirmacion(undefined);
  }

  function seleccionar(guardia: Guardia | undefined) {
    setSeleccionada(guardia);
    setCediendo(false);
    setConfirmacion(undefined);
  }

  function cesionRegistrada(cambio: Cambio) {
    seleccionar(undefined);
    setConfirmacion(
      `Cesión registrada: la guardia del ${fechaLarga(cambio.fechaOrigen)} pasa de ${cambio.cedente.nombre} a ${cambio.receptor.nombre}.`,
    );
    void cargar();
  }

  const porDia = useMemo(() => {
    const dias = new Map<string, Guardia[]>();
    for (const g of guardias ?? []) {
      dias.set(g.fecha, [...(dias.get(g.fecha) ?? []), g]);
    }
    return [...dias.entries()];
  }, [guardias]);

  return (
    <main className="calendario">
      <header>
        <button type="button" onClick={() => cambiarMes(-1)} aria-label="Mes anterior">
          ‹
        </button>
        <h2>{nombreMes(mes)}</h2>
        <button type="button" onClick={() => cambiarMes(1)} aria-label="Mes siguiente">
          ›
        </button>
      </header>

      {confirmacion && <p role="status">{confirmacion}</p>}
      {error && <p role="alert">{error}</p>}
      {!guardias && !error && <p>Cargando…</p>}
      {guardias && porDia.length === 0 && <p>No hay guardias este mes.</p>}

      {porDia.map(([fecha, delDia]) => (
        <section key={fecha} aria-label={fechaLarga(fecha)}>
          <h3>{fechaLarga(fecha)}</h3>
          <ul>
            {delDia.map((g) => {
              const texto = `${g.medico.nombre} — ${etiquetaTipo(g.tipo, g.festivo)}`;
              return (
                <li key={g.id}>
                  {g.fecha > hoy ? (
                    <button type="button" aria-pressed={seleccionada?.id === g.id} onClick={() => seleccionar(g)}>
                      {texto}
                    </button>
                  ) : (
                    <span>{texto}</span>
                  )}
                </li>
              );
            })}
          </ul>
        </section>
      ))}

      {seleccionada && !cediendo && (
        <aside aria-label="Acciones de la guardia">
          <p>
            Guardia de {seleccionada.medico.nombre} el {fechaLarga(seleccionada.fecha)} (
            {etiquetaTipo(seleccionada.tipo, seleccionada.festivo)})
          </p>
          <button type="button" onClick={() => setCediendo(true)}>
            Ceder
          </button>
          {/* El intercambio llega con la historia 2. */}
          <button type="button" disabled title="Disponible próximamente">
            Intercambiar
          </button>
          <button type="button" onClick={() => seleccionar(undefined)}>
            Cancelar
          </button>
        </aside>
      )}

      {seleccionada && cediendo && (
        <CesionForm
          servicioId={servicioId}
          guardia={seleccionada}
          onRegistrada={cesionRegistrada}
          onCancelar={() => setCediendo(false)}
        />
      )}
    </main>
  );
}
