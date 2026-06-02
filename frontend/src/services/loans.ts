import { api, unwrap } from "./http";
import type { Book, Loan, PagedResponse, UserAdmin } from "./types";

export const loansService = {
  list: async (params: { page?: number; size?: number; status?: string }) => {
    const res = await api.get("/loans", { params });
    return unwrap<PagedResponse<Loan>>(res.data);
  },

  getMyLoans: async (params: { page?: number; size?: number }) => {
    const res = await api.get("/loans/my", { params });
    return unwrap<PagedResponse<Loan>>(res.data);
  },

  create: async (data: {
    bookId: number;
    expectedReturnDate: string;
    notes?: string;
    condition?: string;
  }) => {
    return api.post("/loans", data);
  },

  returnLoan: async (id: number, returnedBy: string) => {
    return api.put(`/loans/${id}/return`, null, { params: { returnedBy } });
  },

  getBooks: async (size: number = 200) => {
    const res = await api.get("/books", { params: { size } });
    return unwrap<PagedResponse<Book>>(res.data);
  },

  getUsers: async (size: number = 200) => {
    const res = await api.get("/users", { params: { size } });
    return unwrap<PagedResponse<UserAdmin>>(res.data);
  },
};
