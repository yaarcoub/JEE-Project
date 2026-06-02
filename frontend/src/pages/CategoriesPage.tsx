import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Box,
  Button,
  Card,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Stack,
  TextField,
  Tooltip,
} from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import { categoriesService, type Category } from "@/services";
import { useAuth } from "@/context/auth";
import { PageHeader } from "@/components/PageHeader";
import { ConfirmDialog } from "@/components/ConfirmDialog";
import { notifySuccess, notifyError, getErrorMessage } from "@/utils/notifications";
import { useTranslation } from "react-i18next";

export function CategoriesPage() {
  const { t } = useTranslation();
  const { hasRole } = useAuth();
  const canEdit = hasRole("ROLE_ADMIN", "ROLE_MANAGER");
  const canDelete = hasRole("ROLE_ADMIN");
  const qc = useQueryClient();
  const [editing, setEditing] = useState<Partial<Category> | null>(null);
  const [toDelete, setToDelete] = useState<Category | null>(null);

  const list = useQuery({
    queryKey: ["categories"],
    queryFn: () => categoriesService.list(),
  });

  const save = useMutation({
    mutationFn: (c: Partial<Category>) =>
      c.id ? categoriesService.update(c.id, c) : categoriesService.create(c),
    onSuccess: (_, c) => {
      notifySuccess(c.id ? t("categories.updateSuccess") : t("categories.createSuccess"));
      qc.invalidateQueries({ queryKey: ["categories"] });
      setEditing(null);
    },
    onError: (err) => notifyError(getErrorMessage(err)),
  });

  const del = useMutation({
    mutationFn: (id: number) => categoriesService.delete(id),
    onSuccess: () => {
      notifySuccess(t("categories.deleteSuccess"));
      qc.invalidateQueries({ queryKey: ["categories"] });
    },
    onError: (err) => notifyError(getErrorMessage(err)),
  });

  const columns: GridColDef<Category>[] = [
    {
      field: "color",
      headerName: "",
      width: 60,
      renderCell: (p) => (
        <Box
          sx={{
            width: 20,
            height: 20,
            borderRadius: "50%",
            bgcolor: (p.value as string) ?? "#e8ecf1",
          }}
        />
      ),
    },
    { field: "name", headerName: t("categories.name"), flex: 1 },
    { field: "description", headerName: t("books.description"), flex: 2 },
    {
      field: "bookCount",
      headerName: t("nav.books"),
      width: 110,
      renderCell: (p) => <Chip size="small" label={p.value ?? 0} />,
    },
    {
      field: "actions",
      headerName: "",
      width: 110,
      sortable: false,
      renderCell: (p) =>
        canEdit ? (
          <Stack direction="row" spacing={0.5}>
            <Tooltip title={t("books.editTip")}>
              <IconButton size="small" onClick={() => setEditing(p.row)}>
                <EditIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            {canDelete && (
              <Tooltip title={t("categories.deleteTip")}>
                <IconButton size="small" color="error" onClick={() => setToDelete(p.row)}>
                  <DeleteIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            )}
          </Stack>
        ) : null,
    },
  ];

  return (
    <Box>
      <PageHeader
        title={t("categories.title")}
        subtitle={t("categories.subtitle")}
        actions={
          canEdit ? (
            <Button startIcon={<AddIcon />} variant="contained" onClick={() => setEditing({})}>
              {t("categories.add")}
            </Button>
          ) : null
        }
      />

      <Card>
        <DataGrid
          autoHeight
          rows={list.data?.filter((row) => row.id != null) ?? []}
          columns={columns}
          loading={list.isLoading}
          disableRowSelectionOnClick
          getRowId={(row) => row.id ?? Math.random()}
          sx={{ border: 0, "& .MuiDataGrid-columnHeaders": { bgcolor: "#f7f9fc" } }}
        />
      </Card>

      <Dialog open={!!editing} onClose={() => setEditing(null)} fullWidth maxWidth="sm">
        <DialogTitle sx={{ fontWeight: 700 }}>
          {editing?.id ? t("books.edit") : t("categories.add")}
        </DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              label={t("categories.name")}
              value={editing?.name ?? ""}
              onChange={(e) => setEditing({ ...editing, name: e.target.value })}
              required
            />
            <TextField
              label={t("books.description")}
              value={editing?.description ?? ""}
              onChange={(e) => setEditing({ ...editing, description: e.target.value })}
              multiline
              minRows={2}
            />
            <TextField
              type="color"
              label="Color"
              value={editing?.color ?? "#3b82f6"}
              onChange={(e) => setEditing({ ...editing, color: e.target.value })}
              slotProps={{ inputLabel: { shrink: true } }}
            />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setEditing(null)}>{t("categories.cancel")}</Button>
          <Button
            variant="contained"
            onClick={() => editing && save.mutate(editing)}
            disabled={save.isPending}
          >
            {t("categories.create")}
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmDialog
        open={!!toDelete}
        title={t("categories.deleteTip")}
        onClose={() => setToDelete(null)}
        onConfirm={() => {
          if (toDelete) {
            del.mutate(toDelete.id);
            setToDelete(null);
          }
        }}
      />
    </Box>
  );
}
