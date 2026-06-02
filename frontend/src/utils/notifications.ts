import { toast } from "sonner";

export function notifySuccess(message: string, id?: string) {
  console.log("[SUCCESS]", message);
  toast.success(message, {
    id: id || message,
    duration: 4000,
    position: "top-right",
  });
}

export function notifyError(message: string, id?: string) {
  console.error("[ERROR]", message);
  toast.error(message, {
    id: id || message,
    duration: 6000,
    position: "top-right",
  });
}

export function notifyInfo(message: string, id?: string) {
  console.info("[INFO]", message);
  toast.info(message, {
    id: id || message,
    duration: 4000,
    position: "top-right",
  });
}

export function notifyWarning(message: string, id?: string) {
  console.warn("[WARNING]", message);
  toast.warning(message, {
    id: id || message,
    duration: 5000,
    position: "top-right",
  });
}

import axios from "axios";
import i18n from "@/lib/i18n";

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error) && error.response) {
    const apiError = error.response.data as { message?: string; errors?: Record<string, string>; details?: Record<string, string>; error?: string };

    if (apiError?.errors) {
      const errorList = Object.entries(apiError.errors)
        .map(([field, msg]) => `${field}: ${msg}`)
        .join(", ");
      return `Erreurs: ${errorList}`;
    }

    if (apiError?.details) {
      const errorList = Object.entries(apiError.details)
        .map(([field, msg]) => `${field}: ${msg}`)
        .join(", ");
      return `Erreurs: ${errorList}`;
    }

    if (apiError?.message) {
      if (apiError.message === "error.optimisticLocking") {
        return i18n.t("common.errorOptimisticLocking");
      }
      return apiError.message;
    }
    
    const fallbackError = error.response.data as { error?: string, status?: number, path?: string };
    if (fallbackError?.error) {
      if (fallbackError.error === "Bad credentials") {
        return "Email ou mot de passe incorrect";
      }
      if (fallbackError.error === "Unauthorized" || fallbackError.error === "Full authentication is required to access this resource") {
        return "Vous devez être connecté pour effectuer cette action.";
      }
      return fallbackError.error;
    }
    
    if (fallbackError?.status === 401) return "Session expirée ou non autorisé.";
    if (fallbackError?.status === 403) return "Accès refusé.";
  }

  if (error instanceof Error) {
    return error.message;
  }

  if (error && typeof error === "object" && "message" in error) {
    return (error as { message: string }).message;
  }

  return "Une erreur inattendue est survenue";
}
