import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { EditorVacaciones } from './EditorVacaciones';
import { HOY, SERVICIO_ID, server, vacaciones } from '../../test/server';
import type { ReglasIncumplidas, VacacionesMedico } from '../../api/client';

const BRUNO = { id: 2, nombre: 'Bruno Prueba' };

function abrirEditor() {
  render(<EditorVacaciones servicioId={SERVICIO_ID} medico={BRUNO} hoy={HOY} />);
}

/** Botón de un día por su fecha («20 de enero de 2030»), sea cual sea su estado. */
function dia(fecha: string) {
  return screen.getByRole('button', { name: new RegExp(`\\b${fecha}\\b`) });
}

describe('EditorVacaciones', () => {
  it('muestra el calendario en español, empezando en lunes', async () => {
    abrirEditor();
    // Las cabeceras están ocultas al lector de pantalla: el nombre de cada día ya dice cuál es.
    const [rejilla] = await screen.findAllByRole('grid');
    if (!rejilla) throw new Error('Sin calendario');
    const cabeceras = within(rejilla)
      .getAllByRole('columnheader', { hidden: true })
      .map((c) => c.getAttribute('aria-label'));
    expect(cabeceras[0]).toBe('lunes');
    expect(cabeceras[6]).toBe('domingo');
    expect(screen.getByText(/enero 2030/i)).toBeInTheDocument();
  });

  it('marca las vacaciones guardadas sin depender solo del color', async () => {
    abrirEditor();
    expect(
      await screen.findByRole('button', { name: /^lunes, 4 de febrero de 2030.*de vacaciones/ }),
    ).toBeInTheDocument();
    expect(dia('20 de enero de 2030')).not.toHaveAccessibleName(/vacaciones/);
  });

  it('no deja marcar días pasados', async () => {
    abrirEditor();
    await screen.findByRole('button', { name: /^lunes, 4 de febrero de 2030/ });
    const pasado = dia('10 de enero de 2030');
    expect(pasado).toBeDisabled();
    expect(pasado).toHaveAccessibleName(/no editable/);
    expect(dia('15 de enero de 2030')).toBeEnabled();
  });

  it('marca varios días con ratón y teclado y solo envía los nuevos', async () => {
    let cuerpo: unknown;
    const bruno = vacaciones.find((v) => v.medico.id === BRUNO.id);
    if (!bruno) throw new Error('Sin datos de Bruno');
    let datos: VacacionesMedico[] = [bruno];
    server.use(
      http.get('*/api/v1/servicios/:servicioId/vacaciones', () => HttpResponse.json(datos)),
      http.patch('*/api/v1/servicios/:s/medicos/:m/vacaciones', async ({ request }) => {
        cuerpo = await request.json();
        datos = [
          { ...bruno, dias: [...bruno.dias, { fecha: '2030-01-20' }, { fecha: '2030-01-21' }] },
        ];
        return HttpResponse.json({ anadidos: ['2030-01-20', '2030-01-21'], quitados: [] });
      }),
    );
    const usuario = userEvent.setup();
    abrirEditor();
    await screen.findByRole('button', { name: /^lunes, 4 de febrero de 2030/ });

    await usuario.click(dia('20 de enero de 2030'));
    expect(dia('20 de enero de 2030')).toHaveAccessibleName(/se añadirá/);
    expect(dia('20 de enero de 2030')).toHaveFocus();
    await usuario.keyboard('{ArrowRight}');
    expect(dia('21 de enero de 2030')).toHaveFocus();
    await usuario.keyboard('{Enter}');
    expect(dia('21 de enero de 2030')).toHaveAccessibleName(/se añadirá/);

    expect(screen.getByText('Sin guardar: añadir 2 días.')).toBeInTheDocument();
    await usuario.click(screen.getByRole('button', { name: 'Guardar vacaciones' }));

    // El selector tiene su propia región de estado; se busca la confirmación por su texto.
    expect(await screen.findByText('Vacaciones guardadas: 2 días añadidos.')).toHaveAttribute(
      'role',
      'status',
    );
    expect(cuerpo).toEqual({ anadir: ['2030-01-20', '2030-01-21'], quitar: [] });
    expect(
      await screen.findByRole('button', { name: /20 de enero de 2030.*de vacaciones/ }),
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Guardar vacaciones' })).toBeDisabled();
  });

  it('desmarcar un día nuevo antes de guardar lo deja como estaba', async () => {
    const usuario = userEvent.setup();
    abrirEditor();
    await screen.findByRole('button', { name: /^lunes, 4 de febrero de 2030/ });

    await usuario.click(dia('20 de enero de 2030'));
    await usuario.click(dia('20 de enero de 2030'));

    expect(dia('20 de enero de 2030')).not.toHaveAccessibleName(/se añadirá/);
    expect(screen.getByRole('button', { name: 'Guardar vacaciones' })).toBeDisabled();
  });

  it('muestra cada día rechazado con su motivo', async () => {
    const rechazo: ReglasIncumplidas = {
      type: 'about:blank',
      title: 'Reglas incumplidas',
      status: 422,
      detail: 'No se han guardado las vacaciones porque incumplen las reglas indicadas.',
      infracciones: [
        {
          regla: 'VAC-003',
          mensaje: 'Bruno Prueba tiene guardia de presencia el 20/01/2030 en este servicio.',
          medicoId: 2,
          fecha: '2030-01-20',
        },
        {
          regla: 'VAC-003',
          mensaje: 'Bruno Prueba tiene guardia de presencia el 21/01/2030 en este servicio.',
          medicoId: 2,
          fecha: '2030-01-21',
        },
      ],
    };
    server.use(
      http.patch('*/api/v1/servicios/:s/medicos/:m/vacaciones', () =>
        HttpResponse.json(rechazo, { status: 422 }),
      ),
    );
    const usuario = userEvent.setup();
    abrirEditor();
    await screen.findByRole('button', { name: /^lunes, 4 de febrero de 2030/ });
    await usuario.click(dia('20 de enero de 2030'));
    await usuario.click(dia('21 de enero de 2030'));
    await usuario.click(screen.getByRole('button', { name: 'Guardar vacaciones' }));

    const alerta = await screen.findByRole('alert');
    expect(alerta).toHaveTextContent('No se han guardado las vacaciones');
    expect(within(alerta).getAllByRole('listitem')).toHaveLength(2);
    expect(alerta).toHaveTextContent('20/01/2030');
    // Los días siguen marcados para corregirlos.
    expect(dia('20 de enero de 2030')).toHaveAccessibleName(/se añadirá/);
  });
});
