import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { UsersPage } from "@/pages/UsersPage";

export const Route = createFileRoute("/_app/users")({
  head: () => ({ meta: [{ title: "Utilisateurs — Library" }] }),
  component: () => (
    <ProtectedRoute roles={["ROLE_ADMIN"]}>
      <UsersPage />
    </ProtectedRoute>
  ),
});
