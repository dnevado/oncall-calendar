import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import type { Candidato, Cambio, Guardia, Problem, Usuario } from '../api/client';

// Datos ficticios con la forma de los esquemas del contrato (contracts/guardias-cambios.openapi.yaml).
export const HOY = '2030-01-15';
export const SERVICIO_ID = 7;

export const usuarioAdmin: Usuario = {
  id: 1,
  nombre: 'Ana Prueba',
  serviciosAdministrados: [{ id: SERVICIO_ID, nombre: 'Cardiología' }],
};

export const guardias: Guardia[] = [
  { id: 10, fecha: '2030-01-10', tipo: 'PRESENCIA', festivo: false, medico: { id: 2, nombre: 'Bruno Prueba' } },
  { id: 20, fecha: '2030-01-20', tipo: 'PRESENCIA', festivo: true, medico: { id: 2, nombre: 'Bruno Prueba' } },
  { id: 21, fecha: '2030-01-20', tipo: 'RESIDENTE', festivo: false, medico: { id: 3, nombre: 'Rita Prueba' } },
];

export const candidatos: Candidato[] = [
  { medico: { id: 4, nombre: 'Carla Prueba' } },
  { medico: { id: 5, nombre: 'David Prueba' } },
];

export const cambioRegistrado: Cambio = {
  id: 99,
  tipo: 'CESION',
  fechaOrigen: '2030-01-20',
  cedente: { id: 2, nombre: 'Bruno Prueba' },
  receptor: { id: 4, nombre: 'Carla Prueba' },
  guardiaOrigen: { tipo: 'PRESENCIA', festivo: true },
  registradoPor: { id: 1, nombre: 'Ana Prueba' },
  registradoEn: '2030-01-15T10:00:00Z',
};

export const noAutenticado: Problem = { type: 'about:blank', title: 'No autenticado', status: 401 };

export const handlers = [
  http.get('*/api/v1/auth/me', () => HttpResponse.json(noAutenticado, { status: 401 })),
  http.post('*/api/v1/auth/logout', () => new HttpResponse(null, { status: 204 })),
  http.post('*/api/v1/auth/login', async ({ request }) => {
    const cuerpo = (await request.json()) as { email: string; password: string };
    return cuerpo.password === 'secreto'
      ? HttpResponse.json(usuarioAdmin)
      : HttpResponse.json({ ...noAutenticado, detail: 'Correo o contraseña incorrectos.' }, { status: 401 });
  }),
  http.get('*/api/v1/servicios/:servicioId/guardias', () => HttpResponse.json(guardias)),
  http.get('*/api/v1/servicios/:servicioId/guardias/:guardiaId/receptores-candidatos', () =>
    HttpResponse.json(candidatos),
  ),
  http.post('*/api/v1/servicios/:servicioId/cambios', () => HttpResponse.json(cambioRegistrado, { status: 201 })),
];

export const server = setupServer(...handlers);

/** Sustituye /auth/me para simular una sesión ya iniciada. */
export function conSesion(usuario: Usuario = usuarioAdmin) {
  server.use(http.get('*/api/v1/auth/me', () => HttpResponse.json(usuario)));
}
