import { useCallback, useState } from "react";
import { parseApiError } from "@/lib/error-handler";
import { notifyError, notifySuccess } from "@/utils/notifications";

export function useApiError() {
  return useCallback((error: unknown) => {
    const apiError = parseApiError(error);
    notifyError(apiError.message);
    return apiError;
  }, []);
}

export function useNotification() {
  return { notifyError, notifySuccess };
}

export function useLoading(initialState = false) {
  const [loading, setLoading] = useState(initialState);

  const withLoading = useCallback(async <T>(fn: () => Promise<T>): Promise<T | undefined> => {
    setLoading(true);
    try {
      return await fn();
    } finally {
      setLoading(false);
    }
  }, []);

  return { loading, withLoading, setLoading };
}
