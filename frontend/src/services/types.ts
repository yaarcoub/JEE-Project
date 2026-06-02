export type Role = "ROLE_ADMIN" | "ROLE_MANAGER" | "ROLE_USER";

export interface AuthUser {
  username: string;
  email: string;
  roles: Role[];
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
  color?: string;
  bookCount?: number;
}

export interface Book {
  id: number;
  title: string;
  author: string;
  isbn?: string;
  description?: string;
  stock: number;
  publishedDate?: string;
  categories?: Category[];
  createdAt?: string;
  updatedAt?: string;
}

export type LoanStatus = "PENDING" | "ACTIVE" | "RETURNED" | "OVERDUE";

export interface Loan {
  id: number;
  loanDate: string;
  expectedReturnDate: string;
  actualReturnDate?: string | null;
  status: LoanStatus;
  book: Book;
  user: { id: number; username: string; email: string };
  detail?: {
    id: number;
    notes?: string;
    itemcondition?: string;
    renewalCount?: number;
    returnedBy?: string;
  };
}

export interface UserAdmin {
  id: number;
  username: string;
  email: string;
  roles: Role[];
  active?: boolean;
  enabled?: boolean;
  createdAt?: string;
  loanCount?: number;
}

export interface DashboardStats {
  totalBooks: number;
  totalUsers: number;
  activeLoans: number;
  overdueLoans: number;
  totalLoansThisMonth: number;
  availableBooks: number;
  loansByMonth: Array<{ month: string; loans: number }>;
  booksByCategory: Array<{ category: string; bookCount: number }>;
}
