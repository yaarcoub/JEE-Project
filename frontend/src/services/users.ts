import { api, unwrap } from "./http";
import type { PagedResponse, Role, UserAdmin } from "./types";

export const usersService = {
  list: async (params: { page?: number; size?: number }) => {
    const res = await api.get("/users", { params });
    return unwrap<PagedResponse<UserAdmin>>(res.data);
  },

  toggleActive: async (id: number) => {
    return api.put(`/users/${id}/toggle`);
  },

  updateRoles: async (id: number, roles: Role[]) => {
    return api.put(`/users/${id}/roles`, roles);
  },

  delete: async (id: number) => {
    return api.delete(`/users/${id}`);
  },
};
