import { createContext, useState } from 'react';
import { clearStoredSession, getStoredSession, saveStoredSession } from '../lib/storage';
import { loginRequest } from '../services/auth.service';

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [session, setSession] = useState(() => getStoredSession());

  const login = async ({ email, password, rememberSession }) => {
    const payload = await loginRequest({ email, password });

    const nextSession = {
      token: payload.token,
      user: {
        username: payload.username,
        fullName: payload.fullName,
        role: payload.role,
        accessScope: payload.accessScope,
      },
      rememberSession,
      authenticatedAt: new Date().toISOString(),
    };

    saveStoredSession(nextSession, rememberSession);
    setSession(nextSession);

    return nextSession;
  };

  const logout = () => {
    clearStoredSession();
    setSession(null);
  };

  const value = {
    session,
    user: session?.user ?? null,
    token: session?.token ?? null,
    isAuthenticated: Boolean(session?.token),
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
