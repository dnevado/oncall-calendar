import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import FullCalendar from '@fullcalendar/react';
import type { DatesSetArg, EventClickArg, EventInput, EventMountArg } from '@fullcalendar/core';
import esLocale from '@fullcalendar/core/locales/es';
import dayGridPlugin from '@fullcalendar/daygrid';
import multiMonthPlugin from '@fullcalendar/multimonth';
import { api, type Cambio, type Guardia } from '../../api/client';
import { CesionForm } from './CesionForm';
import { IntercambioForm } from './IntercambioForm';
import { etiquetaTipo, fechaLarga, hoyEnServicio, ordenTipo, sumarDias, TIPOS } from './fechas';
import './calendario.css';

interface Props {
  servicioId: number;
  /** Solo para pruebas; por defecto, hoy en la zona del servicio. */
  hoy?: string;
}

interface Rango {
  desde: string;
  hasta: string;
}

/**
 * Calendario del servicio con las vistas del legacy (`web/inicio.jsp`): año, mes, semana y día,
 * con «hoy» y enlaces de cada día o semana a su vista. Al elegir una guardia futura se ofrecen
 * las acciones. Qué guardia es futura se calcula aquí solo para la interfaz: el servidor vuelve a
 * validar (FR-008).
 */
export function CalendarioPage({ servicioId, hoy = hoyEnServicio() }: Props) {
  const [rango, setRango] = useState<Rango>();
  const [guardias, setGuardias] = useState<Guardia[]>();
  const [error, setError] = useState<string>();
  const [seleccionada, setSeleccionada] = useState<Guardia>();
  const [accion, setAccion] = useState<'ceder' | 'intercambiar'>();
  const [confirmacion, setConfirmacion] = useState<string>();
  // Solo cuenta la respuesta de la última petición: una tardía de otro periodo se descarta.
  const ultimaPeticion = useRef(0);

  const cargar = useCallback(async () => {
    if (!rango) return;
    const peticion = ++ultimaPeticion.current;
    const { data, error: problema } = await api.GET('/servicios/{servicioId}/guardias', {
      params: { path: { servicioId }, query: rango },
    });
    if (peticion !== ultimaPeticion.current) return;
    if (data) {
      setGuardias(data);
      setError(undefined);
    } else {
      setError(problema?.detail ?? 'No se ha podido cargar el calendario.');
    }
  }, [rango, servicioId]);

  useEffect(() => {
    setSeleccionada(undefined);
    setAccion(undefined);
    void cargar();
  }, [cargar]);

  // FullCalendar avisa al cambiar de vista o de periodo. `end` es exclusivo.
  const rangoActual = useRef<Rango>();
  const periodoVisible = useCallback((arg: DatesSetArg) => {
    const desde = arg.start.toISOString().slice(0, 10);
    const hasta = sumarDias(arg.end.toISOString().slice(0, 10), -1);
    if (rangoActual.current?.desde === desde && rangoActual.current.hasta === hasta) return;
    rangoActual.current = { desde, hasta };
    setRango(rangoActual.current);
    setGuardias(undefined);
    setError(undefined);
    setConfirmacion(undefined);
  }, []);

  function seleccionar(guardia: Guardia | undefined) {
    setSeleccionada(guardia);
    setAccion(undefined);
    setConfirmacion(undefined);
  }

  function cambioRegistrado(cambio: Cambio) {
    seleccionar(undefined);
    setConfirmacion(
      cambio.tipo === 'CAMBIO' && cambio.fechaDestino
        ? `Intercambio registrado: ${cambio.receptor.nombre} hace la guardia del ${fechaLarga(cambio.fechaOrigen)} y ${cambio.cedente.nombre} la del ${fechaLarga(cambio.fechaDestino)}.`
        : `Cesión registrada: la guardia del ${fechaLarga(cambio.fechaOrigen)} pasa de ${cambio.cedente.nombre} a ${cambio.receptor.nombre}.`,
    );
    void cargar();
  }

  const eventos = useMemo<EventInput[]>(
    () =>
      (guardias ?? []).map((g) => ({
        id: String(g.id),
        title: `${g.medico.nombre} — ${etiquetaTipo(g.tipo, g.festivo)}`,
        start: g.fecha,
        allDay: true,
        interactive: g.fecha > hoy,
        classNames: [
          'guardia',
          `guardia--${g.tipo.toLowerCase()}`,
          ...(g.festivo ? ['guardia--festivo'] : []),
          ...(g.fecha > hoy ? [] : ['guardia--pasada']),
          ...(seleccionada?.id === g.id ? ['guardia--seleccionada'] : []),
        ],
        extendedProps: { guardia: g, orden: ordenTipo(g.tipo), idGuardia: g.id },
      })),
    [guardias, hoy, seleccionada],
  );

  const pulsarGuardia = useCallback(
    (arg: EventClickArg) => {
      const guardia = arg.event.extendedProps.guardia as Guardia;
      if (guardia.fecha > hoy) seleccionar(guardia);
    },
    [hoy],
  );

  // Las guardias que se pueden elegir se anuncian como botones (FullCalendar ya les da foco y teclado).
  const montarGuardia = useCallback(
    (arg: EventMountArg) => {
      const guardia = arg.event.extendedProps.guardia as Guardia;
      if (guardia.fecha > hoy) {
        arg.el.setAttribute('role', 'button');
        arg.el.setAttribute('aria-label', arg.event.title);
      }
    },
    [hoy],
  );

  return (
    <main className="calendario">
      {confirmacion && <p role="status">{confirmacion}</p>}
      {error && <p role="alert">{error}</p>}
      {rango && !guardias && !error && <p role="status">Cargando…</p>}
      {guardias && guardias.length === 0 && <p>No hay guardias en este periodo.</p>}

      <FullCalendar
        plugins={[dayGridPlugin, multiMonthPlugin]}
        locale={esLocale}
        buttonHints={{ prev: '$0 anterior' }}
        timeZone="UTC"
        now={hoy}
        initialDate={hoy}
        initialView="dayGridMonth"
        headerToolbar={{
          left: 'prev,next today',
          center: 'title',
          right: 'multiMonthYear,dayGridMonth,dayGridWeek,dayGridDay',
        }}
        fixedWeekCount={false}
        navLinks
        dayMaxEvents
        height="auto"
        multiMonthMaxColumns={3}
        eventOrder={['orden', 'idGuardia']}
        events={eventos}
        eventClick={pulsarGuardia}
        eventDidMount={montarGuardia}
        datesSet={periodoVisible}
      />

      <ul className="leyenda" aria-label="Leyenda">
        {Object.entries(TIPOS).map(([tipo, nombre]) => (
          <li key={tipo} className={`guardia guardia--${tipo.toLowerCase()}`}>
            {nombre}
          </li>
        ))}
        <li className="guardia guardia--festivo">Festivo</li>
      </ul>

      {seleccionada && !accion && (
        <aside aria-label="Acciones de la guardia">
          <p>
            Guardia de {seleccionada.medico.nombre} el {fechaLarga(seleccionada.fecha)} (
            {etiquetaTipo(seleccionada.tipo, seleccionada.festivo)})
          </p>
          <button type="button" onClick={() => setAccion('ceder')}>
            Ceder
          </button>
          <button type="button" onClick={() => setAccion('intercambiar')}>
            Intercambiar
          </button>
          <button type="button" onClick={() => seleccionar(undefined)}>
            Cancelar
          </button>
        </aside>
      )}

      {seleccionada && accion === 'ceder' && (
        <CesionForm
          servicioId={servicioId}
          guardia={seleccionada}
          onRegistrada={cambioRegistrado}
          onCancelar={() => setAccion(undefined)}
        />
      )}

      {seleccionada && accion === 'intercambiar' && (
        <IntercambioForm
          servicioId={servicioId}
          guardia={seleccionada}
          onRegistrado={cambioRegistrado}
          onCancelar={() => setAccion(undefined)}
        />
      )}
    </main>
  );
}
