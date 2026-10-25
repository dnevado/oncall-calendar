import { useEffect, useMemo, useState } from 'react';
import { DayPicker, type Modifiers } from 'react-day-picker';
import { es } from 'react-day-picker/locale';
import 'react-day-picker/style.css';
import type { Infraccion, Schemas } from '../../api/client';
import { modificarVacaciones, useVacaciones } from './useVacaciones';
import { aDate, aTexto, dias, fechaLargaConAnio, finDeMes, inicioDeMes } from './dias';
import './vacaciones.css';

interface Props {
  servicioId: number;
  medico: Schemas['MedicoResumen'];
  /** Hoy en la zona del servicio (`yyyy-MM-dd`). */
  hoy: string;
}

/** Un mes en pantallas estrechas y dos en escritorio (plan, «Móvil»). */
function useMesesVisibles(): number {
  const consulta = '(max-width: 640px)';
  const [estrecho, setEstrecho] = useState(
    () => typeof window.matchMedia === 'function' && window.matchMedia(consulta).matches,
  );
  useEffect(() => {
    if (typeof window.matchMedia !== 'function') return;
    const lista = window.matchMedia(consulta);
    const cambio = () => setEstrecho(lista.matches);
    lista.addEventListener('change', cambio);
    return () => lista.removeEventListener('change', cambio);
  }, []);
  return estrecho ? 1 : 2;
}

/** Estado de un día para el lector de pantalla: el color nunca es la única pista (FR-019). */
function etiqueta(fecha: Date, modificadores: Modifiers): string {
  const partes = [fechaLargaConAnio(fecha)];
  if (modificadores.nuevo) partes.push('se añadirá al guardar');
  else if (modificadores.quitado) partes.push('se quitará al guardar');
  else if (modificadores.guardado) partes.push('de vacaciones');
  if (modificadores.inicioPeriodo) partes.push('inicio del periodo');
  if (modificadores.disabled) partes.push('no editable');
  return partes.join(', ');
}

/** «añadir 2 días y quitar 1 día» o «2 días añadidos y 1 día quitado». */
function resumen(anadir: number, quitar: number, participio: boolean): string {
  const partes = [];
  if (anadir > 0) partes.push(participio ? dias(anadir, 'añadido') : `añadir ${dias(anadir)}`);
  if (quitar > 0) partes.push(participio ? dias(quitar, 'quitado') : `quitar ${dias(quitar)}`);
  return partes.join(' y ');
}

function diasEntre(a: string, b: string): string[] {
  const [desde, hasta] = a <= b ? [a, b] : [b, a];
  const resultado: string[] = [];
  for (let d = aDate(desde); aTexto(d) <= hasta; d.setDate(d.getDate() + 1)) {
    resultado.push(aTexto(d));
  }
  return resultado;
}

/**
 * Calendario de vacaciones de un médico: se marcan y desmarcan varios días con ratón o teclado y
 * se guardan de una vez. Solo prepara la operación; el servidor aplica las reglas (FR-018 a FR-020).
 */
export function EditorVacaciones({ servicioId, medico, hoy }: Props) {
  const mesesVisibles = useMesesVisibles();
  const [mes, setMes] = useState(() => aDate(hoy));
  const {
    datos,
    error: errorCarga,
    recargar,
  } = useVacaciones(servicioId, inicioDeMes(mes), finDeMes(mes, mesesVisibles - 1), medico.id);
  const [anadir, setAnadir] = useState<Set<string>>(new Set());
  const [quitar, setQuitar] = useState<Set<string>>(new Set());
  const [porPeriodo, setPorPeriodo] = useState(false);
  const [inicioPeriodo, setInicioPeriodo] = useState<string>();
  const [enviando, setEnviando] = useState(false);
  const [infracciones, setInfracciones] = useState<Infraccion[]>([]);
  const [error, setError] = useState<string>();
  const [confirmacion, setConfirmacion] = useState<string>();

  const guardados = useMemo(() => new Set((datos?.[0]?.dias ?? []).map((d) => d.fecha)), [datos]);

  function alternar(texto: string) {
    if (anadir.has(texto)) {
      setAnadir((actual) => new Set([...actual].filter((d) => d !== texto)));
    } else if (quitar.has(texto)) {
      setQuitar((actual) => new Set([...actual].filter((d) => d !== texto)));
    } else if (guardados.has(texto)) {
      setQuitar((actual) => new Set(actual).add(texto));
    } else {
      setAnadir((actual) => new Set(actual).add(texto));
    }
  }

  /** Periodo de días seguidos (FR-018, opcional): se guarda como días sueltos. */
  function marcarPeriodo(texto: string) {
    if (!inicioPeriodo) {
      setInicioPeriodo(texto);
      return;
    }
    const periodo = diasEntre(inicioPeriodo, texto).filter((d) => d >= hoy);
    setQuitar((actual) => new Set([...actual].filter((d) => !periodo.includes(d))));
    setAnadir((actual) => new Set([...actual, ...periodo.filter((d) => !guardados.has(d))]));
    setInicioPeriodo(undefined);
  }

  function elegir(fecha: Date) {
    setConfirmacion(undefined);
    const texto = aTexto(fecha);
    if (porPeriodo) marcarPeriodo(texto);
    else alternar(texto);
  }

  function descartar() {
    setAnadir(new Set());
    setQuitar(new Set());
    setInicioPeriodo(undefined);
    setInfracciones([]);
    setError(undefined);
  }

  async function guardar() {
    setEnviando(true);
    setInfracciones([]);
    setError(undefined);
    setConfirmacion(undefined);
    const respuesta = await modificarVacaciones(servicioId, medico.id, {
      anadir: [...anadir].sort(),
      quitar: [...quitar].sort(),
    });
    setEnviando(false);
    if (respuesta.ok) {
      const { anadidos, quitados } = respuesta.resultado;
      setAnadir(new Set());
      setQuitar(new Set());
      setConfirmacion(
        `Vacaciones guardadas: ${resumen(anadidos.length, quitados.length, true) || 'sin cambios'}.`,
      );
      await recargar();
    } else {
      setInfracciones(respuesta.infracciones);
      setError(respuesta.error);
    }
  }

  const seleccionados = [...[...guardados].filter((d) => !quitar.has(d)), ...anadir].map(aDate);
  const hayCambios = anadir.size + quitar.size > 0;

  return (
    <section className="editor-vacaciones" aria-labelledby="editor-vacaciones-titulo">
      <h2 id="editor-vacaciones-titulo">Vacaciones de {medico.nombre}</h2>
      {errorCarga && <p role="alert">{errorCarga}</p>}
      <label className="modo-periodo">
        <input
          type="checkbox"
          checked={porPeriodo}
          onChange={(e) => {
            setPorPeriodo(e.target.checked);
            setInicioPeriodo(undefined);
          }}
        />{' '}
        Elegir un periodo (primer y último día)
      </label>
      {porPeriodo && (
        <p className="ayuda" aria-live="polite">
          {inicioPeriodo
            ? `Inicio del periodo: ${fechaLargaConAnio(aDate(inicioPeriodo))}. Elige el último día.`
            : 'Elige el primer día del periodo.'}
        </p>
      )}
      <DayPicker
        mode="multiple"
        locale={es}
        weekStartsOn={1}
        numberOfMonths={mesesVisibles}
        month={mes}
        onMonthChange={setMes}
        selected={seleccionados}
        onSelect={(_, fecha) => elegir(fecha)}
        disabled={{ before: aDate(hoy) }}
        modifiers={{
          guardado: [...guardados].filter((d) => !quitar.has(d)).map(aDate),
          nuevo: [...anadir].map(aDate),
          quitado: [...quitar].map(aDate),
          inicioPeriodo: inicioPeriodo ? [aDate(inicioPeriodo)] : [],
        }}
        modifiersClassNames={{
          guardado: 'dia-guardado',
          nuevo: 'dia-nuevo',
          quitado: 'dia-quitado',
          inicioPeriodo: 'dia-inicio-periodo',
        }}
        labels={{ labelDayButton: etiqueta }}
      />
      <ul className="leyenda-vacaciones" aria-label="Leyenda">
        <li>
          <span className="muestra dia-guardado" aria-hidden="true" /> De vacaciones
        </li>
        <li>
          <span className="muestra dia-nuevo" aria-hidden="true" /> Se añadirá al guardar
        </li>
        <li>
          <span className="muestra dia-quitado" aria-hidden="true" /> Se quitará al guardar
        </li>
      </ul>
      <p className="pendientes" aria-live="polite">
        {hayCambios
          ? `Sin guardar: ${resumen(anadir.size, quitar.size, false)}.`
          : 'Sin cambios pendientes.'}
      </p>
      <div className="acciones">
        <button
          type="button"
          className="primario"
          disabled={!hayCambios || enviando}
          onClick={() => void guardar()}
        >
          Guardar vacaciones
        </button>
        <button type="button" disabled={!hayCambios || enviando} onClick={descartar}>
          Descartar cambios
        </button>
      </div>
      {confirmacion && <p role="status">{confirmacion}</p>}
      {infracciones.length > 0 && (
        <div role="alert">
          <p>No se han guardado las vacaciones:</p>
          <ul>
            {infracciones.map((i, indice) => (
              <li key={`${i.regla}-${i.fecha ?? indice}`}>{i.mensaje}</li>
            ))}
          </ul>
        </div>
      )}
      {error && <p role="alert">{error}</p>}
    </section>
  );
}
