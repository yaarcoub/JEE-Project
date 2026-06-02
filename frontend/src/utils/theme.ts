import { createTheme, type Theme } from "@mui/material/styles";
import { frFR as coreFrFR, enUS as coreEnUS } from "@mui/material/locale";
import { frFR as dataGridFrFR, enUS as dataGridEnUS } from "@mui/x-data-grid/locales";

export const getTheme = (mode: "light" | "dark", lang: string): Theme =>
  createTheme({
    palette: {
      mode,
      primary: { main: "#3b82f6", dark: "#2563eb", contrastText: "#ffffff" },
      secondary: { main: mode === "light" ? "#0f1b3d" : "#cbd5e1" },
      background:
        mode === "light"
          ? { default: "#f7f9fc", paper: "#ffffff" }
          : { default: "#020617", paper: "#0f172a" }, // slate-950 and slate-900
      text:
        mode === "light"
          ? { primary: "#0f1b3d", secondary: "#475569" }
          : { primary: "#f8fafc", secondary: "#94a3b8" },
      divider: mode === "light" ? "#e8ecf1" : "rgba(255,255,255,0.1)",
      success: { main: "#10b981" },
      warning: { main: "#f59e0b" },
      error: { main: "#ef4444" },
      info: { main: "#3b82f6" },
    },
    shape: { borderRadius: 12 },
    typography: {
      fontFamily: '"Inter","-apple-system",BlinkMacSystemFont,"Segoe UI",Roboto,sans-serif',
      h1: { fontWeight: 700, letterSpacing: "-0.02em" },
      h2: { fontWeight: 700, letterSpacing: "-0.02em" },
      h3: { fontWeight: 700, letterSpacing: "-0.01em" },
      h4: { fontWeight: 700, letterSpacing: "-0.01em" },
      h5: { fontWeight: 600 },
      h6: { fontWeight: 600 },
      button: { textTransform: "none", fontWeight: 600 },
    },
    components: {
      MuiAppBar: {
        styleOverrides: {
          root: {
            backgroundColor: mode === "light" ? "#ffffff" : "#0f172a",
            color: mode === "light" ? "#0f1b3d" : "#f8fafc",
            boxShadow: mode === "light" ? "0 1px 0 #e8ecf1" : "0 1px 0 rgba(255,255,255,0.1)",
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            borderRadius: 16,
            border: mode === "light" ? "1px solid #e8ecf1" : "1px solid rgba(255,255,255,0.1)",
            boxShadow: mode === "light" ? "0 1px 2px rgba(15,27,61,0.04)" : "none",
            backgroundImage: "none",
          },
        },
      },
      MuiButton: {
        defaultProps: { disableElevation: true },
        styleOverrides: { root: { borderRadius: 10, paddingInline: 16 } },
      },
      MuiPaper: {
        defaultProps: { elevation: 0 },
        styleOverrides: { root: { backgroundImage: "none" } },
      },
      MuiTextField: { defaultProps: { size: "small", fullWidth: true } },
    },
  },
  lang.startsWith('fr') ? coreFrFR : coreEnUS,
  lang.startsWith('fr') ? dataGridFrFR : dataGridEnUS
  );
