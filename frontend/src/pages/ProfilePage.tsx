import { useEffect, useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Alert, Button, Chip, Stack } from "@mui/material";
import { authService } from "@/services";
import { useAuth } from "@/context/auth";
import { notifySuccess, notifyError, getErrorMessage } from "@/utils/notifications";
import { useTranslation } from "react-i18next";

export function ProfilePage() {
  const { t } = useTranslation();
  const { user, refresh } = useAuth();
  const [form, setForm] = useState({ username: "", email: "" });
  const [passwordForm, setPasswordForm] = useState({ oldPassword: "", newPassword: "", confirmPassword: "" });
  const [msg, setMsg] = useState<string | null>(null);

  useEffect(() => {
    if (user) setForm({ username: user.username, email: user.email });
  }, [user]);

  const update = useMutation({
    mutationFn: () => authService.updateProfile(form),
    onSuccess: async () => {
      notifySuccess(t("profile.success"));
      setMsg(null);
      await refresh();
    },
    onError: (err) => {
      const errMsg = getErrorMessage(err);
      notifyError(errMsg);
    },
  });

  const updatePassword = useMutation({
    mutationFn: () => authService.changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword }),
    onSuccess: () => {
      notifySuccess(t("profile.passwordSuccess"));
      setPasswordForm({ oldPassword: "", newPassword: "", confirmPassword: "" });
    },
    onError: (err) => {
      const errMsg = getErrorMessage(err);
      notifyError(errMsg);
    },
  });

  return (
    <div className="space-y-6 animate-in fade-in duration-500 max-w-2xl mx-auto mt-10">
      <div className="mb-6 text-center">
        <div className="w-24 h-24 rounded-full bg-gradient-to-tr from-blue-500 to-blue-600 flex items-center justify-center text-white text-4xl font-bold shadow-lg mx-auto mb-4">
          {user?.username?.[0]?.toUpperCase() ?? "?"}
        </div>
        <h1 className="text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-blue-400">
          {t("profile.title")}
        </h1>
        <p className="text-gray-400 mt-1 font-light">{t("profile.subtitle")}</p>
      </div>

      <div className="glass rounded-3xl p-8 shadow-2xl border border-white/10">
        <Stack spacing={4}>
          <div>
            <p className="text-sm text-gray-400 mb-2 font-medium">{t("profile.roles")}</p>
            <Stack direction="row" spacing={1} sx={{ flexWrap: "wrap", gap: 1 }}>
              {user?.roles?.map((r) => (
                <Chip
                  key={r}
                  label={r.replace("ROLE_", "")}
                  sx={{
                    fontWeight: "bold",
                    bgcolor:
                      r === "ROLE_ADMIN"
                        ? "rgba(239, 68, 68, 0.2)"
                        : r === "ROLE_MANAGER"
                          ? "rgba(245, 158, 11, 0.2)"
                          : "rgba(59, 130, 246, 0.2)",
                    color:
                      r === "ROLE_ADMIN" ? "#fca5a5" : r === "ROLE_MANAGER" ? "#fcd34d" : "#93c5fd",
                    border: "1px solid",
                    borderColor:
                      r === "ROLE_ADMIN"
                        ? "rgba(239, 68, 68, 0.3)"
                        : r === "ROLE_MANAGER"
                          ? "rgba(245, 158, 11, 0.3)"
                          : "rgba(59, 130, 246, 0.3)",
                  }}
                />
              ))}
            </Stack>
          </div>

          {msg && (
            <Alert
              severity="success"
              sx={{
                bgcolor: "rgba(16, 185, 129, 0.1)",
                color: "#6ee7b7",
                border: "1px solid rgba(16, 185, 129, 0.2)",
              }}
            >
              {msg}
            </Alert>
          )}

          <div className="space-y-5">
            <div className="flex flex-col space-y-1.5">
              <label className="text-sm text-slate-400 font-medium ml-1">{t("profile.username")}</label>
              <input
                type="text"
                value={form.username}
                onChange={(e) => setForm({ ...form, username: e.target.value })}
                className="bg-slate-100 dark:bg-white/5 border border-slate-200 dark:border-white/10 rounded-xl px-4 py-3 text-slate-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
              />
            </div>
            
            <div className="flex flex-col space-y-1.5">
              <label className="text-sm text-slate-400 font-medium ml-1">{t("profile.email")}</label>
              <input
                type="email"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                className="bg-slate-100 dark:bg-white/5 border border-slate-200 dark:border-white/10 rounded-xl px-4 py-3 text-slate-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
              />
            </div>
          </div>

          <Button
            variant="contained"
            onClick={() => update.mutate()}
            disabled={update.isPending}
            size="large"
            sx={{
              background: "linear-gradient(to right, #3b82f6, #8b5cf6)",
              borderRadius: 2,
              py: 1.5,
              fontWeight: "bold",
              "&:hover": { opacity: 0.9 },
              mt: 2,
            }}
          >
            {t("profile.update")}
          </Button>
        </Stack>
      </div>

      <div className="glass rounded-3xl p-8 shadow-2xl border border-white/10 mt-6">
        <h2 className="text-xl font-bold text-white mb-4">{t("profile.changePassword")}</h2>
        <Stack spacing={4}>
          <div className="space-y-5">
            <div className="flex flex-col space-y-1.5">
              <label className="text-sm text-slate-400 font-medium ml-1">{t("profile.oldPassword")}</label>
              <input
                type="password"
                value={passwordForm.oldPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, oldPassword: e.target.value })}
                className="bg-slate-100 dark:bg-white/5 border border-slate-200 dark:border-white/10 rounded-xl px-4 py-3 text-slate-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
              />
            </div>
            
            <div className="flex flex-col space-y-1.5">
              <label className="text-sm text-slate-400 font-medium ml-1">{t("profile.newPassword")}</label>
              <input
                type="password"
                value={passwordForm.newPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                className="bg-slate-100 dark:bg-white/5 border border-slate-200 dark:border-white/10 rounded-xl px-4 py-3 text-slate-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
              />
            </div>

            <div className="flex flex-col space-y-1.5">
              <label className="text-sm text-slate-400 font-medium ml-1">{t("profile.confirmPassword")}</label>
              <input
                type="password"
                value={passwordForm.confirmPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                className="bg-slate-100 dark:bg-white/5 border border-slate-200 dark:border-white/10 rounded-xl px-4 py-3 text-slate-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
              />
            </div>
          </div>

          <Button
            variant="contained"
            onClick={() => {
              if (passwordForm.newPassword !== passwordForm.confirmPassword) {
                notifyError(t("profile.passwordMismatch"));
                return;
              }
              if (passwordForm.newPassword.length < 6) {
                notifyError(t("profile.passwordTooShort"));
                return;
              }
              updatePassword.mutate();
            }}
            disabled={updatePassword.isPending || !passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword}
            size="large"
            sx={{
              background: "linear-gradient(to right, #10b981, #059669)",
              borderRadius: 2,
              py: 1.5,
              fontWeight: "bold",
              "&:hover": { opacity: 0.9 },
              mt: 2,
            }}
          >
            {t("profile.updatePasswordBtn")}
          </Button>
        </Stack>
      </div>
    </div>
  );
}
