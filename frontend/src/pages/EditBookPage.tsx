import { useNavigate } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Box, CircularProgress, Card, Typography, Chip, Stack } from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import { booksService, type Book, type BookFormValues, type Loan } from "@/services";
import { PageHeader } from "@/components/PageHeader";
import { BookForm } from "@/components/BookForm";
import { notifySuccess, notifyError, getErrorMessage } from "@/utils/notifications";
import { useTranslation } from "react-i18next";

interface EditBookPageProps {
  bookId: string;
}

export function EditBookPage({ bookId }: EditBookPageProps) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const book = useQuery({
    queryKey: ["book", bookId],
    queryFn: () => booksService.get(Number(bookId)),
  });

  const cats = useQuery({
    queryKey: ["categories"],
    queryFn: () => booksService.getCategories(),
  });

  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(5);

  const loans = useQuery({
    queryKey: ["bookLoans", bookId, page, pageSize],
    queryFn: () => booksService.getBookLoans(Number(bookId), { page, size: pageSize }),
  });

  const columns: GridColDef<Loan>[] = [
    { field: "loanDate", headerName: t("loans.loanDate"), flex: 1 },
    { field: "expectedReturnDate", headerName: t("loans.expectedReturn"), flex: 1 },
    { field: "actualReturnDate", headerName: t("loans.returnDate"), flex: 1 },
    { field: "status", headerName: t("loans.status"), width: 130, renderCell: (p) => (
      <Chip
        label={t(`loans.status_${p.value}`) || p.value}
        size="small"
        color={p.value === "ACTIVE" ? "primary" : p.value === "RETURNED" ? "success" : "error"}
        variant={p.value === "OVERDUE" ? "filled" : "outlined"}
      />
    )},
    { field: "user", headerName: t("loans.user"), flex: 1, valueGetter: (v, row) => row.user?.username },
  ];

  const update = useMutation({
    mutationFn: (v: BookFormValues) => booksService.update(Number(bookId), v),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["books"] });
      navigate({ to: "/books" });
    },
    onError: (err) => {
      notifyError(getErrorMessage(err));
    },
  });

  if (book.isLoading) {
    return (
      <Box sx={{ display: "grid", placeItems: "center", minHeight: 300 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <PageHeader
        title={book.data?.title ?? t("dashboard.book")}
        subtitle={t("books.edit")}
      />
      <BookForm
        initial={book.data}
        categories={cats.data ?? []}
        submitting={update.isPending}
        onCancel={() => navigate({ to: "/books" })}
        onSubmit={(v) => update.mutate(v)}
      />

      <Box sx={{ mt: 6, mb: 4 }} className="animate-in fade-in slide-in-from-bottom-4 duration-500 delay-100">
        <Typography variant="h6" sx={{ fontWeight: 700, mb: 2, color: "text.primary" }}>
          {t("books.loanHistory")}
        </Typography>
        
        {loans.data?.content && loans.data.content.length > 0 ? (
          <Card sx={{ borderRadius: 3, overflow: "hidden", boxShadow: "0 4px 6px -1px rgb(0 0 0 / 0.1)" }}>
            <DataGrid
              autoHeight
              rows={loans.data.content}
              rowCount={loans.data.totalElements}
              paginationModel={{ pageSize, page }}
              onPaginationModelChange={(m) => {
                setPage(m.page);
                setPageSize(m.pageSize);
              }}
              pageSizeOptions={[5, 10, 20]}
              columns={columns}
              loading={loans.isLoading}
              disableRowSelectionOnClick
              paginationMode="server"
              getRowId={(row) => row.id}
              sx={{ border: 0 }}
            />
          </Card>
        ) : (
          <Card sx={{ borderRadius: 3, p: 4, textAlign: "center", bgcolor: "rgba(0,0,0,0.02)" }}>
            <Typography color="text.secondary">{t("books.noHistory")}</Typography>
          </Card>
        )}
      </Box>
    </Box>
  );
}
