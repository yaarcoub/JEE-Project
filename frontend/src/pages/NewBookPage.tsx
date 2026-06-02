import { useNavigate } from "@tanstack/react-router";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Box, CircularProgress } from "@mui/material";
import { booksService, type Category, type BookFormValues } from "@/services";
import { PageHeader } from "@/components/PageHeader";
import { BookForm } from "@/components/BookForm";
import { notifySuccess, notifyError, getErrorMessage } from "@/utils/notifications";
import { useTranslation } from "react-i18next";

export function NewBookPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const cats = useQuery({
    queryKey: ["categories"],
    queryFn: () => booksService.getCategories(),
  });

  const create = useMutation({
    mutationFn: (v: BookFormValues) => booksService.create(v),
    onSuccess: () => {
      navigate({ to: "/books" });
    },
    onError: (err) => {
      notifyError(getErrorMessage(err));
    },
  });

  return (
    <Box>
      <PageHeader title={t("books.add")} subtitle={t("books.subtitle")} />
      <BookForm
        categories={cats.data ?? []}
        submitting={create.isPending}
        onCancel={() => navigate({ to: "/books" })}
        onSubmit={(v) => create.mutate(v)}
      />
    </Box>
  );
}
