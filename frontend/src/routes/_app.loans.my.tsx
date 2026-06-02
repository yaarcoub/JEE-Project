import { createFileRoute } from "@tanstack/react-router";
import { MyLoansPage } from "@/pages/MyLoansPage";

export const Route = createFileRoute("/_app/loans/my")({
  head: () => ({ meta: [{ title: "Mes emprunts — Library" }] }),
  component: MyLoansPage,
});
