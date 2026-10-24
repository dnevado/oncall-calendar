import { Link } from 'react-router-dom';
import type { Usuario } from '../../api/client';

/** Elige el servicio a gestionar cuando el usuario administra varios. */
export function ServicioSelector({ usuario }: { usuario: Usuario }) {
  if (usuario.serviciosAdministrados.length === 0) {
    return <p role="alert">No administras ningún servicio. Solo los administradores pueden registrar cambios.</p>;
  }
  return (
    <nav aria-label="Servicios">
      <h2>Elige un servicio</h2>
      <ul>
        {usuario.serviciosAdministrados.map((s) => (
          <li key={s.id}>
            <Link to={`/servicios/${s.id}`}>{s.nombre}</Link>
          </li>
        ))}
      </ul>
    </nav>
  );
}
