import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  MenuItem,
  Stack,
  TextField,
  Tooltip,
} from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import AddIcon from "@mui/icons-material/Add";
import ReturnIcon from "@mui/icons-material/AssignmentReturn";
import { loansService, type Loan, type LoanStatus } from "@/services";
import { useAuth } from "@/context/auth";
import { notifySuccess, notifyError, getErrorMessage } from "@/utils/notifications";
import { useTranslation } from "react-i18next";

const statusColor: Record<LoanStatus, "default" | "success" | "warning" | "error" | "info"> = {
  PENDING: "default",
  ACTIVE: "info",
  RETURNED: "success",
  OVERDUE: "error",
};

export function LoansPage() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const qc = useQueryClient();
  const [status, setStatus] = useState<string>("");
  const [openNew, setOpenNew] = useState(false);
  const [form, setForm] = useState({
    bookId: "",
    userId: "",
    loanDate: "",
    expectedReturnDate: "",
  });

  const loans = useQuery({
    queryKey: ["loans", status],
    queryFn: () => loansService.list({ page: 0, size: 50, ...(status ? { status } : {}) }),
  });

  const books = useQuery({
    queryKey: ["books", "all-light"],
    queryFn: () => loansService.getBooks(),
  });

  const users = useQuery({
    queryKey: ["users", "all-light"],
    queryFn: () => loansService.getUsers(),
    retry: false,
  });

  const createLoan = useMutation({
    mutationFn: () =>
      loansService.create({
        bookId: Number(form.bookId),
        userId: Number(form.userId),
        loanDate: form.loanDate,
        expectedReturnDate: form.expectedReturnDate,
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["loans"] });
      setOpenNew(false);
      setForm({ bookId: "", userId: "", loanDate: "", expectedReturnDate: "" });
    },
    onError: (err) => notifyError(getErrorMessage(err)),
  });

  const ret = useMutation({
    mutationFn: (id: number) => loansService.returnLoan(id, user?.username ?? "system"),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["loans"] });
    },
    onError: (err) => notifyError(getErrorMessage(err)),
  });

  const columns: GridColDef<Loan>[] = [
    { field: "user", headerName: t("loans.borrower"), flex: 1, valueGetter: (_, r) => r.user?.username },
    { field: "book", headerName: t("loans.book"), flex: 1.3, valueGetter: (_, r) => r.book?.title },
    { field: "loanDate", headerName: t("loans.loanDate"), width: 130 },
    { field: "expectedReturnDate", headerName: t("loans.expectedReturn"), width: 140 },
    {
      field: "status",
      headerName: t("loans.status"),
      width: 130,
      renderCell: (p) => (
        <Chip
          label={p.value as string}
          color={statusColor[p.value as LoanStatus]}
          size="small"
          sx={{ fontWeight: "bold" }}
        />
      ),
    },
    {
      field: "actions",
      headerName: "",
      width: 80,
      sortable: false,
      renderCell: (p) =>
        p.row.status === "ACTIVE" || p.row.status === "OVERDUE" ? (
          <Tooltip title={t("loans.returnTip")}>
            <IconButton size="small" sx={{ color: "#10b981" }} onClick={() => ret.mutate(p.row.id)}>
              <ReturnIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        ) : null,
    },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
        <div>
          <h1 className="text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-emerald-400 to-teal-400">
            {t("loans.title")}
          </h1>
          <p className="text-gray-400 mt-1 font-light">{t("loans.subtitle")}</p>
        </div>

        <Button
          startIcon={<AddIcon />}
          variant="contained"
          onClick={() => setOpenNew(true)}
          sx={{
            background: "linear-gradient(to right, #10b981, #14b8a6)",
            "&:hover": { opacity: 0.9 },
          }}
        >
          {t("loans.add")}
        </Button>
      </div>

      <div className="glass rounded-2xl p-6">
        <TextField
          select
          label={t("loans.filterStatus")}
          value={status}
          onChange={(e) => setStatus(e.target.value)}
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
          <MenuItem value="PENDING">PENDING</MenuItem>
          <MenuItem value="ACTIVE">ACTIVE</MenuItem>
          <MenuItem value="RETURNED">RETURNED</MenuItem>
          <MenuItem value="OVERDUE">OVERDUE</MenuItem>
        </TextField>
      </div>

      <div className="glass rounded-2xl overflow-hidden p-2">
        <DataGrid
          autoHeight
          rows={loans.data?.content?.filter((row) => row.id != null) ?? []}
          columns={columns}
          loading={loans.isLoading}
          disableRowSelectionOnClick
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

      <Dialog open={openNew} onClose={() => setOpenNew(false)} fullWidth maxWidth="sm">
        <DialogTitle sx={{ fontWeight: 700 }}>{t("loans.add")}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              select
              label={t("loans.book")}
              value={form.bookId}
              onChange={(e) => setForm({ ...form, bookId: e.target.value })}
            >
              {(books.data?.content ?? []).map((b) => (
                <MenuItem key={b.id} value={b.id}>
                  {b.title} (stock: {b.stock})
                </MenuItem>
              ))}
            </TextField>
            <TextField
              select
              label={t("loans.user")}
              value={form.userId}
              onChange={(e) => setForm({ ...form, userId: e.target.value })}
            >
              {(users.data?.content ?? []).map((u) => (
                <MenuItem key={u.id} value={u.id}>
                  {u.username} — {u.email}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              type="date"
              label={t("loans.loanDate")}
              value={form.loanDate}
              onChange={(e) => setForm({ ...form, loanDate: e.target.value })}
              slotProps={{ inputLabel: { shrink: true } }}
            />
            <TextField
              type="date"
              label={t("loans.expectedReturn")}
              value={form.expectedReturnDate}
              onChange={(e) => setForm({ ...form, expectedReturnDate: e.target.value })}
              slotProps={{ inputLabel: { shrink: true } }}
            />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setOpenNew(false)}>{t("loans.cancel")}</Button>
          <Button
            variant="contained"
            onClick={() => createLoan.mutate()}
            disabled={createLoan.isPending}
          >
            {t("loans.create")}
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
}
