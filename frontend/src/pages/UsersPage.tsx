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
  Switch,
  TextField,
  Tooltip,
} from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import SecurityIcon from "@mui/icons-material/Security";
import DeleteIcon from "@mui/icons-material/Delete";
import { usersService, type Role, type UserAdmin } from "@/services";
import { ConfirmDialog } from "@/components/ConfirmDialog";
import { notifySuccess, notifyError, getErrorMessage } from "@/utils/notifications";
import { useTranslation } from "react-i18next";

const allRoles: Role[] = ["ROLE_ADMIN", "ROLE_MANAGER", "ROLE_USER"];

export function UsersPage() {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const [editing, setEditing] = useState<UserAdmin | null>(null);
  const [roles, setRoles] = useState<Role[]>([]);
  const [toDelete, setToDelete] = useState<UserAdmin | null>(null);

  const list = useQuery({
    queryKey: ["users"],
    queryFn: () => usersService.list({ page: 0, size: 200 }),
  });

  const toggle = useMutation({
    mutationFn: (id: number) => usersService.toggleActive(id),
    onSuccess: () => {
      notifySuccess(t("users.updateSuccess"));
      qc.invalidateQueries({ queryKey: ["users"] });
    },
    onError: (err) => notifyError(getErrorMessage(err)),
  });

  const saveRoles = useMutation({
    mutationFn: () => (editing ? usersService.updateRoles(editing.id, roles) : Promise.resolve()),
    onSuccess: () => {
      notifySuccess(t("users.updateSuccess"));
      qc.invalidateQueries({ queryKey: ["users"] });
      setEditing(null);
    },
    onError: (err) => notifyError(getErrorMessage(err)),
  });

  const del = useMutation({
    mutationFn: (id: number) => usersService.delete(id),
    onSuccess: () => {
      notifySuccess(t("users.deleteSuccess"));
      qc.invalidateQueries({ queryKey: ["users"] });
    },
    onError: (err) => notifyError(getErrorMessage(err)),
  });

  const columns: GridColDef<UserAdmin>[] = [
    { field: "username", headerName: t("users.user"), flex: 1 },
    { field: "email", headerName: t("users.email"), flex: 1.3 },
    {
      field: "roles",
      headerName: t("users.roles"),
      flex: 1,
      renderCell: (p) => (
        <Stack direction="row" spacing={0.5} sx={{ flexWrap: "wrap", gap: 0.5 }}>
          {(p.value as Role[] | undefined)?.map((r) => (
            <Chip
              key={r}
              label={r.replace("ROLE_", "")}
              size="small"
              sx={{ fontWeight: "bold" }}
              color={r === "ROLE_ADMIN" ? "error" : r === "ROLE_MANAGER" ? "warning" : "primary"}
            />
          ))}
        </Stack>
      ),
    },
    {
      field: "active",
      headerName: t("users.active"),
      width: 100,
      renderCell: (p) => (
        <Switch
          checked={!!(p.row.active ?? p.row.enabled)}
          onChange={() => toggle.mutate(p.row.id)}
          size="small"
          color="secondary"
        />
      ),
    },
    {
      field: "actions",
      headerName: "",
      width: 120,
      sortable: false,
      renderCell: (p) => (
        <Stack direction="row" spacing={0.5}>
          <Tooltip title={t("users.editTip")}>
            <IconButton
              size="small"
              sx={{ color: "#8b5cf6" }}
              onClick={() => {
                setEditing(p.row);
                setRoles(p.row.roles ?? []);
              }}
            >
              <SecurityIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title={t("users.deleteTip")}>
            <IconButton size="small" color="error" onClick={() => setToDelete(p.row)}>
              <DeleteIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Stack>
      ),
    },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-pink-400">
          {t("users.title")}
        </h1>
        <p className="text-gray-400 mt-1 font-light">{t("users.subtitle")}</p>
      </div>

      <div className="glass rounded-2xl overflow-hidden p-2">
        <DataGrid
          autoHeight
          rows={list.data?.content?.filter((row) => row.id != null) ?? []}
          columns={columns}
          loading={list.isLoading}
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

      <Dialog open={!!editing} onClose={() => setEditing(null)} fullWidth maxWidth="xs">
        <DialogTitle sx={{ fontWeight: 700 }}>{t("users.editTip")}</DialogTitle>
        <DialogContent>
          <TextField
            select
            label={t("users.roles")}
            value={roles}
            onChange={(e) =>
              setRoles(
                typeof e.target.value === "string"
                  ? (e.target.value.split(",") as Role[])
                  : (e.target.value as unknown as Role[]),
              )
            }
            slotProps={{ select: { multiple: true } }}
            sx={{ mt: 1, width: "100%" }}
          >
            {allRoles.map((r) => (
              <MenuItem key={r} value={r}>
                {r.replace("ROLE_", "")}
              </MenuItem>
            ))}
          </TextField>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setEditing(null)}>{t("users.cancel")}</Button>
          <Button
            variant="contained"
            onClick={() => saveRoles.mutate()}
            disabled={saveRoles.isPending}
          >
            {t("users.save")}
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmDialog
        open={!!toDelete}
        title={t("users.deleteTip")}
        onClose={() => setToDelete(null)}
        onConfirm={() => {
          if (toDelete) {
            del.mutate(toDelete.id);
            setToDelete(null);
          }
        }}
      />
    </div>
  );
}
