import { api, unwrap } from "./http";
import type { Category } from "./types";

export const categoriesService = {
  list: async () => {
    const res = await api.get("/categories");
    return unwrap<Category[]>(res.data);
  },

  create: async (data: Partial<Category>) => {
    return api.post("/categories", data);
  },

  update: async (id: number, data: Partial<Category>) => {
    return api.put(`/categories/${id}`, data);
  },

  delete: async (id: number) => {
    return api.delete(`/categories/${id}`);
  },
};
