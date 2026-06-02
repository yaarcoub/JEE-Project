import { api, unwrap, API_BASE_URL, tokenStore } from "./http";
import type { Book, Category, PagedResponse, Loan } from "./types";

export interface BookFormValues {
  title: string;
  author: string;
  isbn?: string;
  description?: string;
  stock: number;
  publishedDate?: string;
  categoryIds: number[];
}

export const booksService = {
  list: async (params: { page?: number; size?: number; search?: string; categoryId?: string; sort?: string; direction?: string }) => {
    const res = await api.get("/books", { params });
    return unwrap<PagedResponse<Book>>(res.data);
  },

  get: async (id: number) => {
    const res = await api.get(`/books/${id}`);
    return unwrap<Book>(res.data);
  },

  create: async (data: BookFormValues) => {
    const { categoryIds, ...rest } = data;
    const res = await api.post("/books", rest);
    const book = unwrap<Book>(res.data);
    if (categoryIds.length) {
      await api.post(`/books/${book.id}/categories`, categoryIds);
    }
    return book;
  },

  update: async (id: number, data: BookFormValues) => {
    const { categoryIds, ...rest } = data;
    await api.put(`/books/${id}`, rest);
    if (categoryIds.length) {
      await api.post(`/books/${id}/categories`, categoryIds);
    }
  },

  delete: async (id: number) => {
    return api.delete(`/books/${id}`);
  },

  getCategories: async () => {
    const res = await api.get("/categories");
    return unwrap<Category[]>(res.data);
  },

  exportPdf: async () => {
    const res = await fetch("/api/export/pdf/books", {
      headers: { Authorization: `Bearer ${tokenStore.getAccess() ?? ""}` },
    });
    if (!res.ok) throw new Error("Export failed");
    return res.blob();
  },

  exportExcel: async () => {
    const res = await fetch("/api/export/excel/books", {
      headers: { Authorization: `Bearer ${tokenStore.getAccess() ?? ""}` },
    });
    if (!res.ok) throw new Error("Export failed");
    return res.blob();
  },

  getBookLoans: async (id: number, params?: { page?: number; size?: number }) => {
    const res = await api.get(`/books/${id}/loans`, { params });
    return unwrap<PagedResponse<Loan>>(res.data);
  },
};
