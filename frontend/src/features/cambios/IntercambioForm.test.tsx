import { fireEvent, render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { CalendarioPage } from './CalendarioPage';
import { HOY, SERVICIO_ID, server } from '../../test/server';
import type { Cambio, Candidato, ReglasIncumplidas } from '../../api/client';

const candidatosIntercambio: Candidato[] = [
  { medico: { id: 4, nombre: 'Carla Prueba' }, guardiaDestinoId: 40 },
  { medico: { id: 5, nombre: 'David Prueba' }, guardiaDestinoId: 50 },
];

const intercambioRegistrado: Cambio = {
  id: 100,
  tipo: 'CAMBIO',
  fechaOrigen: '2030-01-20',
  fechaDestino: '2030-01-25',
  cedente: { id: 2, nombre: 'Bruno Prueba' },
  receptor: { id: 4, nombre: 'Carla Prueba' },
  guardiaOrigen: { tipo: 'PRESENCIA', festivo: true },
  guardiaDestino: { tipo: 'LOCALIZADA', festivo: false },
  registradoPor: { id: 1, nombre: 'Ana Prueba' },
  registradoEn: '2030-01-15T10:00:00Z',
};

async function abrirIntercambioDelDia20() {
  render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);
  await userEvent.click(
    await screen.findByRole('button', { name: 'Bruno Prueba — Presencia (festivo)' }),
  );
  await userEvent.click(screen.getByRole('button', { name: 'Intercambiar' }));
  return screen.findByRole('form', { name: 'Intercambiar guardia' });
}

function elegirFecha(formulario: HTMLElement, fecha: string) {
  fireEvent.change(within(formulario).getByLabelText(/fecha de la guardia que recibe/), {
    target: { value: fecha },
  });
}

describe('IntercambioForm', () => {
  it('pide los candidatos de la fecha destino', async () => {
    let consulta: URLSearchParams | undefined;
    server.use(
      http.get(
        '*/api/v1/servicios/:servicioId/guardias/:guardiaId/receptores-candidatos',
        ({ request }) => {
          consulta = new URL(request.url).searchParams;
          return HttpResponse.json(candidatosIntercambio);
        },
      ),
    );
    const formulario = await abrirIntercambioDelDia20();
    expect(
      within(formulario).getByRole('button', { name: 'Registrar intercambio' }),
    ).toBeDisabled();

    elegirFecha(formulario, '2030-01-25');
    expect(
      await within(formulario).findByRole('radio', { name: 'Carla Prueba' }),
    ).toBeInTheDocument();
    expect(within(formulario).getByRole('radio', { name: 'David Prueba' })).toBeInTheDocument();
    expect(consulta?.get('tipo')).toBe('CAMBIO');
    expect(consulta?.get('fechaDestino')).toBe('2030-01-25');
  });

  it('registra el intercambio con la guardia del candidato elegido', async () => {
    let peticion: { cuerpo: unknown; clave: string | null } | undefined;
    server.use(
      http.get('*/api/v1/servicios/:servicioId/guardias/:guardiaId/receptores-candidatos', () =>
        HttpResponse.json(candidatosIntercambio),
      ),
      http.post('*/api/v1/servicios/:servicioId/cambios', async ({ request }) => {
        peticion = { cuerpo: await request.json(), clave: request.headers.get('Idempotency-Key') };
        return HttpResponse.json(intercambioRegistrado, { status: 201 });
      }),
    );
    const formulario = await abrirIntercambioDelDia20();
    elegirFecha(formulario, '2030-01-25');
    await userEvent.click(await within(formulario).findByRole('radio', { name: 'Carla Prueba' }));
    await userEvent.click(
      within(formulario).getByRole('button', { name: 'Registrar intercambio' }),
    );

    expect(await screen.findByRole('status')).toHaveTextContent('Intercambio registrado');
    expect(screen.queryByRole('form', { name: 'Intercambiar guardia' })).not.toBeInTheDocument();
    expect(peticion?.cuerpo).toEqual({
      tipo: 'CAMBIO',
      guardiaOrigenId: 20,
      cedenteId: 2,
      receptorId: 4,
      guardiaDestinoId: 40,
    });
    expect(peticion?.clave).toMatch(/.{8,}/);
  });

  it('con un 422 lista todas las infracciones', async () => {
    const problema: ReglasIncumplidas = {
      type: 'about:blank',
      title: 'Reglas incumplidas',
      status: 422,
      infracciones: [
        { regla: 'CG-012', mensaje: 'Carla Prueba está de vacaciones el 20/01/2030.', medicoId: 4 },
        {
          regla: 'CG-013',
          mensaje: 'Bruno Prueba tiene guardia de presencia el 24/01/2030.',
          medicoId: 2,
        },
      ],
    };
    server.use(
      http.get('*/api/v1/servicios/:servicioId/guardias/:guardiaId/receptores-candidatos', () =>
        HttpResponse.json(candidatosIntercambio),
      ),
      http.post('*/api/v1/servicios/:servicioId/cambios', () =>
        HttpResponse.json(problema, { status: 422 }),
      ),
    );
    const formulario = await abrirIntercambioDelDia20();
    elegirFecha(formulario, '2030-01-25');
    await userEvent.click(await within(formulario).findByRole('radio', { name: 'Carla Prueba' }));
    await userEvent.click(
      within(formulario).getByRole('button', { name: 'Registrar intercambio' }),
    );

    const alerta = await within(formulario).findByRole('alert');
    expect(within(alerta).getAllByRole('listitem')).toHaveLength(2);
    expect(alerta).toHaveTextContent('No se ha registrado el intercambio');
  });

  it('sin candidatos ese día lo indica', async () => {
    server.use(
      http.get('*/api/v1/servicios/:servicioId/guardias/:guardiaId/receptores-candidatos', () =>
        HttpResponse.json([]),
      ),
    );
    const formulario = await abrirIntercambioDelDia20();
    elegirFecha(formulario, '2030-01-25');
    expect(
      await within(formulario).findByText(/Ningún médico con guardia ese día/),
    ).toBeInTheDocument();
  });
});
