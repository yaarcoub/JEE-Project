import { useState, type ReactNode } from "react";
import { Link, useRouterState } from "@tanstack/react-router";
import { useAuth } from "@/context/auth";
import type { Role } from "@/services";

import DashboardIcon from "@mui/icons-material/SpaceDashboard";
import BookIcon from "@mui/icons-material/MenuBook";
import LoanIcon from "@mui/icons-material/SyncAlt";
import MyLoansIcon from "@mui/icons-material/Bookmarks";
import CategoryIcon from "@mui/icons-material/Category";
import PeopleIcon from "@mui/icons-material/People";
import LogoutIcon from "@mui/icons-material/Logout";
import PersonIcon from "@mui/icons-material/Person";
import MenuIcon from "@mui/icons-material/Menu";
import CloseIcon from "@mui/icons-material/Close";
import TranslateIcon from "@mui/icons-material/Translate";
import ChevronLeftIcon from "@mui/icons-material/ChevronLeft";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";
import { Tooltip } from "@mui/material";
import { useTranslation } from "react-i18next";
import { Footer } from "./Footer";
import { useTheme } from "@/hooks/useTheme";

const DRAWER_WIDTH = 280;

interface NavItem {
  to: string;
  labelKey: string;
  icon: ReactNode;
  roles?: Role[];
}

const navItems: NavItem[] = [
  {
    to: "/dashboard",
    labelKey: "nav.dashboard",
    icon: <DashboardIcon />,
  },
  { to: "/books", labelKey: "nav.books", icon: <BookIcon /> },
  { to: "/loans/my", labelKey: "nav.myLoans", icon: <MyLoansIcon /> },
  { to: "/loans", labelKey: "nav.loans", icon: <LoanIcon />, roles: ["ROLE_ADMIN", "ROLE_MANAGER"] },
  { to: "/categories", labelKey: "nav.categories", icon: <CategoryIcon /> },
  { to: "/users", labelKey: "nav.users", icon: <PeopleIcon />, roles: ["ROLE_ADMIN"] },
];

function roleColor(r: Role) {
  if (r === "ROLE_ADMIN") return "bg-red-500/20 text-red-300 border-red-500/30";
  if (r === "ROLE_MANAGER") return "bg-amber-500/20 text-amber-300 border-amber-500/30";
  return "bg-blue-500/20 text-blue-300 border-blue-500/30";
}

export function AppLayout({ children }: { children: ReactNode }) {
  const { user, hasRole, logout } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [isCollapsed, setIsCollapsed] = useState(true);
  const [profileOpen, setProfileOpen] = useState(false);
  const path = useRouterState({ select: (s) => s.location.pathname });
  const { t, i18n } = useTranslation();
  const { toggleTheme } = useTheme();

  const filtered = navItems.filter((i) => !i.roles || i.roles.some((r) => hasRole(r)));

  const SidebarContent = () => (
    <div className="flex flex-col h-full bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border-r border-slate-200 dark:border-white/10 text-slate-800 dark:text-white shadow-2xl transition-colors duration-300 relative">
      <div className={`p-4 flex items-center ${isCollapsed ? "justify-center" : "space-x-3"} h-16`}>
        <div className="w-8 h-8 shrink-0 rounded-lg bg-gradient-to-tr from-blue-500 to-blue-600 flex items-center justify-center text-white shadow-lg">
          <BookIcon fontSize="small" />
        </div>
        {!isCollapsed && (
          <div className="overflow-hidden whitespace-nowrap animate-in fade-in">
            <h2 className="text-lg font-bold tracking-tight text-blue-600 dark:text-blue-400">
              Library
            </h2>
            <p className="text-[10px] text-gray-400 font-medium">{t("nav.premium")}</p>
          </div>
        )}
      </div>

      <button
        onClick={() => setIsCollapsed(!isCollapsed)}
        className="absolute -right-3 top-6 bg-white dark:bg-slate-800 border border-slate-200 dark:border-white/10 text-slate-500 dark:text-gray-400 rounded-full p-1 shadow-sm hover:text-blue-500 z-50 hidden lg:flex"
      >
        {isCollapsed ? <ChevronRightIcon fontSize="small" /> : <ChevronLeftIcon fontSize="small" />}
      </button>

      <div className="px-4 py-2 flex-1 overflow-y-auto space-y-1">
        {filtered.map((item) => {
          let active = false;
          if (item.to === "/loans") {
            active = path === "/loans" || (path.startsWith("/loans/") && !path.startsWith("/loans/my"));
          } else {
            active = path === item.to || path.startsWith(item.to + "/");
          }
          
          return (
            <Tooltip
              key={item.to}
              title={isCollapsed ? t(item.labelKey) : ""}
              placement="right"
              arrow
            >
              <Link
                to={item.to}
                onClick={() => setMobileOpen(false)}
                className={`flex items-center ${isCollapsed ? "justify-center px-2" : "px-3"} py-2.5 rounded-lg transition-all duration-300 group relative ${
                  active
                    ? "bg-gradient-to-r from-blue-600/80 to-blue-600/80 text-white shadow-md shadow-blue-900/20"
                    : "text-slate-500 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-white/5 dark:hover:text-white"
                }`}
              >
                <div
                  className={`${isCollapsed ? "" : "mr-3"} transition-transform duration-300 ${active ? "scale-110" : "group-hover:scale-110"}`}
                >
                  <span className="scale-90 inline-block">{item.icon}</span>
                </div>
                {!isCollapsed && (
                  <span className="font-medium text-sm whitespace-nowrap">{t(item.labelKey)}</span>
                )}
              </Link>
            </Tooltip>
          );
        })}
      </div>

      <div className="p-4 border-t border-white/10 bg-black/20">
        <div
          className={`flex items-center ${isCollapsed ? "justify-center" : "p-2"} rounded-lg hover:bg-slate-100 dark:hover:bg-white/5 transition-colors cursor-pointer`}
          onClick={() => setProfileOpen(!profileOpen)}
        >
          <div className="w-8 h-8 shrink-0 rounded-full bg-gradient-to-r from-indigo-500 to-blue-500 flex items-center justify-center text-white text-xs font-bold shadow-sm">
            {user?.username?.[0]?.toUpperCase() ?? "?"}
          </div>
          {!isCollapsed && (
            <div className="ml-3 overflow-hidden">
              <p className="text-sm font-semibold text-slate-800 dark:text-white truncate">
                {user?.username}
              </p>
              <p className="text-[10px] text-gray-500 dark:text-gray-400 truncate">{user?.email}</p>
            </div>
          )}
        </div>

        {/* Profile Dropdown Simulation */}
        {profileOpen && (
          <div className="mt-2 p-2 bg-slate-800/90 backdrop-blur-md rounded-xl border border-white/10 shadow-xl absolute bottom-[80px] w-[248px] animate-in slide-in-from-bottom-2 fade-in z-50">
            <Link
              to="/profile"
              onClick={() => setProfileOpen(false)}
              className="flex items-center px-3 py-2 text-sm text-gray-300 hover:text-white hover:bg-white/10 rounded-lg transition-colors"
            >
              <PersonIcon className="mr-2" fontSize="small" /> {t("nav.profile")}
            </Link>
            <button
              onClick={logout}
              className="w-full flex items-center px-3 py-2 text-sm text-red-400 hover:text-red-300 hover:bg-red-500/10 rounded-lg transition-colors mt-1"
            >
              <LogoutIcon className="mr-2" fontSize="small" /> {t("nav.logout")}
            </button>
          </div>
        )}
      </div>
    </div>
  );

  return (
    <div className="flex min-h-screen bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-50 selection:bg-blue-500/30 transition-colors duration-300">
      {/* Mobile Sidebar Overlay */}
      {mobileOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm transition-opacity lg:hidden"
          onClick={() => setMobileOpen(false)}
        />
      )}

      {/* Profile Click-Away Overlay (Covers the entire screen) */}
      {profileOpen && (
        <div
          className="fixed inset-0 z-40"
          onClick={() => setProfileOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-50 ${isCollapsed ? "w-[80px]" : "w-[280px]"} transform transition-all duration-300 ease-in-out lg:translate-x-0 ${
          mobileOpen ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <SidebarContent />
      </aside>

      {/* Main Content */}
      <main
        className={`flex-1 flex flex-col min-w-0 transition-all duration-300 ${isCollapsed ? "lg:pl-[80px]" : "lg:pl-[280px]"}`}
      >
        {/* Header */}
        <header className="sticky top-0 z-30 flex items-center justify-between h-20 px-6 lg:px-10 bg-white/50 dark:bg-slate-900/50 backdrop-blur-xl border-b border-slate-200 dark:border-white/5 shadow-sm transition-colors duration-300">
          <div className="flex items-center">
            <button
              className="p-2 mr-4 text-slate-500 dark:text-gray-400 hover:text-slate-900 dark:hover:text-white lg:hidden bg-slate-200/50 dark:bg-white/5 rounded-lg"
              onClick={() => setMobileOpen(true)}
            >
              <MenuIcon />
            </button>
            <h1 className="text-xl font-semibold text-slate-800 dark:text-white tracking-tight hidden sm:block">
              {(() => {
                const activeItem = filtered.find((i) => path === i.to || (path.startsWith(i.to + "/") && i.to !== "/loans"));
                return activeItem ? t(activeItem.labelKey) : "Library";
              })()}
            </h1>
          </div>

          <div className="flex items-center space-x-3">
            <button
              onClick={() => {
                const current = (i18n.resolvedLanguage || i18n.language || "fr").substring(0, 2).toLowerCase();
                const next = current === "fr" ? "en" : "fr";
                i18n.changeLanguage(next);
                document.documentElement.dir = "ltr"; // English and French are LTR
              }}
              className="p-2 text-xs font-bold uppercase rounded-full bg-slate-200 dark:bg-white/10 hover:bg-slate-300 dark:hover:bg-white/20 text-slate-800 dark:text-white transition-colors flex items-center justify-center"
            >
              <TranslateIcon fontSize="small" className="mr-1" />
              {(i18n.resolvedLanguage || i18n.language || "fr").substring(0, 2).toUpperCase()}
            </button>
            <button
              onClick={toggleTheme}
              className="p-2 rounded-full bg-slate-200 dark:bg-white/10 hover:bg-slate-300 dark:hover:bg-white/20 text-slate-800 dark:text-white transition-colors"
            >
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"
                />
              </svg>
            </button>
          </div>
        </header>

        <div className="flex-1 p-4 sm:p-6 lg:p-10 w-full relative z-0">
          <div className="relative z-10 animate-in fade-in duration-500 min-h-[calc(100vh-200px)]">
            {children}
          </div>
        </div>
        <Footer />
      </main>
    </div>
  );
}
