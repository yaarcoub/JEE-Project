import { useQuery } from "@tanstack/react-query";
import { CircularProgress } from "@mui/material";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip as RechartsTooltip,
  ResponsiveContainer,
  CartesianGrid,
  PieChart,
  Pie,
  Cell,
  Legend,
} from "recharts";
import BookIcon from "@mui/icons-material/MenuBook";
import PeopleIcon from "@mui/icons-material/People";
import LoanIcon from "@mui/icons-material/SyncAlt";
import WarningIcon from "@mui/icons-material/WarningAmber";
import TrendingUpIcon from "@mui/icons-material/TrendingUp";
import CalendarTodayIcon from "@mui/icons-material/CalendarToday";
import InboxIcon from "@mui/icons-material/Inbox";
import { dashboardService, loansService } from "@/services";
import { useTheme } from "@/hooks/useTheme";
import { useTranslation } from "react-i18next";

const COLORS = ["#8b5cf6", "#3b82f6", "#10b981", "#f59e0b", "#ec4899", "#ef4444", "#06b6d4"];

interface StatCardProps {
  label: string;
  value: number | string;
  icon: React.ReactNode;
  gradient: string;
  trend?: string;
}

function StatCard({ label, value, icon, gradient, trend }: StatCardProps) {
  return (
    <div className="bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border border-slate-200 dark:border-white/10 rounded-2xl p-5 flex items-center space-x-4 transform transition-all duration-300 hover:-translate-y-1 hover:shadow-xl relative overflow-hidden group">
      <div
        className={`absolute -right-6 -top-6 w-24 h-24 rounded-full opacity-10 dark:opacity-20 bg-gradient-to-br ${gradient} blur-xl group-hover:blur-2xl transition-all duration-500`}
      ></div>
      <div
        className={`w-12 h-12 rounded-xl bg-gradient-to-br ${gradient} flex items-center justify-center text-white shadow-md z-10 shrink-0`}
      >
        {icon}
      </div>
      <div className="z-10 flex-1">
        <p className="text-slate-500 dark:text-gray-400 text-xs font-semibold uppercase tracking-wider">
          {label}
        </p>
        <div className="flex items-baseline space-x-2 mt-1">
          <p className="text-2xl font-extrabold text-slate-800 dark:text-gray-100">{value}</p>
          {trend && (
            <span className="text-[10px] font-medium text-emerald-500 flex items-center">
              <TrendingUpIcon sx={{ fontSize: 12 }} className="mr-0.5" /> {trend}
            </span>
          )}
        </div>
      </div>
    </div>
  );
}

export function AdminDashboard() {
  const { theme } = useTheme();
  const { t, i18n } = useTranslation();

  const { data, isLoading, error } = useQuery({
    queryKey: ["dashboard-stats"],
    queryFn: () => dashboardService.getStats(),
  });

  const { data: recentLoansData } = useQuery({
    queryKey: ["recent-loans"],
    queryFn: () => loansService.list({ size: 5 }),
  });

  const isDark = theme === "dark";
  const tooltipStyle = {
    backgroundColor: isDark ? "rgba(15, 23, 42, 0.95)" : "rgba(255, 255, 255, 0.95)",
    borderRadius: "8px",
    border: isDark ? "1px solid rgba(255,255,255,0.1)" : "1px solid rgba(0,0,0,0.1)",
    color: isDark ? "#fff" : "#0f1b3d",
    boxShadow: "0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)",
    fontSize: "12px",
  };

  const currentDate = new Date().toLocaleDateString(i18n.resolvedLanguage === "en" ? "en-US" : "fr-FR", {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  });

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <CircularProgress sx={{ color: "#3b82f6" }} />
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="bg-red-500/10 p-6 rounded-2xl text-red-500 border border-red-500/20 text-center text-sm font-medium">
        Impossible de charger les statistiques. Vérifiez que le backend est lancé.
      </div>
    );
  }

  const recentLoans = recentLoansData?.content || [];

  return (
    <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-700">
      <div className="mb-6 flex flex-col md:flex-row md:justify-between md:items-end">
        <div>
          <h1 className="text-3xl font-extrabold text-slate-800 dark:text-white tracking-tight">
            {t("nav.dashboard")}
          </h1>
          <p className="text-slate-500 dark:text-gray-400 mt-1 text-sm font-medium">
            {t("dashboard.today")}
          </p>
        </div>
        <div className="mt-4 md:mt-0 flex items-center text-sm font-medium text-slate-500 dark:text-slate-400 bg-white/60 dark:bg-slate-800/60 backdrop-blur-md px-4 py-2 rounded-lg border border-slate-200 dark:border-white/5">
          <CalendarTodayIcon fontSize="small" className="mr-2 text-blue-500" />
          <span className="capitalize">{currentDate}</span>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          label={t("dashboard.books")}
          value={data.totalBooks}
          icon={<BookIcon />}
          gradient="from-blue-600 to-cyan-500"
          trend="+12%"
        />
        <StatCard
          label={t("dashboard.users")}
          value={data.totalUsers}
          icon={<PeopleIcon />}
          gradient="from-indigo-600 to-purple-500"
          trend="+5%"
        />
        <StatCard
          label={t("dashboard.activeLoans")}
          value={data.activeLoans}
          icon={<LoanIcon />}
          gradient="from-emerald-500 to-teal-400"
        />
        <StatCard
          label={t("dashboard.overdue")}
          value={data.overdueLoans}
          icon={<WarningIcon />}
          gradient="from-rose-500 to-orange-500"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
        <div className="bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border border-slate-200 dark:border-white/10 rounded-2xl p-5 transform transition-all duration-500 flex flex-col">
          <h2 className="text-lg font-bold text-slate-800 dark:text-gray-100 mb-4 flex items-center">
            <span className="w-1.5 h-6 rounded-full bg-gradient-to-b from-blue-500 to-indigo-500 mr-2"></span>
            {t("dashboard.distribution")}
          </h2>
          {data.booksByCategory && data.booksByCategory.length > 0 ? (
            <div className="flex-1 min-h-[250px] relative">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={data.booksByCategory}
                    dataKey="bookCount"
                    nameKey="category"
                    cx="50%"
                    cy="45%"
                    innerRadius={60}
                    outerRadius={90}
                    paddingAngle={3}
                    stroke="none"
                  >
                    {data.booksByCategory.map((_, i) => (
                      <Cell
                        key={i}
                        fill={COLORS[i % COLORS.length]}
                        className="hover:opacity-80 transition-opacity cursor-pointer outline-none"
                      />
                    ))}
                  </Pie>
                  <RechartsTooltip
                    contentStyle={tooltipStyle}
                    itemStyle={{
                      color: isDark ? "#fff" : "#0f1b3d",
                      fontWeight: 600,
                      fontSize: "12px",
                    }}
                  />
                  <Legend
                    wrapperStyle={{
                      color: isDark ? "#cbd5e1" : "#475569",
                      fontWeight: 500,
                      bottom: 0,
                      fontSize: "11px",
                    }}
                    iconType="circle"
                  />
                </PieChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center text-slate-400 text-sm">
              <InboxIcon sx={{ fontSize: 48, opacity: 0.3 }} className="mb-2" />
              <p>{t("dashboard.noData")}</p>
            </div>
          )}
        </div>

        <div className="bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border border-slate-200 dark:border-white/10 rounded-2xl p-5 transform transition-all duration-500 flex flex-col">
          <h2 className="text-lg font-bold text-slate-800 dark:text-gray-100 mb-4 flex items-center">
            <span className="w-1.5 h-6 rounded-full bg-gradient-to-b from-emerald-500 to-teal-500 mr-2"></span>
            {t("dashboard.statistics")}
          </h2>
          {data.loansByMonth && data.loansByMonth.length > 0 ? (
            <div className="flex-1 min-h-[250px]">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart
                  data={data.loansByMonth}
                  margin={{ top: 10, right: 10, left: -25, bottom: 0 }}
                >
                  <CartesianGrid
                    strokeDasharray="3 3"
                    stroke={isDark ? "rgba(255,255,255,0.05)" : "rgba(0,0,0,0.05)"}
                    vertical={false}
                  />
                  <XAxis
                    dataKey="month"
                    stroke={isDark ? "#94a3b8" : "#64748b"}
                    axisLine={false}
                    tickLine={false}
                    tick={{ fill: isDark ? "#94a3b8" : "#64748b", fontWeight: 500, fontSize: 11 }}
                    dy={10}
                  />
                  <YAxis
                    stroke={isDark ? "#94a3b8" : "#64748b"}
                    axisLine={false}
                    tickLine={false}
                    tick={{ fill: isDark ? "#94a3b8" : "#64748b", fontWeight: 500, fontSize: 11 }}
                  />
                  <RechartsTooltip
                    cursor={{ fill: isDark ? "rgba(255,255,255,0.05)" : "rgba(0,0,0,0.03)" }}
                    contentStyle={tooltipStyle}
                  />
                  <Bar dataKey="loans" radius={[4, 4, 0, 0]} maxBarSize={40}>
                    {data.loansByMonth.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[(index + 2) % COLORS.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center text-slate-400 text-sm">
              <InboxIcon sx={{ fontSize: 48, opacity: 0.3 }} className="mb-2" />
              <p>{t("dashboard.noLoans")}</p>
            </div>
          )}
        </div>
      </div>

      <div className="bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border border-slate-200 dark:border-white/10 rounded-2xl p-5 mt-6 overflow-hidden">
        <h2 className="text-lg font-bold text-slate-800 dark:text-gray-100 mb-4 flex items-center">
          <span className="w-1.5 h-6 rounded-full bg-gradient-to-b from-purple-500 to-pink-500 mr-2"></span>
          {t("dashboard.recentLoans")}
        </h2>

        {recentLoans.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="text-xs text-slate-500 dark:text-slate-400 uppercase bg-slate-50/50 dark:bg-slate-800/50">
                <tr>
                  <th className="px-4 py-3 font-medium rounded-tl-lg">{t("dashboard.book")}</th>
                  <th className="px-4 py-3 font-medium">{t("dashboard.user")}</th>
                  <th className="px-4 py-3 font-medium">{t("dashboard.date")}</th>
                  <th className="px-4 py-3 font-medium text-right rounded-tr-lg">{t("dashboard.status")}</th>
                </tr>
              </thead>
              <tbody>
                {recentLoans.map((loan) => (
                  <tr
                    key={loan.id}
                    className="border-b border-slate-100 dark:border-slate-800/50 last:border-0 hover:bg-slate-50/50 dark:hover:bg-slate-800/30 transition-colors"
                  >
                    <td className="px-4 py-3 font-medium text-slate-800 dark:text-slate-200">
                      {loan.book.title}
                    </td>
                    <td className="px-4 py-3 text-slate-600 dark:text-slate-400">
                      {loan.user.username}
                    </td>
                    <td className="px-4 py-3 text-slate-500 dark:text-slate-500">
                      {new Date(loan.loanDate).toLocaleDateString("fr-FR")}
                    </td>
                    <td className="px-4 py-3 text-right">
                      <span
                        className={`inline-flex items-center px-2 py-1 rounded-md text-[10px] font-bold uppercase tracking-wider ${
                          loan.status === "BORROWED"
                            ? "bg-blue-100 text-blue-700 dark:bg-blue-500/20 dark:text-blue-400"
                            : loan.status === "RETURNED"
                              ? "bg-emerald-100 text-emerald-700 dark:bg-emerald-500/20 dark:text-emerald-400"
                              : "bg-rose-100 text-rose-700 dark:bg-rose-500/20 dark:text-rose-400"
                        }`}
                      >
                        {loan.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center text-slate-400 text-sm py-8">
            <InboxIcon sx={{ fontSize: 40, opacity: 0.3 }} className="mb-2" />
            <p>{t("dashboard.noLoans")}</p>
          </div>
        )}
      </div>
    </div>
  );
}
