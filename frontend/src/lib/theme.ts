import { createTheme } from "@mui/material/styles";

export const theme = createTheme({
  palette: {
    mode: "light",
    primary: { main: "#3b82f6", dark: "#0f1b3d", contrastText: "#ffffff" },
    secondary: { main: "#0f1b3d" },
    background: { default: "#f7f9fc", paper: "#ffffff" },
    text: { primary: "#0f1b3d", secondary: "#475569" },
    divider: "#e8ecf1",
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
          backgroundColor: "#ffffff",
          color: "#0f1b3d",
          boxShadow: "0 1px 0 #e8ecf1",
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          border: "1px solid #e8ecf1",
          boxShadow: "0 1px 2px rgba(15,27,61,0.04)",
        },
      },
    },
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: { root: { borderRadius: 10, paddingInline: 16 } },
    },
    MuiPaper: { defaultProps: { elevation: 0 } },
    MuiTextField: { defaultProps: { size: "small", fullWidth: true } },
  },
});
