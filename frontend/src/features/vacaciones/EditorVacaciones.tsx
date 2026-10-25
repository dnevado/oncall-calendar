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
  else if (modificadores.guardado) partes.push('de vacaciones');
  if (modificadores.disabled) partes.push('no editable');
  return partes.join(', ');
}

/**
 * Calendario de vacaciones de un médico: se marcan varios días con ratón o teclado y se guardan
 * de una vez. Solo prepara la operación; el servidor aplica las reglas (FR-018 a FR-020).
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
  const [enviando, setEnviando] = useState(false);
  const [infracciones, setInfracciones] = useState<Infraccion[]>([]);
  const [error, setError] = useState<string>();
  const [confirmacion, setConfirmacion] = useState<string>();

  const guardados = useMemo(() => new Set((datos?.[0]?.dias ?? []).map((d) => d.fecha)), [datos]);

  function alternar(fecha: Date) {
    const texto = aTexto(fecha);
    setConfirmacion(undefined);
    if (guardados.has(texto) && !anadir.has(texto)) return;
    setAnadir((actual) => {
      const siguiente = new Set(actual);
      if (siguiente.has(texto)) siguiente.delete(texto);
      else siguiente.add(texto);
      return siguiente;
    });
  }

  async function guardar() {
    setEnviando(true);
    setInfracciones([]);
    setError(undefined);
    setConfirmacion(undefined);
    const respuesta = await modificarVacaciones(servicioId, medico.id, {
      anadir: [...anadir].sort(),
      quitar: [],
    });
    setEnviando(false);
    if (respuesta.ok) {
      setAnadir(new Set());
      setConfirmacion(
        `Vacaciones guardadas: ${dias(respuesta.resultado.anadidos.length, 'añadido')}.`,
      );
      await recargar();
    } else {
      setInfracciones(respuesta.infracciones);
      setError(respuesta.error);
    }
  }

  const seleccionados = [...guardados, ...anadir].map(aDate);
  const pendientes = anadir.size;

  return (
    <section className="editor-vacaciones" aria-labelledby="editor-vacaciones-titulo">
      <h2 id="editor-vacaciones-titulo">Vacaciones de {medico.nombre}</h2>
      {errorCarga && <p role="alert">{errorCarga}</p>}
      <DayPicker
        mode="multiple"
        locale={es}
        weekStartsOn={1}
        numberOfMonths={mesesVisibles}
        month={mes}
        onMonthChange={setMes}
        selected={seleccionados}
        onSelect={(_, fecha) => alternar(fecha)}
        disabled={{ before: aDate(hoy) }}
        modifiers={{
          guardado: [...guardados].map(aDate),
          nuevo: [...anadir].map(aDate),
        }}
        modifiersClassNames={{ guardado: 'dia-guardado', nuevo: 'dia-nuevo' }}
        labels={{ labelDayButton: etiqueta }}
      />
      <ul className="leyenda-vacaciones" aria-label="Leyenda">
        <li>
          <span className="muestra dia-guardado" aria-hidden="true" /> De vacaciones
        </li>
        <li>
          <span className="muestra dia-nuevo" aria-hidden="true" /> Se añadirá al guardar
        </li>
      </ul>
      <p className="pendientes" aria-live="polite">
        {pendientes > 0 ? `Sin guardar: añadir ${dias(pendientes)}.` : 'Sin cambios pendientes.'}
      </p>
      <div className="acciones">
        <button
          type="button"
          className="primario"
          disabled={pendientes === 0 || enviando}
          onClick={() => void guardar()}
        >
          Guardar vacaciones
        </button>
        <button
          type="button"
          disabled={pendientes === 0 || enviando}
          onClick={() => {
            setAnadir(new Set());
            setInfracciones([]);
            setError(undefined);
          }}
        >
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
