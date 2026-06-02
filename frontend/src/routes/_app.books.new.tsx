import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { NewBookPage } from "@/pages/NewBookPage";

export const Route = createFileRoute("/_app/books/new")({
  head: () => ({ meta: [{ title: "Nouveau livre — Library" }] }),
  component: () => (
    <ProtectedRoute roles={["ROLE_ADMIN", "ROLE_MANAGER"]}>
      <NewBookPage />
    </ProtectedRoute>
  ),
});
