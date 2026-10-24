import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { http, HttpResponse } from 'msw';
import { AuthProvider } from './AuthContext';
import { Rutas } from '../../App';
import { conSesion, server, usuarioAdmin } from '../../test/server';

function renderApp() {
  return render(
    <MemoryRouter>
      <AuthProvider>
        <Rutas />
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('LoginPage', () => {
  it('sin sesión muestra el formulario y entra con credenciales correctas', async () => {
    renderApp();
    await userEvent.type(await screen.findByLabelText('Correo electrónico'), 'ana@ejemplo.test');
    await userEvent.type(screen.getByLabelText('Contraseña'), 'secreto');
    await userEvent.click(screen.getByRole('button', { name: 'Entrar' }));

    expect(await screen.findByText('Ana Prueba')).toBeInTheDocument();
    expect(screen.queryByLabelText('Contraseña')).not.toBeInTheDocument();
  });

  it('con credenciales incorrectas muestra el error del servidor', async () => {
    renderApp();
    await userEvent.type(await screen.findByLabelText('Correo electrónico'), 'ana@ejemplo.test');
    await userEvent.type(screen.getByLabelText('Contraseña'), 'mala');
    await userEvent.click(screen.getByRole('button', { name: 'Entrar' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Correo o contraseña incorrectos.');
  });

  it('envía la cabecera CSRF tomada de la cookie', async () => {
    document.cookie = 'XSRF-TOKEN=token-de-prueba; path=/';
    let cabecera: string | null = null;
    server.use(
      http.post('*/api/v1/auth/login', ({ request }) => {
        cabecera = request.headers.get('X-XSRF-TOKEN');
        return HttpResponse.json(usuarioAdmin);
      }),
    );
    renderApp();
    await userEvent.type(await screen.findByLabelText('Correo electrónico'), 'ana@ejemplo.test');
    await userEvent.type(screen.getByLabelText('Contraseña'), 'secreto');
    await userEvent.click(screen.getByRole('button', { name: 'Entrar' }));

    await screen.findByText('Ana Prueba');
    expect(cabecera).toBe('token-de-prueba');
  });

  it('con varios servicios pide elegir uno', async () => {
    conSesion({
      ...usuarioAdmin,
      serviciosAdministrados: [
        { id: 7, nombre: 'Cardiología' },
        { id: 8, nombre: 'Neumología' },
      ],
    });
    renderApp();
    expect(await screen.findByRole('link', { name: 'Neumología' })).toBeInTheDocument();
  });

  it('sin servicios administrados lo indica', async () => {
    conSesion({ ...usuarioAdmin, serviciosAdministrados: [] });
    renderApp();
    expect(await screen.findByRole('alert')).toHaveTextContent('No administras ningún servicio');
  });
});
