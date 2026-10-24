import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { CalendarioPage } from './CalendarioPage';
import { HOY, SERVICIO_ID, guardias, server } from '../../test/server';

describe('CalendarioPage', () => {
  it('lista las guardias por día con médico, tipo y festivo', async () => {
    render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);

    const dia20 = await screen.findByRole('region', { name: /20 de enero/ });
    expect(within(dia20).getByText('Bruno Prueba — Presencia (festivo)')).toBeInTheDocument();
    expect(within(dia20).getByText('Rita Prueba — Residente')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /enero de 2030/ })).toBeInTheDocument();
  });

  it('pide al servidor el mes visible', async () => {
    const rangos: string[] = [];
    server.use(
      http.get('*/api/v1/servicios/:servicioId/guardias', ({ request }) => {
        const url = new URL(request.url);
        rangos.push(`${url.searchParams.get('desde')}..${url.searchParams.get('hasta')}`);
        return HttpResponse.json([]);
      }),
    );
    render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);
    await screen.findByText('No hay guardias este mes.');
    await userEvent.click(screen.getByRole('button', { name: 'Mes siguiente' }));
    await screen.findByRole('heading', { name: /febrero de 2030/ });

    expect(rangos).toEqual(['2030-01-01..2030-01-31', '2030-02-01..2030-02-28']);
  });

  it('al cambiar de mes no muestra las guardias del mes anterior mientras carga', async () => {
    let liberar: () => void = () => undefined;
    server.use(
      http.get('*/api/v1/servicios/:servicioId/guardias', async ({ request }) => {
        if (new URL(request.url).searchParams.get('desde') === '2030-02-01') {
          await new Promise<void>((resolver) => {
            liberar = resolver;
          });
          return HttpResponse.json([]);
        }
        return HttpResponse.json(guardias);
      }),
    );
    render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);
    await screen.findByText('Bruno Prueba — Presencia (festivo)');

    await userEvent.click(screen.getByRole('button', { name: 'Mes siguiente' }));
    expect(screen.getByRole('heading', { name: /febrero de 2030/ })).toBeInTheDocument();
    expect(screen.getByText('Cargando…')).toBeInTheDocument();
    expect(screen.queryByText('Bruno Prueba — Presencia (festivo)')).not.toBeInTheDocument();

    liberar();
    expect(await screen.findByText('No hay guardias este mes.')).toBeInTheDocument();
  });

  it('una respuesta tardía de otro mes no sustituye al mes visible', async () => {
    let liberarEnero: () => void = () => undefined;
    server.use(
      http.get('*/api/v1/servicios/:servicioId/guardias', async ({ request }) => {
        if (new URL(request.url).searchParams.get('desde') === '2030-01-01') {
          await new Promise<void>((resolver) => {
            liberarEnero = resolver;
          });
          return HttpResponse.json(guardias);
        }
        return HttpResponse.json([]);
      }),
    );
    render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);
    await userEvent.click(screen.getByRole('button', { name: 'Mes siguiente' }));
    await screen.findByText('No hay guardias este mes.');

    liberarEnero();
    await new Promise((resolver) => setTimeout(resolver, 50));
    expect(screen.queryByText('Bruno Prueba — Presencia (festivo)')).not.toBeInTheDocument();
    expect(screen.getByText('No hay guardias este mes.')).toBeInTheDocument();
  });

  it('solo las guardias futuras se pueden seleccionar y ofrecen Ceder e Intercambiar', async () => {
    render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);

    const dia10 = await screen.findByRole('region', { name: /10 de enero/ });
    expect(within(dia10).queryByRole('button')).not.toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: 'Bruno Prueba — Presencia (festivo)' }));
    const acciones = screen.getByRole('complementary', { name: 'Acciones de la guardia' });
    expect(within(acciones).getByRole('button', { name: 'Ceder' })).toBeInTheDocument();
    expect(within(acciones).getByRole('button', { name: 'Intercambiar' })).toBeInTheDocument();
  });

  it('muestra el error del servidor', async () => {
    server.use(
      http.get('*/api/v1/servicios/:servicioId/guardias', () =>
        HttpResponse.json(
          { type: 'about:blank', title: 'Prohibido', status: 403, detail: 'No administras este servicio.' },
          { status: 403 },
        ),
      ),
    );
    render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);
    expect(await screen.findByRole('alert')).toHaveTextContent('No administras este servicio.');
  });
});
