/**
 * Conversión entre el texto `yyyy-MM-dd` del contrato y el `Date` local del selector. Se usa la
 * fecha local a mediodía para que ningún cambio de hora mueva el día.
 */
export function aDate(texto: string): Date {
  const [anio, mes, dia] = texto.split('-').map(Number);
  return new Date(anio ?? 1970, (mes ?? 1) - 1, dia ?? 1, 12);
}

function dosDigitos(n: number): string {
  return String(n).padStart(2, '0');
}

export function aTexto(fecha: Date): string {
  return `${fecha.getFullYear()}-${dosDigitos(fecha.getMonth() + 1)}-${dosDigitos(fecha.getDate())}`;
}

/** Primer día del mes de `fecha` desplazado `meses`, como texto. */
export function inicioDeMes(fecha: Date, meses = 0): string {
  return aTexto(new Date(fecha.getFullYear(), fecha.getMonth() + meses, 1, 12));
}

/** Último día del mes de `fecha` desplazado `meses`, como texto. */
export function finDeMes(fecha: Date, meses = 0): string {
  return aTexto(new Date(fecha.getFullYear(), fecha.getMonth() + meses + 1, 0, 12));
}

const LARGA = new Intl.DateTimeFormat('es-ES', {
  weekday: 'long',
  day: 'numeric',
  month: 'long',
  year: 'numeric',
});

export function fechaLargaConAnio(fecha: Date): string {
  return LARGA.format(fecha);
}

/** «1 día», «3 días», y con participio: «1 día añadido», «3 días añadidos». */
export function dias(n: number, participio?: string): string {
  const texto = n === 1 ? '1 día' : `${n} días`;
  if (!participio) return texto;
  return n === 1 ? `${texto} ${participio}` : `${texto} ${participio}s`;
}
