import { createFileRoute } from "@tanstack/react-router";
import { BooksPage } from "@/pages/BooksPage";

export const Route = createFileRoute("/_app/books/")({
  head: () => ({ meta: [{ title: "Livres — Library" }] }),
  component: BooksPage,
});
