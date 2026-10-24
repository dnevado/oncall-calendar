import createClient, { type Middleware } from 'openapi-fetch';
import type { components, paths } from './schema';

export type Schemas = components['schemas'];
export type Usuario = Schemas['Usuario'];
export type Guardia = Schemas['Guardia'];
export type Candidato = Schemas['Candidato'];
export type Cambio = Schemas['Cambio'];
export type Problem = Schemas['Problem'];
export type ReglasIncumplidas = Schemas['ReglasIncumplidas'];
export type Infraccion = Schemas['Infraccion'];

const METODOS_SEGUROS = new Set(['GET', 'HEAD', 'OPTIONS']);

/** Valor de la cookie `XSRF-TOKEN` que emite el backend (Spring Security, CSRF para SPA). */
export function leerTokenCsrf(cookies: string = document.cookie): string | undefined {
  const par = cookies
    .split(';')
    .map((c) => c.trim())
    .find((c) => c.startsWith('XSRF-TOKEN='));
  return par ? decodeURIComponent(par.slice('XSRF-TOKEN='.length)) : undefined;
}

const csrf: Middleware = {
  onRequest({ request }) {
    if (!METODOS_SEGUROS.has(request.method)) {
      const token = leerTokenCsrf();
      if (token) {
        request.headers.set('X-XSRF-TOKEN', token);
      }
    }
    return request;
  },
};

// URL absoluta: fetch no resuelve rutas relativas fuera del navegador (pruebas).
// fetch se resuelve en cada llamada para que MSW pueda interceptarlo en las pruebas.
export const api = createClient<paths>({
  baseUrl: new URL('/api/v1', window.location.origin).toString(),
  credentials: 'same-origin',
  fetch: (request) => globalThis.fetch(request),
});
api.use(csrf);
