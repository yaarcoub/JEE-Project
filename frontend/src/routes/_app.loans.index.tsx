import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { LoansPage } from "@/pages/LoansPage";

export const Route = createFileRoute("/_app/loans/")({
  head: () => ({ meta: [{ title: "Emprunts — Library" }] }),
  component: () => (
    <ProtectedRoute roles={["ROLE_ADMIN", "ROLE_MANAGER"]}>
      <LoansPage />
    </ProtectedRoute>
  ),
});
