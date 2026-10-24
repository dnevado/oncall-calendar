import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { CalendarioPage } from './CalendarioPage';
import { HOY, SERVICIO_ID, server } from '../../test/server';

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
