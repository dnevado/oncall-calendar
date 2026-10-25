import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { VacacionesPage } from './VacacionesPage';
import { HOY, SERVICIO_ID, server, vacaciones } from '../../test/server';

function abrirPagina() {
  render(<VacacionesPage servicioId={SERVICIO_ID} hoy={HOY} />);
}

async function filaDe(nombre: string) {
  const tabla = await screen.findByRole('table', { name: /Vacaciones del servicio/ });
  const celda = within(tabla).getByRole('rowheader', { name: new RegExp(nombre) });
  const fila = celda.closest('tr');
  if (!fila) throw new Error(`Sin fila para ${nombre}`);
  return fila;
}

describe('ResumenVacaciones', () => {
  it('pide por defecto de hoy a seis meses', async () => {
    const consultas: string[] = [];
    server.use(
      http.get('*/api/v1/servicios/:servicioId/vacaciones', ({ request }) => {
        const p = new URL(request.url).searchParams;
        consultas.push(`${p.get('desde')}..${p.get('hasta')}`);
        return HttpResponse.json(vacaciones);
      }),
    );
    abrirPagina();
    await screen.findByRole('table', { name: /Vacaciones del servicio/ });
    expect(consultas).toContain('2030-01-15..2030-07-15');
  });

  it('muestra todos los médicos con sus días agrupados por mes', async () => {
    abrirPagina();

    expect(await filaDe('Ana Prueba')).toHaveTextContent('Sin vacaciones en el periodo');
    const bruno = await filaDe('Bruno Prueba');
    expect(bruno).toHaveTextContent('febrero de 2030: 4 y 5');
    expect(bruno).toHaveTextContent('2 días');
    const carla = await filaDe('Carla Prueba');
    expect(carla).toHaveTextContent('Inactivo en el servicio');
    expect(carla).toHaveTextContent('marzo de 2030: 2');
  });

  it('señala los conflictos con texto y la guardia, en el periodo en que están', async () => {
    const usuario = userEvent.setup();
    abrirPagina();
    expect(await filaDe('Bruno Prueba')).not.toHaveTextContent('Conflicto');

    await usuario.click(screen.getByRole('button', { name: '6 meses anteriores' }));
    const bruno = await filaDe('Bruno Prueba');
    expect(await within(bruno).findByText(/Conflicto/)).toBeInTheDocument();
    expect(bruno).toHaveTextContent('Conflicto: 10/01/2030, guardia de presencia');
    expect(screen.getByRole('table', { name: /Vacaciones del servicio/ })).toHaveAccessibleName(
      /15 de julio de 2029/,
    );

    await usuario.click(screen.getByRole('button', { name: 'Volver a hoy' }));
    expect(
      await screen.findByRole('table', { name: /15 de enero de 2030 al 15 de julio de 2030/ }),
    ).toBeInTheDocument();
  });

  it('elegir un médico abre su editor', async () => {
    const usuario = userEvent.setup();
    abrirPagina();
    const bruno = await filaDe('Bruno Prueba');

    await usuario.click(
      within(bruno).getByRole('button', { name: 'Editar vacaciones de Bruno Prueba' }),
    );

    expect(
      await screen.findByRole('heading', { name: 'Vacaciones de Bruno Prueba' }),
    ).toBeInTheDocument();
  });

  it('el editor también señala los días en conflicto', async () => {
    const usuario = userEvent.setup();
    abrirPagina();
    const bruno = await filaDe('Bruno Prueba');
    await usuario.click(
      within(bruno).getByRole('button', { name: 'Editar vacaciones de Bruno Prueba' }),
    );

    expect(
      await screen.findByRole('button', {
        name: /10 de enero de 2030, de vacaciones, conflicto con guardia de presencia/,
      }),
    ).toBeInTheDocument();
  });

  it('tras guardar en el editor, el resumen se actualiza', async () => {
    let datos = vacaciones;
    server.use(
      http.get('*/api/v1/servicios/:servicioId/vacaciones', ({ request }) => {
        const medicoId = new URL(request.url).searchParams.get('medicoId');
        return HttpResponse.json(
          medicoId ? datos.filter((v) => v.medico.id === Number(medicoId)) : datos,
        );
      }),
      http.patch('*/api/v1/servicios/:s/medicos/:m/vacaciones', () => {
        datos = vacaciones.map((v) =>
          v.medico.id === 1 ? { ...v, dias: [{ fecha: '2030-01-20' }] } : v,
        );
        return HttpResponse.json({ anadidos: ['2030-01-20'], quitados: [] });
      }),
    );
    const usuario = userEvent.setup();
    abrirPagina();
    const ana = await filaDe('Ana Prueba');
    await usuario.click(
      within(ana).getByRole('button', { name: 'Editar vacaciones de Ana Prueba' }),
    );
    await usuario.click(
      await screen.findByRole('button', { name: /^domingo, 20 de enero de 2030/ }),
    );
    await usuario.click(screen.getByRole('button', { name: 'Guardar vacaciones' }));

    await screen.findByText('Vacaciones guardadas: 1 día añadido.');
    expect(await filaDe('Ana Prueba')).toHaveTextContent('enero de 2030: 20');
  });
});
