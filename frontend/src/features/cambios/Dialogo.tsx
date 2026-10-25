import { useEffect, useId, useRef, type KeyboardEvent, type ReactNode } from 'react';

interface Props {
  titulo: string;
  onCerrar: () => void;
  children: ReactNode;
}

const ENFOCABLES =
  'button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), a[href], [tabindex]:not([tabindex="-1"])';

/**
 * Ventana modal (como el popup de cambio del legacy, `web/inicio.jsp`). Lleva el foco dentro al
 * abrirse, lo mantiene dentro con Tab, se cierra con Escape y lo devuelve al cerrarse.
 */
export function Dialogo({ titulo, onCerrar, children }: Props) {
  const idTitulo = useId();
  const ventana = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const anterior = document.activeElement as HTMLElement | null;
    ventana.current?.querySelector<HTMLElement>(ENFOCABLES)?.focus();
    return () => anterior?.focus();
  }, []);

  function teclado(evento: KeyboardEvent<HTMLDivElement>) {
    if (evento.key === 'Escape') {
      evento.stopPropagation();
      onCerrar();
      return;
    }
    if (evento.key !== 'Tab' || !ventana.current) return;
    const enfocables = [...ventana.current.querySelectorAll<HTMLElement>(ENFOCABLES)];
    const primero = enfocables[0];
    const ultimo = enfocables[enfocables.length - 1];
    if (!primero || !ultimo) return;
    if (evento.shiftKey && document.activeElement === primero) {
      evento.preventDefault();
      ultimo.focus();
    } else if (!evento.shiftKey && document.activeElement === ultimo) {
      evento.preventDefault();
      primero.focus();
    }
  }

  return (
    <div className="dialogo-fondo">
      <div
        ref={ventana}
        className="dialogo"
        role="dialog"
        aria-modal="true"
        aria-labelledby={idTitulo}
        onKeyDown={teclado}
      >
        <h2 id={idTitulo}>{titulo}</h2>
        {children}
      </div>
    </div>
  );
}
