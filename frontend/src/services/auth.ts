import { api, unwrap } from "./http";
import type { AuthUser } from "./types";

export const authService = {
  login: async (email: string, password: string) => {
    const res = await api.post("/auth/login", { email, password });
    return unwrap<{ accessToken: string; refreshToken: string }>(res.data);
  },

  register: async (username: string, email: string, password: string) => {
    const res = await api.post("/auth/register", { username, email, password });
    return unwrap<{ accessToken: string; refreshToken: string }>(res.data);
  },

  logout: async () => {
    return api.post("/auth/logout");
  },

  getMe: async () => {
    const res = await api.get("/users/me");
    return unwrap<AuthUser>(res.data);
  },

  updateProfile: async (data: { username: string; email: string }) => {
    return api.put("/users/me", data);
  },

  changePassword: async (data: { oldPassword: string; newPassword: string }) => {
    return api.put("/users/me/password", data);
  },
};
