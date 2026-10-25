import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { api, type Usuario } from '../../api/client';

type Estado =
  { fase: 'cargando' } | { fase: 'anonimo' } | { fase: 'autenticado'; usuario: Usuario };

interface Auth {
  estado: Estado;
  /** Devuelve el mensaje de error si el login falla. */
  login: (email: string, password: string) => Promise<string | undefined>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<Auth | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [estado, setEstado] = useState<Estado>({ fase: 'cargando' });

  useEffect(() => {
    let vigente = true;
    // También obtiene la cookie XSRF-TOKEN necesaria para el login.
    api
      .GET('/auth/me')
      .then(({ data }) => {
        if (vigente) setEstado(data ? { fase: 'autenticado', usuario: data } : { fase: 'anonimo' });
      })
      .catch(() => {
        if (vigente) setEstado({ fase: 'anonimo' });
      });
    return () => {
      vigente = false;
    };
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const { data, error } = await api.POST('/auth/login', { body: { email, password } });
    if (data) {
      setEstado({ fase: 'autenticado', usuario: data });
      return undefined;
    }
    return error?.detail ?? 'No se ha podido iniciar sesión.';
  }, []);

  const logout = useCallback(async () => {
    await api.POST('/auth/logout');
    setEstado({ fase: 'anonimo' });
  }, []);

  const valor = useMemo(() => ({ estado, login, logout }), [estado, login, logout]);
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

export function useAuth(): Auth {
  const auth = useContext(AuthContext);
  if (!auth) {
    throw new Error('useAuth fuera de AuthProvider');
  }
  return auth;
}
