import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { DashboardPage } from "@/pages/DashboardPage";

export const Route = createFileRoute("/_app/dashboard")({
  head: () => ({ meta: [{ title: "Tableau de bord — Library" }] }),
  component: () => (
    <ProtectedRoute roles={["ROLE_ADMIN", "ROLE_MANAGER", "ROLE_USER"]}>
      <DashboardPage />
    </ProtectedRoute>
  ),
});
