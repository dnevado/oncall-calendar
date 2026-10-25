import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { CambiosListPage } from './CambiosListPage';
import { HOY, SERVICIO_ID, server } from '../../test/server';
import type { Cambio, Candidato, Guardia } from '../../api/client';

const ANA = { id: 2, nombre: 'Ana Prueba' };
const BRUNO = { id: 3, nombre: 'Bruno Prueba' };
const ADMIN = { id: 1, nombre: 'Admin Prueba' };

const cesion: Cambio = {
  id: 7,
  tipo: 'CESION',
  fechaOrigen: '2030-01-20',
  cedente: ANA,
  receptor: BRUNO,
  guardiaOrigen: { tipo: 'PRESENCIA', festivo: false },
  registradoPor: ADMIN,
  registradoEn: '2030-01-14T09:30:00Z',
};

const intercambio: Cambio = {
  id: 8,
  tipo: 'CAMBIO',
  fechaOrigen: '2030-01-22',
  fechaDestino: '2030-01-25',
  cedente: ANA,
  receptor: BRUNO,
  guardiaOrigen: { tipo: 'LOCALIZADA', festivo: false },
  guardiaDestino: { tipo: 'PRESENCIA', festivo: true },
  registradoPor: ADMIN,
  registradoEn: '2030-01-14T10:00:00Z',
};

const pasado: Cambio = { ...cesion, id: 9, fechaOrigen: '2030-01-10' };

// Calendario actual: tras los cambios, el día 20 y el 22 son de Bruno y el 25 de Ana.
const guardiasActuales: Guardia[] = [
  { id: 20, fecha: '2030-01-20', tipo: 'PRESENCIA', festivo: false, medico: BRUNO },
  { id: 22, fecha: '2030-01-22', tipo: 'LOCALIZADA', festivo: false, medico: BRUNO },
  { id: 25, fecha: '2030-01-25', tipo: 'PRESENCIA', festivo: true, medico: ANA },
];

function conCambios(
  cambios: Cambio[],
  candidatos: Candidato[] = [{ medico: ANA, guardiaDestinoId: 25 }],
) {
  server.use(
    http.get('*/api/v1/servicios/:servicioId/cambios', () => HttpResponse.json(cambios)),
    http.get('*/api/v1/servicios/:servicioId/guardias', ({ request }) => {
      const desde = new URL(request.url).searchParams.get('desde');
      return HttpResponse.json(guardiasActuales.filter((g) => g.fecha === desde));
    }),
    http.get('*/api/v1/servicios/:servicioId/guardias/:guardiaId/receptores-candidatos', () =>
      HttpResponse.json(candidatos),
    ),
  );
}

describe('CambiosListPage', () => {
  it('lista los cambios del mes con quién y cuándo los registró', async () => {
    conCambios([intercambio, cesion]);
    render(<CambiosListPage servicioId={SERVICIO_ID} hoy={HOY} />);

    const items = await screen.findAllByRole('listitem');
    expect(items).toHaveLength(2);
    expect(items[0]).toHaveTextContent('Intercambio: Ana Prueba');
    expect(items[1]).toHaveTextContent('Cesión del');
    expect(items[1]).toHaveTextContent('de Ana Prueba a Bruno Prueba');
    expect(items[1]).toHaveTextContent('Registrado por Admin Prueba');
  });

  it('el inverso de una cesión cede la guardia de vuelta al cedente original', async () => {
    let cuerpo: unknown;
    conCambios([cesion]);
    server.use(
      http.post('*/api/v1/servicios/:servicioId/cambios', async ({ request }) => {
        cuerpo = await request.json();
        return HttpResponse.json(
          { ...cesion, id: 10, cedente: BRUNO, receptor: ANA },
          { status: 201 },
        );
      }),
    );
    render(<CambiosListPage servicioId={SERVICIO_ID} hoy={HOY} />);

    await userEvent.click(await screen.findByRole('button', { name: /Registrar cambio inverso/ }));
    const formulario = await screen.findByRole('form', { name: 'Ceder guardia' });
    expect(formulario).toHaveTextContent('Bruno Prueba cede su guardia');
    expect(await within(formulario).findByRole('radio', { name: 'Ana Prueba' })).toBeChecked();

    await userEvent.click(within(formulario).getByRole('button', { name: 'Registrar cesión' }));
    expect(await screen.findByRole('status')).toHaveTextContent('Cambio inverso registrado');
    expect(cuerpo).toEqual({
      tipo: 'CESION',
      guardiaOrigenId: 20,
      cedenteId: BRUNO.id,
      receptorId: ANA.id,
    });
  });

  it('el inverso de un intercambio viene con la fecha y el médico elegidos', async () => {
    let cuerpo: unknown;
    conCambios([intercambio]);
    server.use(
      http.post('*/api/v1/servicios/:servicioId/cambios', async ({ request }) => {
        cuerpo = await request.json();
        return HttpResponse.json({ ...intercambio, id: 11 }, { status: 201 });
      }),
    );
    render(<CambiosListPage servicioId={SERVICIO_ID} hoy={HOY} />);

    await userEvent.click(await screen.findByRole('button', { name: /Registrar cambio inverso/ }));
    const formulario = await screen.findByRole('form', { name: 'Intercambiar guardia' });
    expect(within(formulario).getByLabelText(/fecha de la guardia que recibe/)).toHaveValue(
      '2030-01-25',
    );
    expect(await within(formulario).findByRole('radio', { name: 'Ana Prueba' })).toBeChecked();

    await userEvent.click(
      within(formulario).getByRole('button', { name: 'Registrar intercambio' }),
    );
    expect(await screen.findByRole('status')).toHaveTextContent('Cambio inverso registrado');
    expect(cuerpo).toEqual({
      tipo: 'CAMBIO',
      guardiaOrigenId: 22,
      cedenteId: BRUNO.id,
      receptorId: ANA.id,
      guardiaDestinoId: 25,
    });
  });

  it('si el cedente original ya no cumple las reglas, lo indica y no lo elige', async () => {
    conCambios([cesion]);
    server.use(
      http.get('*/api/v1/servicios/:servicioId/guardias/:guardiaId/receptores-candidatos', () =>
        HttpResponse.json([{ medico: { id: 9, nombre: 'Otro Prueba' } }]),
      ),
    );
    render(<CambiosListPage servicioId={SERVICIO_ID} hoy={HOY} />);

    await userEvent.click(await screen.findByRole('button', { name: /Registrar cambio inverso/ }));
    const formulario = await screen.findByRole('form', { name: 'Ceder guardia' });
    expect(
      await within(formulario).findByText(
        'Ana Prueba no cumple ahora las reglas para recibir esta guardia.',
      ),
    ).toBeInTheDocument();
    expect(within(formulario).getByRole('button', { name: 'Registrar cesión' })).toBeDisabled();
  });

  it('un cambio con fechas ya pasadas no se puede corregir', async () => {
    conCambios([pasado]);
    render(<CambiosListPage servicioId={SERVICIO_ID} hoy={HOY} />);

    expect(await screen.findByRole('button', { name: /Registrar cambio inverso/ })).toBeDisabled();
    expect(screen.getByText(/alguna de sus fechas ya no es futura/)).toBeInTheDocument();
  });

  it('si la guardia ya no es de quien la recibió, avisa', async () => {
    conCambios([{ ...cesion, receptor: { id: 99, nombre: 'Nadie Prueba' } }]);
    render(<CambiosListPage servicioId={SERVICIO_ID} hoy={HOY} />);

    await userEvent.click(await screen.findByRole('button', { name: /Registrar cambio inverso/ }));
    expect(await screen.findByRole('alert')).toHaveTextContent('ya no es de Nadie Prueba');
    expect(screen.queryByRole('form')).not.toBeInTheDocument();
  });
});
