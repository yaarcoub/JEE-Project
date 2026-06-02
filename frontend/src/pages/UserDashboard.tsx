import React, { useState, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { notifySuccess, notifyError, getErrorMessage } from "@/utils/notifications";
import { CircularProgress, Button, Dialog, DialogTitle, DialogContent, DialogActions, TextField } from "@mui/material";
import type { Book } from "@/services/types";
import { Link } from "@tanstack/react-router";
import BookIcon from "@mui/icons-material/MenuBook";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import WarningIcon from "@mui/icons-material/WarningAmber";
import CalendarTodayIcon from "@mui/icons-material/CalendarToday";
import InboxIcon from "@mui/icons-material/Inbox";
import ArrowForwardIcon from "@mui/icons-material/ArrowForward";
import HistoryIcon from "@mui/icons-material/History";
import { loansService, booksService, dashboardService } from "@/services";
import { useAuth } from "@/context/auth";
import { useTranslation } from "react-i18next";
import { useTheme } from "@/hooks/useTheme";
import {
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as RechartsTooltip,
  ResponsiveContainer,
  Radar,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
} from "recharts";

function UserStatCard({ label, value, icon, gradient }: { label: string; value: number | string; icon: React.ReactNode; gradient: string }) {
  return (
    <div className="bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border border-slate-200 dark:border-white/10 rounded-2xl p-5 flex items-center space-x-4 transform transition-all duration-300 hover:-translate-y-1 hover:shadow-xl relative overflow-hidden group">
      <div className={`absolute -right-6 -top-6 w-24 h-24 rounded-full opacity-10 dark:opacity-20 bg-gradient-to-br ${gradient} blur-xl group-hover:blur-2xl transition-all duration-500`}></div>
      <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${gradient} flex items-center justify-center text-white shadow-md z-10 shrink-0`}>
        {icon}
      </div>
      <div className="z-10 flex-1">
        <p className="text-slate-500 dark:text-gray-400 text-xs font-semibold uppercase tracking-wider">{label}</p>
        <div className="flex items-baseline space-x-2 mt-1">
          <p className="text-2xl font-extrabold text-slate-800 dark:text-gray-100">{value}</p>
        </div>
      </div>
    </div>
  );
}

const UserHistoryTimeline = ({ loans }: { loans: Loan[] }) => {
  const { t } = useTranslation();

  // Create events from loans
  const events: { id: string; type: 'BORROW' | 'RETURN'; date: Date; bookTitle: string; bookAuthor: string }[] = [];
  
  loans.forEach(loan => {
    // Borrow event
    if (loan.loanDate) {
      events.push({
        id: `borrow-${loan.id}`,
        type: 'BORROW',
        date: new Date(loan.loanDate),
        bookTitle: loan.book.title,
        bookAuthor: loan.book.author,
      });
    }
    // Return event
    if (loan.actualReturnDate) {
      events.push({
        id: `return-${loan.id}`,
        type: 'RETURN',
        date: new Date(loan.actualReturnDate),
        bookTitle: loan.book.title,
        bookAuthor: loan.book.author,
      });
    }
  });

  // Sort by date descending
  events.sort((a, b) => b.date.getTime() - a.date.getTime());
  
  // Do not slice, show all events to allow scrolling
  const recentEvents = events;

  if (recentEvents.length === 0) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center text-slate-400 text-sm p-8 bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border border-slate-200 dark:border-white/10 rounded-2xl h-full transform transition-all duration-500">
        <HistoryIcon sx={{ fontSize: 40, opacity: 0.3 }} className="mb-2" />
        <p>{t("dashboard.noRecentHistory")}</p>
      </div>
    );
  }

  return (
    <div className="bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border border-slate-200 dark:border-white/10 rounded-2xl p-5 transform transition-all duration-500 flex flex-col h-full shadow-sm hover:shadow-md">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-lg font-bold text-slate-800 dark:text-gray-100 flex items-center">
          <span className="w-1.5 h-6 rounded-full bg-gradient-to-b from-blue-500 to-indigo-500 mr-2"></span>
          {t("dashboard.activityHistory")}
        </h2>
      </div>
      
      <div className="flex-1 overflow-y-auto pr-2 custom-scrollbar">
        <div className="relative border-l-2 border-slate-200 dark:border-slate-700 ml-3 space-y-6 pb-2">
          {recentEvents.map((event, index) => (
            <div key={event.id} className="relative pl-6 animate-in fade-in slide-in-from-bottom-4" style={{ animationDelay: `${index * 100}ms`, animationFillMode: 'both' }}>
              {/* Dot */}
              <div className={`absolute -left-[9px] top-1 w-4 h-4 rounded-full border-2 border-white dark:border-slate-900 ${
                event.type === 'BORROW' ? 'bg-blue-500' : 'bg-emerald-500'
              }`}></div>
              
              <div className="flex flex-col">
                <span className="text-xs font-bold text-slate-500 dark:text-slate-400 mb-0.5 uppercase tracking-wider">
                  {event.date.toLocaleDateString(undefined, { day: 'numeric', month: 'short', year: 'numeric' })}
                </span>
                <div className="bg-slate-50 dark:bg-slate-800/50 rounded-lg p-3 border border-slate-100 dark:border-white/5 mt-1 shadow-sm hover:border-indigo-500/30 transition-colors">
                  <p className="text-sm font-medium text-slate-800 dark:text-slate-200">
                    {event.type === 'BORROW' ? (
                      <>{t("dashboard.youBorrowed")} <span className="font-bold text-blue-600 dark:text-blue-400">{event.bookTitle}</span></>
                    ) : (
                      <>{t("dashboard.youReturned")} <span className="font-bold text-emerald-600 dark:text-emerald-400">{event.bookTitle}</span></>
                    )}
                  </p>
                  <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">{event.bookAuthor}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export function UserDashboard() {
  const { t, i18n } = useTranslation();
  const { user } = useAuth();
  const { isDark } = useTheme();
  const queryClient = useQueryClient();
  const [borrowModalOpen, setBorrowModalOpen] = useState(false);
  const [selectedBook, setSelectedBook] = useState<Book | null>(null);
  const [expectedReturnDate, setExpectedReturnDate] = useState("");
  const [notes, setNotes] = useState("");
  const [condition, setCondition] = useState("Bon état");

  const handleOpenBorrowModal = (book: Book) => {
    setSelectedBook(book);
    const defaultDate = new Date();
    defaultDate.setDate(defaultDate.getDate() + 14);
    setExpectedReturnDate(defaultDate.toISOString().split("T")[0]);
    setNotes("");
    setCondition("Bon état");
    setBorrowModalOpen(true);
  };

  const handleCloseBorrowModal = () => {
    setBorrowModalOpen(false);
    setTimeout(() => setSelectedBook(null), 200);
  };

  const borrowMutation = useMutation({
    mutationFn: (data: { bookId: number; expectedReturnDate: string; notes: string; condition: string }) => {
      return loansService.create(data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["books"] });
      queryClient.invalidateQueries({ queryKey: ["loans"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard-stats"] });
      handleCloseBorrowModal();
    },
    onError: (err) => {
      notifyError(getErrorMessage(err) || "Erreur lors de l'emprunt du livre");
    },
  });

  const COLORS = ["#10b981", "#8b5cf6", "#3b82f6", "#ef4444", "#ec4899", "#f59e0b", "#06b6d4"];

  const statsQuery = useQuery({
    queryKey: ["dashboard-stats"],
    queryFn: () => dashboardService.getStats(),
  });

  const tooltipStyle = {
    backgroundColor: isDark ? "rgba(15, 23, 42, 0.9)" : "rgba(255, 255, 255, 0.9)",
    borderColor: isDark ? "rgba(255,255,255,0.1)" : "rgba(0,0,0,0.1)",
    color: isDark ? "#fff" : "#000",
    borderRadius: "8px",
    boxShadow: "0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)",
  };

  const myLoans = useQuery({
    queryKey: ["loans", "my"],
    queryFn: () => loansService.getMyLoans({ page: 0, size: 50 }),
  });

  const recentBooks = useQuery({
    queryKey: ["books", "recent"],
    // size=4 to fit nicely in a grid
    queryFn: () => booksService.list({ page: 0, size: 4, sort: "id", direction: "desc" }),
  });

  const [currentBookIndex, setCurrentBookIndex] = React.useState(0);
  const popularBooks = useQuery({
    queryKey: ["books", "featured"],
    queryFn: () => booksService.list({ page: 0, size: 10, sort: "createdAt", direction: "asc" }),
  });

  React.useEffect(() => {
    if (!popularBooks.data?.content?.length) return;
    const interval = setInterval(() => {
      setCurrentBookIndex((prev) => (prev + 1) % popularBooks.data.content.length);
    }, 7000); // Change book every 7 seconds
    return () => clearInterval(interval);
  }, [popularBooks.data?.content]);

  const currentDate = new Date().toLocaleDateString(i18n.resolvedLanguage === "en" ? "en-US" : "fr-FR", {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  });

  if (myLoans.isLoading || recentBooks.isLoading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <CircularProgress sx={{ color: "#3b82f6" }} />
      </div>
    );
  }

  const loans = myLoans.data?.content || [];
  const activeLoans = loans.filter((l) => l.status === "ACTIVE");
  const overdueLoans = loans.filter((l) => l.status === "OVERDUE");
  const returnedLoans = loans.filter((l) => l.status === "RETURNED");

  const latestBooks = recentBooks.data?.content || [];

  return (
    <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-700">
      <div className="mb-6 flex flex-col md:flex-row md:justify-between md:items-end">
        <div>
          <h1 className="text-3xl font-extrabold text-slate-800 dark:text-white tracking-tight">
            {t("dashboard.welcome")}, <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-500 to-indigo-500">{user?.username}</span> !
          </h1>
          <p className="text-slate-500 dark:text-gray-400 mt-1 text-sm font-medium">
            {t("dashboard.userSubtitle")}
          </p>
        </div>
        <div className="mt-4 md:mt-0 flex items-center text-sm font-medium text-slate-500 dark:text-slate-400 bg-white/60 dark:bg-slate-800/60 backdrop-blur-md px-4 py-2 rounded-lg border border-slate-200 dark:border-white/5">
          <CalendarTodayIcon fontSize="small" className="mr-2 text-blue-500" />
          <span className="capitalize">{currentDate}</span>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <UserStatCard
          label={t("dashboard.myActiveLoans")}
          value={activeLoans.length}
          icon={<BookIcon />}
          gradient="from-blue-600 to-cyan-500"
        />
        <UserStatCard
          label={t("dashboard.myReturnedLoans")}
          value={returnedLoans.length}
          icon={<CheckCircleIcon />}
          gradient="from-emerald-500 to-teal-400"
        />
        <UserStatCard
          label={t("dashboard.myOverdueLoans")}
          value={overdueLoans.length}
          icon={<WarningIcon />}
          gradient={overdueLoans.length > 0 ? "from-rose-500 to-orange-500" : "from-slate-400 to-slate-300"}
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
        {/* Historique d'activité (Timeline) */}
        <div className="transform transition-all duration-500 flex flex-col h-[320px]">
          <UserHistoryTimeline loans={loans} />
        </div>

        {/* Historique des emprunts */}
        <div className="bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border border-slate-200 dark:border-white/10 rounded-2xl p-5 transform transition-all duration-500 flex flex-col h-[320px]">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-bold text-slate-800 dark:text-gray-100 flex items-center">
              <span className="w-1.5 h-6 rounded-full bg-gradient-to-b from-blue-500 to-cyan-500 mr-2"></span>
              {t("dashboard.myRecentLoans")}
            </h2>
            <Link to="/loans/my" className="text-xs font-bold text-blue-500 hover:text-blue-600 flex items-center">
              {t("dashboard.viewAll")} <ArrowForwardIcon sx={{ fontSize: 14, ml: 0.5 }} />
            </Link>
          </div>

          {loans.length > 0 ? (
            <div className="flex-1 flex flex-col space-y-3 overflow-y-auto pr-2 custom-scrollbar">
              {loans.map((loan) => (
                <div key={loan.id} className="flex items-center justify-between p-3 border border-slate-100 dark:border-white/5 bg-slate-50/50 dark:bg-slate-800/30 rounded-xl shrink-0">
                  <div className="flex items-center space-x-3">
                    <div className={`w-10 h-10 rounded-full flex items-center justify-center ${
                      loan.status === "RETURNED" ? "bg-emerald-100 text-emerald-600 dark:bg-emerald-500/20" :
                      loan.status === "ACTIVE" ? "bg-blue-100 text-blue-600 dark:bg-blue-500/20" :
                      "bg-rose-100 text-rose-600 dark:bg-rose-500/20"
                    }`}>
                      {loan.status === "RETURNED" ? <CheckCircleIcon fontSize="small" /> :
                       loan.status === "ACTIVE" ? <AccessTimeIcon fontSize="small" /> :
                       <WarningIcon fontSize="small" />}
                    </div>
                    <div>
                      <h3 className="font-bold text-sm text-slate-800 dark:text-slate-200">{loan.book.title}</h3>
                      <p className="text-xs text-slate-500 dark:text-slate-400">
                        {t("dashboard.expectedReturn")}: {new Date(loan.expectedReturnDate).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                  <span className={`text-[10px] font-bold uppercase tracking-wider px-2 py-1 rounded-md ${
                    loan.status === "RETURNED" ? "bg-emerald-100 text-emerald-700 dark:bg-emerald-500/20 dark:text-emerald-400" :
                    loan.status === "ACTIVE" ? "bg-blue-100 text-blue-700 dark:bg-blue-500/20 dark:text-blue-400" :
                    "bg-rose-100 text-rose-700 dark:bg-rose-500/20 dark:text-rose-400"
                  }`}>
                    {t(`loans.status_${loan.status}`)}
                  </span>
                </div>
              ))}
            </div>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center text-slate-400 text-sm">
              <InboxIcon sx={{ fontSize: 40, opacity: 0.3 }} className="mb-2" />
              <p>{t("dashboard.noLoans")}</p>
            </div>
          )}
        </div>
      </div>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
        <div className="bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border border-slate-200 dark:border-white/10 rounded-2xl p-5 transform transition-all duration-500 flex flex-col">
          <h2 className="text-lg font-bold text-slate-800 dark:text-gray-100 mb-4 flex items-center">
            <span className="w-1.5 h-6 rounded-full bg-gradient-to-b from-blue-500 to-indigo-500 mr-2"></span>
            {t("dashboard.distribution")}
          </h2>
          {statsQuery.data?.booksByCategory && statsQuery.data.booksByCategory.length > 0 ? (
            <div className="flex-1 min-h-[250px] relative">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={statsQuery.data.booksByCategory}
                    dataKey="bookCount"
                    nameKey="category"
                    cx="50%"
                    cy="45%"
                    innerRadius={60}
                    outerRadius={80}
                    paddingAngle={5}
                    stroke="none"
                  >
                    {statsQuery.data.booksByCategory.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <RechartsTooltip contentStyle={tooltipStyle} />
                </PieChart>
              </ResponsiveContainer>
              <div className="absolute bottom-0 left-0 right-0 flex justify-center flex-wrap gap-3 mt-4">
                {statsQuery.data.booksByCategory.map((entry, index) => (
                  <div key={entry.category} className="flex items-center text-xs">
                    <span
                      className="w-2.5 h-2.5 rounded-full mr-1.5"
                      style={{ backgroundColor: COLORS[index % COLORS.length] }}
                    ></span>
                    <span className="text-slate-600 dark:text-slate-400">{entry.category}</span>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center text-slate-400 text-sm">
              <InboxIcon sx={{ fontSize: 48, opacity: 0.3 }} className="mb-2" />
              <p>{t("dashboard.noData")}</p>
            </div>
          )}
        </div>

        <div className="bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border border-slate-200 dark:border-white/10 rounded-2xl p-5 transform transition-all duration-500 flex flex-col">
          <h2 className="text-lg font-bold text-slate-800 dark:text-gray-100 mb-4 flex items-center justify-between">
            <div className="flex items-center">
              <span className="w-1.5 h-6 rounded-full bg-gradient-to-b from-purple-500 to-pink-500 mr-2"></span>
              {t("dashboard.featuredBook")}
            </div>
            {popularBooks.data?.content && (
              <div className="flex space-x-1">
                {popularBooks.data.content.map((_, idx) => (
                  <button 
                    key={idx} 
                    onClick={() => setCurrentBookIndex(idx)}
                    className={`w-2 h-2 rounded-full transition-all duration-500 cursor-pointer hover:bg-purple-400 ${idx === currentBookIndex ? "bg-purple-500 w-4" : "bg-slate-300 dark:bg-slate-700"}`} 
                    aria-label={`Aller au livre ${idx + 1}`}
                  />
                ))}
              </div>
            )}
          </h2>
          
          {popularBooks.data?.content && popularBooks.data.content.length > 0 ? (
            <div className="flex-1 relative overflow-hidden rounded-xl min-h-[250px] group flex items-center justify-center bg-gradient-to-br from-slate-50 to-slate-100 dark:from-slate-800/50 dark:to-slate-900/50">
              {popularBooks.data.content.map((book, idx) => (
                <div
                  key={book.id}
                  className={`absolute inset-0 p-6 flex flex-col justify-center transition-all duration-1000 transform ${
                    idx === currentBookIndex ? "opacity-100 translate-x-0 scale-100" : "opacity-0 translate-x-10 scale-95 pointer-events-none"
                  }`}
                >
                  <div className="flex flex-col md:flex-row items-center md:items-start gap-6">
                    <div className="w-24 h-32 md:w-32 md:h-44 bg-gradient-to-tr from-purple-600 to-blue-500 rounded-lg shadow-lg flex-shrink-0 flex items-center justify-center text-white transform transition-transform group-hover:scale-105 group-hover:rotate-2">
                      <BookIcon sx={{ fontSize: 60 }} className="opacity-80" />
                    </div>
                    
                    <div className="flex-1 text-center md:text-left flex flex-col justify-center h-full">
                      <div className="mb-2">
                        <span className={`inline-block px-3 py-1 rounded-full text-xs font-bold tracking-widest mb-3 uppercase ${
                          book.stock > 0 
                            ? "bg-emerald-100 text-emerald-700 dark:bg-emerald-500/20 dark:text-emerald-400" 
                            : "bg-orange-100 text-orange-700 dark:bg-orange-500/20 dark:text-orange-400"
                        }`}>
                          {book.stock > 0 ? t("dashboard.available") : t("dashboard.unavailable")}
                        </span>
                        <h3 className="text-xl md:text-2xl font-extrabold text-slate-800 dark:text-white leading-tight mb-1">{book.title}</h3>
                        <p className="text-sm font-semibold text-purple-500 dark:text-purple-400">{book.author}</p>
                      </div>
                      
                      <p className="text-sm text-slate-600 dark:text-slate-400 line-clamp-3 mb-4 max-w-md mx-auto md:mx-0">
                        {book.description || t("dashboard.noDescription")}
                      </p>
                      
                      {book.stock === 0 && !activeLoans.some(l => l.book.id === book.id) && (
                        <button 
                          disabled
                          className="inline-flex items-center text-sm font-medium text-slate-400 bg-slate-100 dark:bg-slate-800/50 px-3 py-2 rounded-lg justify-center md:justify-start w-fit mx-auto md:mx-0 cursor-not-allowed"
                        >
                          <WarningIcon sx={{ fontSize: 18 }} className="mr-2" />
                          {t("dashboard.unavailable")}
                        </button>
                      )}
                      
                      {book.stock > 0 && !activeLoans.some(l => l.book.id === book.id) && (
                        <button 
                          onClick={() => handleOpenBorrowModal(book)}
                          className="inline-flex items-center text-sm font-medium text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-900/30 px-3 py-2 rounded-lg justify-center md:justify-start w-fit mx-auto md:mx-0 hover:bg-emerald-100 dark:hover:bg-emerald-900/50 transition-colors"
                        >
                          <CheckCircleIcon sx={{ fontSize: 18 }} className="mr-2" />
                          {t("dashboard.borrowNow")}
                        </button>
                      )}

                      {activeLoans.some(l => l.book.id === book.id) && (
                        <div className="inline-flex items-center text-sm font-medium text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/30 px-3 py-2 rounded-lg justify-center md:justify-start w-fit mx-auto md:mx-0">
                          <BookIcon sx={{ fontSize: 18 }} className="mr-2" />
                          {t("dashboard.alreadyBorrowed")}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center text-slate-400 text-sm">
              <InboxIcon sx={{ fontSize: 48, opacity: 0.3 }} className="mb-2" />
              <p>{t("dashboard.noBookFound")}</p>
            </div>
          )}
        </div>
    </div>

      {/* Modal d'emprunt */}
      <Dialog open={borrowModalOpen} onClose={handleCloseBorrowModal} maxWidth="sm" fullWidth>
        <DialogTitle className="bg-slate-50 dark:bg-slate-800 text-slate-800 dark:text-white font-bold border-b border-slate-200 dark:border-white/10">
          {t("dashboard.borrowModalTitle")}
        </DialogTitle>
        <DialogContent className="bg-white dark:bg-slate-900 pt-6">
          <div className="mb-4 p-3 bg-indigo-50 dark:bg-indigo-500/10 rounded-lg flex items-start gap-3">
            <BookIcon className="text-indigo-500 mt-0.5" />
            <div>
              <p className="font-semibold text-slate-800 dark:text-slate-200">{selectedBook?.title}</p>
              <p className="text-sm text-slate-500 dark:text-slate-400">{selectedBook?.author}</p>
            </div>
          </div>
          <div className="flex flex-col gap-5 mt-5">
            <TextField
              label={t("dashboard.expectedReturnDate")}
              type="date"
              fullWidth
              InputLabelProps={{ shrink: true }}
              value={expectedReturnDate}
              onChange={(e) => setExpectedReturnDate(e.target.value)}
              required
            />
            <TextField
              label={t("dashboard.bookCondition")}
              fullWidth
              value={condition}
              onChange={(e) => setCondition(e.target.value)}
              placeholder={t("dashboard.bookConditionPlaceholder")}
            />
            <TextField
              label={t("dashboard.bookNotes")}
              fullWidth
              multiline
              rows={3}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder={t("dashboard.bookNotesPlaceholder")}
            />
          </div>
        </DialogContent>
        <DialogActions className="bg-slate-50 dark:bg-slate-800 border-t border-slate-200 dark:border-white/10 p-4">
          <Button onClick={handleCloseBorrowModal} color="inherit" disabled={borrowMutation.isPending}>
            {t("dashboard.cancel")}
          </Button>
          <Button
            onClick={() => selectedBook && borrowMutation.mutate({
              bookId: selectedBook.id,
              expectedReturnDate,
              notes,
              condition
            })}
            variant="contained"
            color="primary"
            disabled={borrowMutation.isPending || !expectedReturnDate}
            startIcon={borrowMutation.isPending ? <CircularProgress size={20} color="inherit" /> : <CheckCircleIcon />}
            className="bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700"
          >
            {borrowMutation.isPending ? t("dashboard.borrowing") : t("dashboard.confirmBorrow")}
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
}

