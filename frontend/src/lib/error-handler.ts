import type { AxiosError, AxiosInstance } from "axios";
import { notifyError } from "@/utils/notifications";

interface ApiError {
  message: string;
  code?: string;
  status: number;
}

export function parseApiError(error: unknown): ApiError {
  if (error instanceof Error && "response" in error) {
    const axiosError = error as AxiosError<{ message?: string, error?: string, errors?: Record<string, string> }>;
    const data = axiosError.response?.data;
    
    let extractedMessage = data?.error || data?.message;
    
    if (data?.errors) {
      const errorList = Object.entries(data.errors)
        .map(([field, msg]) => `${field}: ${msg}`)
        .join(", ");
      extractedMessage = `Erreurs: ${errorList}`;
    }

    if (!extractedMessage) {
      if (axiosError.response?.status === 401) extractedMessage = "Identifiants incorrects ou session expirée";
      else if (axiosError.response?.status === 403) extractedMessage = "Vous n'avez pas la permission d'effectuer cette action";
      else extractedMessage = axiosError.message;
    }

    return {
      message: extractedMessage,
      code: axiosError.code,
      status: axiosError.response?.status || 500,
    };
  }

  if (error instanceof Error) {
    return {
      message: error.message,
      status: 500,
    };
  }

  return {
    message: "Une erreur inconnue est survenue",
    status: 500,
  };
}

export function setupErrorInterceptors(api: AxiosInstance) {
  api.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
      const parsedError = parseApiError(error);
      const isGetMeEndpoint = error.config?.url?.includes("/users/me");
      const isLoginPage = typeof window !== "undefined" && window.location.pathname === "/login";

      // Handle specific status codes
      switch (parsedError.status) {
        case 401:
          // Unauthorized - handled by AuthContext
          if (typeof window !== "undefined") {
            localStorage.removeItem("lib_access_token");
            localStorage.removeItem("lib_refresh_token");
            if (!window.location.pathname.startsWith("/login")) {
              window.location.href = "/login";
            }
          }
          break;
        // Global error toasts are disabled here to avoid duplication
        // since components already handle their own notifyError() calls.
      }

      return Promise.reject(parsedError);
    },
  );
}
