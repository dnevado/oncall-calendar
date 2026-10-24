import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { CalendarioPage } from './CalendarioPage';
import { HOY, SERVICIO_ID, cambioRegistrado, guardias, server } from '../../test/server';
import type { Guardia, ReglasIncumplidas } from '../../api/client';

async function abrirCesionDelDia20() {
  render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);
  await userEvent.click(await screen.findByRole('button', { name: 'Bruno Prueba — Presencia (festivo)' }));
  await userEvent.click(screen.getByRole('button', { name: 'Ceder' }));
  return screen.findByRole('form', { name: 'Ceder guardia' });
}

describe('CesionForm', () => {
  it('muestra los receptores candidatos', async () => {
    const formulario = await abrirCesionDelDia20();
    expect(await within(formulario).findByRole('radio', { name: 'Carla Prueba' })).toBeInTheDocument();
    expect(within(formulario).getByRole('radio', { name: 'David Prueba' })).toBeInTheDocument();
  });

  it('registra la cesión con Idempotency-Key y refresca el calendario', async () => {
    let peticion: { cuerpo: unknown; clave: string | null } | undefined;
    let calendario: Guardia[] = guardias;
    server.use(
      http.get('*/api/v1/servicios/:servicioId/guardias', () => HttpResponse.json(calendario)),
      http.post('*/api/v1/servicios/:servicioId/cambios', async ({ request }) => {
        peticion = { cuerpo: await request.json(), clave: request.headers.get('Idempotency-Key') };
        calendario = guardias.map((g) => (g.id === 20 ? { ...g, medico: { id: 4, nombre: 'Carla Prueba' } } : g));
        return HttpResponse.json(cambioRegistrado, { status: 201 });
      }),
    );

    const formulario = await abrirCesionDelDia20();
    await userEvent.click(await within(formulario).findByRole('radio', { name: 'Carla Prueba' }));
    await userEvent.click(within(formulario).getByRole('button', { name: 'Registrar cesión' }));

    expect(await screen.findByRole('status')).toHaveTextContent('Cesión registrada');
    expect(await screen.findByText('Carla Prueba — Presencia (festivo)')).toBeInTheDocument();
    expect(screen.queryByRole('form', { name: 'Ceder guardia' })).not.toBeInTheDocument();
    expect(peticion?.cuerpo).toEqual({ tipo: 'CESION', guardiaOrigenId: 20, cedenteId: 2, receptorId: 4 });
    expect(peticion?.clave).toMatch(/.{8,}/);
  });

  it('con un 422 lista todas las infracciones y no cierra el formulario', async () => {
    const problema: ReglasIncumplidas = {
      type: 'about:blank',
      title: 'Reglas incumplidas',
      status: 422,
      infracciones: [
        { regla: 'CG-011', mensaje: 'Carla Prueba ya tiene otra guardia el 20/01/2030.', medicoId: 4, fecha: '2030-01-20' },
        { regla: 'CG-012', mensaje: 'Carla Prueba está de vacaciones el 20/01/2030.', medicoId: 4, fecha: '2030-01-20' },
      ],
    };
    server.use(
      http.post('*/api/v1/servicios/:servicioId/cambios', () => HttpResponse.json(problema, { status: 422 })),
    );

    const formulario = await abrirCesionDelDia20();
    await userEvent.click(await within(formulario).findByRole('radio', { name: 'Carla Prueba' }));
    await userEvent.click(within(formulario).getByRole('button', { name: 'Registrar cesión' }));

    const alerta = await within(formulario).findByRole('alert');
    expect(within(alerta).getAllByRole('listitem')).toHaveLength(2);
    expect(alerta).toHaveTextContent('Carla Prueba ya tiene otra guardia el 20/01/2030.');
    expect(alerta).toHaveTextContent('Carla Prueba está de vacaciones el 20/01/2030.');
    expect(within(formulario).getByRole('button', { name: 'Registrar cesión' })).toBeEnabled();
  });

  it('deshabilita el botón mientras envía', async () => {
    let liberar: () => void = () => undefined;
    server.use(
      http.post('*/api/v1/servicios/:servicioId/cambios', async () => {
        await new Promise<void>((resolver) => {
          liberar = resolver;
        });
        return HttpResponse.json(cambioRegistrado, { status: 201 });
      }),
    );

    const formulario = await abrirCesionDelDia20();
    await userEvent.click(await within(formulario).findByRole('radio', { name: 'Carla Prueba' }));
    await userEvent.click(within(formulario).getByRole('button', { name: 'Registrar cesión' }));

    expect(within(formulario).getByRole('button', { name: 'Registrar cesión' })).toBeDisabled();
    liberar();
    expect(await screen.findByRole('status')).toHaveTextContent('Cesión registrada');
  });

  it('sin candidatos lo indica', async () => {
    server.use(
      http.get('*/api/v1/servicios/:servicioId/guardias/:guardiaId/receptores-candidatos', () => HttpResponse.json([])),
    );
    const formulario = await abrirCesionDelDia20();
    expect(await within(formulario).findByText(/Ningún médico cumple las reglas/)).toBeInTheDocument();
  });
});
