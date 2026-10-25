import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import FullCalendar from '@fullcalendar/react';
import type {
  DateSpanApi,
  DatesSetArg,
  EventClickArg,
  EventDropArg,
  EventInput,
  EventMountArg,
} from '@fullcalendar/core';
import esLocale from '@fullcalendar/core/locales/es';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import multiMonthPlugin from '@fullcalendar/multimonth';
import { api, type Cambio, type Guardia } from '../../api/client';
import { CesionForm } from './CesionForm';
import { Dialogo } from './Dialogo';
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

/** Guardia elegida en el popup; al arrastrarla, ya con el intercambio y la fecha de destino. */
interface EnCurso {
  guardia: Guardia;
  accion?: 'ceder' | 'intercambiar';
  fechaDestino?: string;
}

/**
 * Calendario del servicio con las vistas del legacy (`web/inicio.jsp`): año, mes, semana y día,
 * con «hoy» y enlaces de cada día o semana a su vista. Una guardia futura se arrastra a otro día
 * para intercambiarla, o se pulsa para ceder o intercambiar; las dos cosas abren el popup.
 * Arrastrar solo prepara el cambio: no se aplica nada hasta registrarlo, y el servidor valida
 * todas las reglas (FR-008, FR-016). Qué guardia es futura se calcula aquí solo para la interfaz.
 */
export function CalendarioPage({ servicioId, hoy = hoyEnServicio() }: Props) {
  const [rango, setRango] = useState<Rango>();
  const [guardias, setGuardias] = useState<Guardia[]>();
  const [error, setError] = useState<string>();
  const [enCurso, setEnCurso] = useState<EnCurso>();
  const seleccionada = enCurso?.guardia;
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
    setEnCurso(undefined);
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

  function abrir(cambio: EnCurso | undefined) {
    setEnCurso(cambio);
    setConfirmacion(undefined);
  }

  function cambioRegistrado(cambio: Cambio) {
    abrir(undefined);
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
        startEditable: g.fecha > hoy,
        durationEditable: false,
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
      if (guardia.fecha > hoy) abrir({ guardia });
    },
    [hoy],
  );

  // Como en el legacy: soltar la guardia en otro día propone el intercambio con ese día. La
  // guardia vuelve a su sitio en el acto; el calendario cambia solo al registrar.
  const soltarGuardia = useCallback((arg: EventDropArg) => {
    const guardia = arg.oldEvent.extendedProps.guardia as Guardia;
    const fechaDestino = arg.event.startStr.slice(0, 10);
    arg.revert();
    if (fechaDestino !== guardia.fecha) {
      abrir({ guardia, accion: 'intercambiar', fechaDestino });
    }
  }, []);

  const destinoPermitido = useCallback(
    (destino: DateSpanApi) => destino.startStr.slice(0, 10) > hoy,
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
        plugins={[dayGridPlugin, multiMonthPlugin, interactionPlugin]}
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
        eventDrop={soltarGuardia}
        eventAllow={destinoPermitido}
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

      {enCurso && (
        <Dialogo titulo="Cambio de guardia" onCerrar={() => abrir(undefined)}>
          {!enCurso.accion && (
            <section aria-label="Acciones de la guardia">
              <p>
                Guardia de {enCurso.guardia.medico.nombre} el {fechaLarga(enCurso.guardia.fecha)} (
                {etiquetaTipo(enCurso.guardia.tipo, enCurso.guardia.festivo)})
              </p>
              <button type="button" onClick={() => setEnCurso({ ...enCurso, accion: 'ceder' })}>
                Ceder
              </button>
              <button
                type="button"
                onClick={() => setEnCurso({ ...enCurso, accion: 'intercambiar' })}
              >
                Intercambiar
              </button>
              <button type="button" onClick={() => abrir(undefined)}>
                Cancelar
              </button>
            </section>
          )}

          {enCurso.accion === 'ceder' && (
            <CesionForm
              servicioId={servicioId}
              guardia={enCurso.guardia}
              onRegistrada={cambioRegistrado}
              onCancelar={() => abrir(undefined)}
            />
          )}

          {enCurso.accion === 'intercambiar' && (
            <IntercambioForm
              servicioId={servicioId}
              guardia={enCurso.guardia}
              fechaDestinoInicial={enCurso.fechaDestino}
              onRegistrado={cambioRegistrado}
              onCancelar={() => abrir(undefined)}
            />
          )}
        </Dialogo>
      )}
    </main>
  );
}
