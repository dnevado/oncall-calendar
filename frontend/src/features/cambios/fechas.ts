import type { Schemas } from '../../api/client';

/** Fechas como texto `yyyy-MM-dd`, igual que el contrato. «Hoy» es el de la zona del servicio. */
export const ZONA_SERVICIO = 'Europe/Madrid';

export function hoyEnServicio(ahora: Date = new Date()): string {
  return new Intl.DateTimeFormat('en-CA', { timeZone: ZONA_SERVICIO }).format(ahora);
}

export interface Mes {
  anio: number;
  /** 1–12 */
  mes: number;
}

export function mesDe(fecha: string): Mes {
  const [anio, mes] = fecha.split('-').map(Number);
  return { anio: anio ?? 1970, mes: mes ?? 1 };
}

export function desplazarMes({ anio, mes }: Mes, delta: number): Mes {
  const indice = anio * 12 + (mes - 1) + delta;
  return { anio: Math.floor(indice / 12), mes: (indice % 12) + 1 };
}

function dosDigitos(n: number): string {
  return String(n).padStart(2, '0');
}

export function rangoDelMes({ anio, mes }: Mes): { desde: string; hasta: string } {
  const ultimo = new Date(Date.UTC(anio, mes, 0)).getUTCDate();
  return { desde: `${anio}-${dosDigitos(mes)}-01`, hasta: `${anio}-${dosDigitos(mes)}-${dosDigitos(ultimo)}` };
}

export function nombreMes({ anio, mes }: Mes): string {
  return new Intl.DateTimeFormat('es-ES', { month: 'long', year: 'numeric', timeZone: 'UTC' }).format(
    new Date(Date.UTC(anio, mes - 1, 1)),
  );
}

export function fechaLarga(fecha: string): string {
  const [anio, mes, dia] = fecha.split('-').map(Number);
  return new Intl.DateTimeFormat('es-ES', { weekday: 'long', day: 'numeric', month: 'long', timeZone: 'UTC' }).format(
    new Date(Date.UTC(anio ?? 1970, (mes ?? 1) - 1, dia ?? 1)),
  );
}

const TIPOS: Record<Schemas['TipoGuardia'], string> = {
  PRESENCIA: 'Presencia',
  LOCALIZADA: 'Localizada',
  REFUERZO: 'Refuerzo',
  RESIDENTE: 'Residente',
};

export function etiquetaTipo(tipo: Schemas['TipoGuardia'], festivo: boolean): string {
  return festivo ? `${TIPOS[tipo]} (festivo)` : TIPOS[tipo];
}
