import { BrowserRouter, Navigate, NavLink, Route, Routes, useParams } from 'react-router-dom';
import { AuthProvider, useAuth } from './features/auth/AuthContext';
import { LoginPage } from './features/auth/LoginPage';
import { ServicioSelector } from './features/auth/ServicioSelector';
import { CalendarioPage } from './features/cambios/CalendarioPage';
import { CambiosListPage } from './features/cambios/CambiosListPage';
import { VacacionesPage } from './features/vacaciones/VacacionesPage';
import type { Usuario } from './api/client';

function PaginaDelServicio({
  usuario,
  pagina,
}: {
  usuario: Usuario;
  pagina: 'calendario' | 'cambios' | 'vacaciones';
}) {
  const servicioId = Number(useParams().servicioId);
  // Solo navegación: el servidor exige el rol en cada operación (FR-001).
  if (!usuario.serviciosAdministrados.some((s) => s.id === servicioId)) {
    return <Navigate to="/" replace />;
  }
  return (
    <>
      <nav aria-label="Secciones del servicio">
        <NavLink to={`/servicios/${servicioId}`} end>
          Calendario
        </NavLink>{' '}
        <NavLink to={`/servicios/${servicioId}/cambios`}>Cambios registrados</NavLink>{' '}
        <NavLink to={`/servicios/${servicioId}/vacaciones`}>Vacaciones</NavLink>
      </nav>
      {pagina === 'calendario' && <CalendarioPage servicioId={servicioId} />}
      {pagina === 'cambios' && <CambiosListPage servicioId={servicioId} />}
      {pagina === 'vacaciones' && <VacacionesPage servicioId={servicioId} />}
    </>
  );
}

function Inicio({ usuario }: { usuario: Usuario }) {
  const [unico] = usuario.serviciosAdministrados;
  if (unico && usuario.serviciosAdministrados.length === 1) {
    return <Navigate to={`/servicios/${unico.id}`} replace />;
  }
  return <ServicioSelector usuario={usuario} />;
}

export function Rutas() {
  const { estado, logout } = useAuth();
  if (estado.fase === 'cargando') return <p>Cargando…</p>;
  if (estado.fase === 'anonimo') return <LoginPage />;
  const { usuario } = estado;
  return (
    <>
      <header className="cabecera">
        <span>{usuario.nombre}</span>
        <button type="button" onClick={() => void logout()}>
          Cerrar sesión
        </button>
      </header>
      <Routes>
        <Route path="/" element={<Inicio usuario={usuario} />} />
        <Route
          path="/servicios/:servicioId"
          element={<PaginaDelServicio usuario={usuario} pagina="calendario" />}
        />
        <Route
          path="/servicios/:servicioId/cambios"
          element={<PaginaDelServicio usuario={usuario} pagina="cambios" />}
        />
        <Route
          path="/servicios/:servicioId/vacaciones"
          element={<PaginaDelServicio usuario={usuario} pagina="vacaciones" />}
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}

export function App() {
  return (
    <BrowserRouter basename="/app">
      <AuthProvider>
        <Rutas />
      </AuthProvider>
    </BrowserRouter>
  );
}
