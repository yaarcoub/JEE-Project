import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from "@mui/material";
import type { ReactNode } from "react";

export function ConfirmDialog({
  open,
  title,
  message = "Êtes-vous sûr ?",
  confirmLabel = "Confirmer",
  onClose,
  onConfirm,
  destructive = true,
}: {
  open: boolean;
  title: string;
  message?: ReactNode;
  confirmLabel?: string;
  onClose: () => void;
  onConfirm: () => void;
  destructive?: boolean;
}) {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle sx={{ fontWeight: 700 }}>{title}</DialogTitle>
      <DialogContent>{message}</DialogContent>
      <DialogActions sx={{ p: 2 }}>
        <Button onClick={onClose}>Annuler</Button>
        <Button variant="contained" color={destructive ? "error" : "primary"} onClick={onConfirm}>
          {confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
