import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { authService, tokenStore, type AuthUser, type Role } from "@/services";

interface AuthContextValue {
  user: AuthUser | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, email: string, password: string) => Promise<void>;
  logout: () => void;
  hasRole: (...roles: Role[]) => boolean;
  refresh: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchMe = useCallback(async () => {
    try {
      const me = await authService.getMe();
      setUser(me);
      tokenStore.setUser(me);
    } catch (err) {
      tokenStore.clear();
      setUser(null);
    }
  }, []);

  useEffect(() => {
    const cached = tokenStore.getUser();
    if (cached) setUser(cached);
    if (tokenStore.getAccess()) {
      fetchMe().finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, [fetchMe]);

  const login = useCallback(
    async (username: string, password: string) => {
      const data = await authService.login(username, password);
      tokenStore.set(data.accessToken, data.refreshToken);
      await fetchMe();
    },
    [fetchMe],
  );

  const register = useCallback(
    async (username: string, email: string, password: string) => {
      const data = await authService.register(username, email, password);
      tokenStore.set(data.accessToken, data.refreshToken);
      await fetchMe();
    },
    [fetchMe],
  );

  const logout = useCallback(() => {
    authService.logout().catch(() => {});
    tokenStore.clear();
    setUser(null);
    if (typeof window !== "undefined") window.location.href = "/login";
  }, []);

  const hasRole = useCallback(
    (...roles: Role[]) => !!user && roles.some((r) => user.roles?.includes(r)),
    [user],
  );

  const value = useMemo<AuthContextValue>(
    () => ({ user, loading, login, register, logout, hasRole, refresh: fetchMe }),
    [user, loading, login, register, logout, hasRole, fetchMe],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
