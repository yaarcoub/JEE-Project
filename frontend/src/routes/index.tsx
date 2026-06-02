import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useEffect } from "react";
import { Box, CircularProgress } from "@mui/material";
import { useAuth } from "@/context/auth";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "Library — Gestion de bibliothèque" },
      { name: "description", content: "Application de gestion de bibliothèque ENSAM." },
    ],
  }),
  component: Index,
});

function Index() {
  const { user, loading } = useAuth();
  const navigate = useNavigate();
  useEffect(() => {
    if (loading) return;
    navigate({ to: user ? "/dashboard" : "/login" });
  }, [user, loading, navigate]);
  return (
    <Box sx={{ display: "grid", placeItems: "center", minHeight: "100vh" }}>
      <CircularProgress />
    </Box>
  );
}
