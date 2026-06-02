import { useEffect, type ReactNode } from "react";
import { useNavigate } from "@tanstack/react-router";
import { Box, CircularProgress, Stack, Typography } from "@mui/material";
import { useAuth } from "@/context/auth";
import type { Role } from "@/services";

export function ProtectedRoute({ children, roles }: { children: ReactNode; roles?: Role[] }) {
  const { user, loading, hasRole } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!loading && !user) navigate({ to: "/login" });
  }, [loading, user, navigate]);

  if (loading || !user) {
    return (
      <Box sx={{ display: "grid", placeItems: "center", minHeight: "60vh" }}>
        <CircularProgress />
      </Box>
    );
  }

  if (roles && !roles.some((r) => hasRole(r))) {
    return (
      <Stack spacing={1} sx={{ p: 6, textAlign: "center" }}>
        <Typography variant="h5" sx={{ fontWeight: 700 }}>
          Accès refusé
        </Typography>
        <Typography color="text.secondary">
          Vous n'avez pas les permissions requises pour cette page.
        </Typography>
      </Stack>
    );
  }

  return <>{children}</>;
}
