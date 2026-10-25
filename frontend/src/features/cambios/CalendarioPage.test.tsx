import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { CalendarioPage } from './CalendarioPage';
import { HOY, SERVICIO_ID, guardias, server } from '../../test/server';
import type { Guardia } from '../../api/client';

/** Registra los rangos pedidos al servidor y responde con `respuesta`. */
function registrarRangos(respuesta: Guardia[] = []): string[] {
  const rangos: string[] = [];
  server.use(
    http.get('*/api/v1/servicios/:servicioId/guardias', ({ request }) => {
      const url = new URL(request.url);
      rangos.push(`${url.searchParams.get('desde')}..${url.searchParams.get('hasta')}`);
      return HttpResponse.json(respuesta);
    }),
  );
  return rangos;
}

function titulo() {
  const elemento = document.querySelector<HTMLElement>('.fc-toolbar-title');
  if (!elemento) throw new Error('No se encuentra el título del calendario');
  return elemento;
}

describe('CalendarioPage', () => {
  it('muestra las guardias del mes de hoy con médico, tipo y festivo', async () => {
    const { container } = render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);

    const presencia = await screen.findByRole('button', {
      name: 'Bruno Prueba — Presencia (festivo)',
    });
    expect(presencia).toHaveClass('guardia--presencia', 'guardia--festivo');
    expect(screen.getByRole('button', { name: 'Rita Prueba — Residente' })).toHaveClass(
      'guardia--residente',
    );
    expect(titulo()).toHaveTextContent('enero de 2030');

    const dia20 = container.querySelector<HTMLElement>('[data-date="2030-01-20"]');
    if (!dia20) throw new Error('No se encuentra el día 20');
    expect(within(dia20).getByText('Bruno Prueba — Presencia (festivo)')).toBeInTheDocument();
  });

  it('ofrece las vistas de año, mes, semana y día, y pide al servidor el periodo visible', async () => {
    const rangos = registrarRangos();
    render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);
    await screen.findByText('No hay guardias en este periodo.');

    await userEvent.click(screen.getByRole('button', { name: 'Semana' }));
    await screen.findByText('No hay guardias en este periodo.');
    await userEvent.click(screen.getByRole('button', { name: 'Día' }));
    await screen.findByText('No hay guardias en este periodo.');
    expect(titulo()).toHaveTextContent('15 de enero de 2030');
    await userEvent.click(screen.getByRole('button', { name: 'Año' }));
    await screen.findByText('No hay guardias en este periodo.');
    expect(titulo()).toHaveTextContent('2030');

    expect(rangos).toEqual([
      '2029-12-31..2030-02-03', // mes, semanas completas de lunes a domingo
      '2030-01-14..2030-01-20',
      '2030-01-15..2030-01-15',
      '2030-01-01..2030-12-31',
    ]);
  });

  it('navega al periodo anterior y siguiente, y vuelve a hoy', async () => {
    const rangos = registrarRangos();
    render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);
    await screen.findByText('No hay guardias en este periodo.');

    await userEvent.click(screen.getByRole('button', { name: 'Mes siguiente' }));
    expect(titulo()).toHaveTextContent('febrero de 2030');
    await userEvent.click(screen.getByRole('button', { name: 'Mes anterior' }));
    await userEvent.click(screen.getByRole('button', { name: 'Mes anterior' }));
    expect(titulo()).toHaveTextContent('diciembre de 2029');
    await userEvent.click(screen.getByRole('button', { name: 'Hoy' }));
    expect(titulo()).toHaveTextContent('enero de 2030');

    expect(rangos[1]).toBe('2030-01-28..2030-03-03');
  });

  it('desde un día se va a su vista de día', async () => {
    render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);
    await screen.findByRole('button', { name: 'Rita Prueba — Residente' });

    await userEvent.click(screen.getByTitle('Ir al 20 de enero de 2030'));
    expect(titulo()).toHaveTextContent('20 de enero de 2030');
    expect(
      await screen.findByRole('button', { name: 'Rita Prueba — Residente' }),
    ).toBeInTheDocument();
    expect(screen.queryByText(/Bruno Prueba — Presencia$/)).not.toBeInTheDocument();
  });

  it('ordena las guardias del día como el legacy: presencia, residente, localizada y refuerzo', async () => {
    const delDia = (id: number, tipo: Guardia['tipo'], nombre: string): Guardia => ({
      id,
      fecha: '2030-01-22',
      tipo,
      festivo: false,
      medico: { id, nombre },
    });
    registrarRangos([
      delDia(1, 'REFUERZO', 'Ref'),
      delDia(2, 'LOCALIZADA', 'Loc'),
      delDia(3, 'RESIDENTE', 'Res'),
      delDia(4, 'PRESENCIA', 'Pre'),
    ]);
    const { container } = render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);
    await screen.findByRole('button', { name: /^Pre/ });

    // La vista de día muestra todas, sin el enlace «más».
    await userEvent.click(screen.getByTitle('Ir al 22 de enero de 2030'));
    await screen.findByRole('button', { name: /^Pre/ });
    const nombres = [...container.querySelectorAll('.fc-event-title')].map(
      (e) => e.textContent?.split(' — ')[0],
    );
    expect(nombres).toEqual(['Pre', 'Res', 'Ref', 'Loc']);
  });

  it('muestra la leyenda de tipos y festivo', async () => {
    render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);
    const leyenda = screen.getByRole('list', { name: 'Leyenda' });
    for (const nombre of ['Presencia', 'Localizada', 'Refuerzo', 'Residente', 'Festivo']) {
      expect(within(leyenda).getByText(nombre)).toBeInTheDocument();
    }
    await screen.findByRole('button', { name: 'Rita Prueba — Residente' });
  });

  it('al cambiar de periodo no muestra las guardias del anterior mientras carga', async () => {
    let liberar: () => void = () => undefined;
    server.use(
      http.get('*/api/v1/servicios/:servicioId/guardias', async ({ request }) => {
        if (new URL(request.url).searchParams.get('desde') === '2030-01-28') {
          await new Promise<void>((resolver) => {
            liberar = resolver;
          });
          return HttpResponse.json([]);
        }
        return HttpResponse.json(guardias);
      }),
    );
    render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);
    await screen.findByRole('button', { name: 'Bruno Prueba — Presencia (festivo)' });

    await userEvent.click(screen.getByRole('button', { name: 'Mes siguiente' }));
    expect(titulo()).toHaveTextContent('febrero de 2030');
    expect(screen.getByText('Cargando…')).toBeInTheDocument();
    expect(screen.queryByText('Bruno Prueba — Presencia (festivo)')).not.toBeInTheDocument();

    liberar();
    expect(await screen.findByText('No hay guardias en este periodo.')).toBeInTheDocument();
  });

  it('una respuesta tardía de otro periodo no sustituye al visible', async () => {
    let liberarEnero: () => void = () => undefined;
    server.use(
      http.get('*/api/v1/servicios/:servicioId/guardias', async ({ request }) => {
        if (new URL(request.url).searchParams.get('desde') === '2029-12-31') {
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
    await screen.findByText('No hay guardias en este periodo.');

    liberarEnero();
    await new Promise((resolver) => setTimeout(resolver, 50));
    expect(screen.queryByText('Bruno Prueba — Presencia (festivo)')).not.toBeInTheDocument();
    expect(screen.getByText('No hay guardias en este periodo.')).toBeInTheDocument();
  });

  it('solo las guardias futuras se pueden elegir o arrastrar; elegir una abre el popup', async () => {
    render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);
    const futura = await screen.findByRole('button', {
      name: 'Bruno Prueba — Presencia (festivo)',
    });
    expect(futura).toHaveClass('fc-event-draggable');

    const pasada = screen.getByText('Bruno Prueba — Presencia').closest('.fc-event');
    expect(pasada).toHaveClass('guardia--pasada');
    expect(pasada).not.toHaveClass('fc-event-draggable');
    expect(pasada).not.toHaveAttribute('role', 'button');
    await userEvent.click(pasada as HTMLElement);
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();

    await userEvent.click(futura);
    const popup = screen.getByRole('dialog', { name: 'Cambio de guardia' });
    expect(within(popup).getByText(/Guardia de Bruno Prueba/)).toBeInTheDocument();
    expect(within(popup).getByRole('button', { name: 'Ceder' })).toHaveFocus();
    expect(within(popup).getByRole('button', { name: 'Intercambiar' })).toBeInTheDocument();
  });

  it('el intercambio del popup muestra inicio y fin', async () => {
    render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);
    await userEvent.click(
      await screen.findByRole('button', { name: 'Bruno Prueba — Presencia (festivo)' }),
    );
    await userEvent.click(screen.getByRole('button', { name: 'Intercambiar' }));

    const formulario = screen.getByRole('form', { name: 'Intercambiar guardia' });
    expect(within(formulario).getByText(/Inicio:/).parentElement).toHaveTextContent(
      'Inicio: domingo, 20 de enero (Presencia (festivo))',
    );
    expect(within(formulario).getByLabelText(/Fin: fecha de la guardia que recibe/)).toHaveValue(
      '',
    );
  });

  it('el popup se cierra con Escape o Cancelar y devuelve el foco a la guardia', async () => {
    render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);
    const guardia = await screen.findByRole('button', { name: 'Rita Prueba — Residente' });

    guardia.focus();
    await userEvent.keyboard('{Enter}');
    expect(screen.getByRole('dialog', { name: 'Cambio de guardia' })).toHaveTextContent(
      'Guardia de Rita Prueba',
    );
    await userEvent.keyboard('{Escape}');
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(guardia).toHaveFocus();

    await userEvent.click(guardia);
    await userEvent.click(screen.getByRole('button', { name: 'Ceder' }));
    await userEvent.click(
      within(await screen.findByRole('form', { name: 'Ceder guardia' })).getByRole('button', {
        name: 'Cancelar',
      }),
    );
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('muestra el error del servidor', async () => {
    server.use(
      http.get('*/api/v1/servicios/:servicioId/guardias', () =>
        HttpResponse.json(
          {
            type: 'about:blank',
            title: 'Prohibido',
            status: 403,
            detail: 'No administras este servicio.',
          },
          { status: 403 },
        ),
      ),
    );
    render(<CalendarioPage servicioId={SERVICIO_ID} hoy={HOY} />);
    expect(await screen.findByRole('alert')).toHaveTextContent('No administras este servicio.');
  });
});
