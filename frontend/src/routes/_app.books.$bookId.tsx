import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { EditBookPage } from "@/pages/EditBookPage";

export const Route = createFileRoute("/_app/books/$bookId")({
  head: () => ({ meta: [{ title: "Livre — Library" }] }),
  component: () => {
    const { bookId } = Route.useParams();
    return (
      <ProtectedRoute roles={["ROLE_ADMIN", "ROLE_MANAGER"]}>
        <EditBookPage bookId={bookId} />
      </ProtectedRoute>
    );
  },
});
