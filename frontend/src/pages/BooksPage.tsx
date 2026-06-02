import { useNavigate } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Button, Chip, IconButton, MenuItem, Stack, TextField, Tooltip, Dialog, DialogTitle, DialogContent, DialogActions } from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import PictureAsPdfIcon from "@mui/icons-material/PictureAsPdf";
import GridOnIcon from "@mui/icons-material/GridOn";
import BookmarkAddIcon from "@mui/icons-material/BookmarkAdd";
import { booksService, loansService, type Book, type Category } from "@/services";
import { useAuth } from "@/context/auth";
import { ConfirmDialog } from "@/components/ConfirmDialog";
import { notifySuccess, notifyError, getErrorMessage } from "@/utils/notifications";
import { useTranslation } from "react-i18next";

export function BooksPage() {
  const { t } = useTranslation();
  const { hasRole } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const canEdit = hasRole("ROLE_ADMIN", "ROLE_MANAGER");
  const canDelete = hasRole("ROLE_ADMIN");
  const isUser = hasRole("ROLE_USER");

  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [search, setSearch] = useState("");
  const [categoryId, setCategoryId] = useState<string>("");
  const [toDelete, setToDelete] = useState<Book | null>(null);
  
  const [borrowing, setBorrowing] = useState<Book | null>(null);
  const [expectedReturnDate, setExpectedReturnDate] = useState("");
  const [notes, setNotes] = useState("");

  const myLoans = useQuery({
    queryKey: ["loans", "my"],
    queryFn: () => loansService.getMyLoans({ page: 0, size: 50 }),
    enabled: isUser,
  });

  const cats = useQuery({
    queryKey: ["categories"],
    queryFn: () => booksService.getCategories(),
  });

  const books = useQuery({
    queryKey: ["books", page, pageSize, search, categoryId],
    queryFn: () =>
      booksService.list({
        page,
        size: pageSize,
        ...(search ? { search } : {}),
        ...(categoryId ? { categoryId } : {}),
      }),
  });

  const del = useMutation({
    mutationFn: (id: number) => booksService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["books"] });
    },
    onError: (err) => notifyError(getErrorMessage(err)),
  });

  const borrow = useMutation({
    mutationFn: () =>
      loansService.create({
        bookId: borrowing!.id,
        expectedReturnDate,
        notes,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["books"] });
      setBorrowing(null);
      setExpectedReturnDate("");
      setNotes("");
    },
    onError: (err) => notifyError(getErrorMessage(err)),
  });

  const downloadExport = async (kind: "pdf" | "excel") => {
    try {
      const blob =
        kind === "pdf" ? await booksService.exportPdf() : await booksService.exportExcel();
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = kind === "pdf" ? "livres.pdf" : "livres.xlsx";
      a.click();
      URL.revokeObjectURL(url);
      notifySuccess(`Export ${kind.toUpperCase()} réussi`);
    } catch (err) {
      notifyError(getErrorMessage(err));
    }
  };

  const columns = useMemo<GridColDef<Book>[]>(
    () => [
      { field: "title", headerName: t("books.titleCol"), flex: 1.5, minWidth: 200 },
      { field: "author", headerName: t("books.author"), flex: 1, minWidth: 150 },
      { field: "isbn", headerName: t("books.isbn"), width: 160 },
      {
        field: "stock",
        headerName: t("books.stock"),
        width: 100,
        renderCell: (p) => (
          <Chip
            size="small"
            label={p.value}
            color={p.value > 0 ? "success" : "default"}
            variant={p.value > 0 ? "filled" : "outlined"}
            sx={{ fontWeight: "bold" }}
          />
        ),
      },
      {
        field: "categories",
        headerName: t("books.category"),
        flex: 1,
        minWidth: 180,
        sortable: false,
        renderCell: (p) => (
          <Stack direction="row" spacing={0.5} sx={{ flexWrap: "wrap", gap: 0.5 }}>
            {(p.value as Category[] | undefined)?.slice(0, 3).map((c) => (
              <Chip
                key={c.id}
                label={c.name}
                size="small"
                sx={{ bgcolor: c.color ?? "#3b82f6", color: "white", fontWeight: 600 }}
              />
            ))}
          </Stack>
        ),
      },
      {
        field: "actions",
        headerName: "",
        width: 120,
        sortable: false,
        filterable: false,
        renderCell: (p) => {
          const hasActiveLoan = myLoans.data?.content?.some(
            (l: any) => l.book.id === p.row.id && l.status === "ACTIVE"
          );
          return (
          <Stack direction="row" spacing={0.5}>
            {canEdit && (
              <Tooltip title={t("books.editTip")}>
                <IconButton
                  size="small"
                  sx={{ color: "#3b82f6" }}
                  onClick={() =>
                    navigate({ to: "/books/$bookId", params: { bookId: String(p.row.id) } })
                  }
                >
                  <EditIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            )}
            {canDelete && (
              <Tooltip title={t("books.deleteTip")}>
                <IconButton size="small" color="error" onClick={() => setToDelete(p.row)}>
                  <DeleteIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            )}
            {isUser && p.row.stock > 0 && !hasActiveLoan && (
              <Tooltip title={t("books.borrowTip")}>
                <IconButton size="small" sx={{ color: "#10b981" }} onClick={() => setBorrowing(p.row)}>
                  <BookmarkAddIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            )}
          </Stack>
          );
        },
      },
    ],
    [navigate, canDelete, canEdit, isUser, t, myLoans.data],
  );

  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
        <div>
          <h1 className="text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-blue-400">
            {t("books.title")}
          </h1>
          <p className="text-gray-400 mt-1 font-light">{t("books.subtitle")}</p>
        </div>

        {canEdit && (
          <div className="flex flex-wrap gap-3">
            <Button
              startIcon={<PictureAsPdfIcon />}
              variant="outlined"
              onClick={() => downloadExport("pdf")}
              sx={{
                color: "#ec4899",
                borderColor: "#ec4899",
                "&:hover": { borderColor: "#db2777", backgroundColor: "rgba(236, 72, 153, 0.1)" },
              }}
            >
              PDF
            </Button>
            <Button
              startIcon={<GridOnIcon />}
              variant="outlined"
              onClick={() => downloadExport("excel")}
              sx={{
                color: "#10b981",
                borderColor: "#10b981",
                "&:hover": { borderColor: "#059669", backgroundColor: "rgba(16, 185, 129, 0.1)" },
              }}
            >
              Excel
            </Button>
            <Button
              startIcon={<AddIcon />}
              variant="contained"
              onClick={() => navigate({ to: "/books/new" })}
              sx={{
                background: "linear-gradient(to right, #3b82f6, #8b5cf6)",
                "&:hover": { opacity: 0.9 },
              }}
            >
              {t("books.add")}
            </Button>
          </div>
        )}
      </div>

      <div className="glass rounded-2xl p-6">
        <div className="flex flex-col md:flex-row gap-4">
          <TextField
            label={t("books.search")}
            value={search}
            onChange={(e) => {
              setPage(0);
              setSearch(e.target.value);
            }}
            fullWidth
            sx={{
              "& .MuiOutlinedInput-root": {
                backgroundColor: "rgba(255,255,255,0.05)",
                color: "inherit",
              },
              "& .MuiInputLabel-root": { color: "#94a3b8" },
            }}
          />
          <TextField
            select
            label={t("books.category")}
            value={categoryId}
            onChange={(e) => {
              setPage(0);
              setCategoryId(e.target.value);
            }}
            sx={{
              minWidth: { xs: "100%", md: 240 },
              "& .MuiOutlinedInput-root": {
                backgroundColor: "rgba(255,255,255,0.05)",
                color: "inherit",
              },
              "& .MuiInputLabel-root": { color: "#94a3b8" },
            }}
          >
            <MenuItem value="">{t("loans.allStatus")}</MenuItem>
            {cats.data?.map((c) => (
              <MenuItem key={c.id} value={c.id}>
                {c.name}
              </MenuItem>
            ))}
          </TextField>
        </div>
      </div>

      <div className="glass rounded-2xl overflow-hidden p-2">
        <DataGrid
          autoHeight
          rows={books.data?.content?.filter((row) => row.id != null && row.title != null) ?? []}
          rowCount={books.data?.totalElements ?? 0}
          paginationModel={{ pageSize, page }}
          onPaginationModelChange={(m) => {
            setPage(m.page);
            setPageSize(m.pageSize);
          }}
          pageSizeOptions={[5, 10, 20, 50]}
          columns={columns}
          loading={books.isLoading}
          disableRowSelectionOnClick
          paginationMode="server"
          getRowId={(row) => row.id ?? Math.random()}
          sx={{
            border: 0,
            color: "inherit",
            "& .MuiDataGrid-columnHeaders": {
              backgroundColor: "rgba(0,0,0,0.2)",
              color: "inherit",
            },
            "& .MuiDataGrid-cell": { borderBottom: "1px solid rgba(255,255,255,0.1)" },
            "& .MuiDataGrid-footerContainer": { borderTop: "1px solid rgba(255,255,255,0.1)" },
            "& .MuiTablePagination-root": { color: "inherit" },
            "& .MuiIconButton-root": { color: "inherit" },
          }}
        />
      </div>

      <ConfirmDialog
        open={!!toDelete}
        title={t("books.deleteTip")}
        onClose={() => setToDelete(null)}
        onConfirm={() => {
          if (toDelete) {
            del.mutate(toDelete.id);
            setToDelete(null);
          }
        }}
      />

      <Dialog open={!!borrowing} onClose={() => setBorrowing(null)} fullWidth maxWidth="xs" PaperProps={{ sx: { borderRadius: 3, p: 1 } }}>
        <DialogTitle sx={{ fontWeight: 700, pb: 1 }}>{t("books.borrowTitle")} - {borrowing?.title}</DialogTitle>
        <DialogContent>
          <Stack spacing={2.5} sx={{ mt: 1 }}>
            <TextField
              type="date"
              label={t("books.expectedReturn")}
              value={expectedReturnDate}
              onChange={(e) => setExpectedReturnDate(e.target.value)}
              fullWidth
              required
              slotProps={{ inputLabel: { shrink: true } }}
            />
            <TextField
              label={t("books.notes")}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              fullWidth
              multiline
              rows={3}
              placeholder="Ex: Projet de fin d'études..."
            />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setBorrowing(null)} color="inherit" sx={{ borderRadius: 2 }}>{t("books.cancel")}</Button>
          <Button
            variant="contained"
            onClick={() => borrow.mutate()}
            disabled={!expectedReturnDate || borrow.isPending}
            sx={{
              borderRadius: 2,
              background: "linear-gradient(to right, #10b981, #059669)",
              color: "white",
              "&:hover": { opacity: 0.9 }
            }}
          >
            {t("books.borrow")}
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
}
