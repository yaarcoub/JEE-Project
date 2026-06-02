import { Link } from "@tanstack/react-router";
import { useTranslation } from "react-i18next";
import BookIcon from "@mui/icons-material/MenuBook";
import FacebookIcon from "@mui/icons-material/Facebook";
import TwitterIcon from "@mui/icons-material/Twitter";
import LinkedInIcon from "@mui/icons-material/LinkedIn";

export function Footer() {
  const { t } = useTranslation();

  return (
    <footer className="bg-white dark:bg-slate-900 text-slate-800 dark:text-slate-100 pt-8 pb-4 mt-auto border-t border-slate-200 dark:border-transparent transition-colors duration-300">
      <div className="w-full px-6 lg:px-10">
        <div className="flex flex-col md:flex-row justify-between gap-6 mb-8">
          {/* Brand Column */}
          <div className="md:max-w-md">
            <div className="flex items-center space-x-3 mb-4">
              <div className="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center text-white shadow">
                <BookIcon fontSize="small" />
              </div>
              <h2 className="text-xl font-bold tracking-tight text-slate-800 dark:text-white">
                {t("footer.brand")}
              </h2>
            </div>
            <p className="text-slate-500 dark:text-slate-400 text-sm mb-6">
              {t("footer.description")}
            </p>
            
          </div>

          {/* Links Column 1 */}
          <div className="md:min-w-[150px]">
            <h3 className="font-semibold mb-4 text-slate-700 dark:text-slate-200">
              {t("footer.products")}
            </h3>
            <ul className="space-y-3 text-sm text-slate-500 dark:text-slate-400">
              <li>
                <Link
                  to="/books"
                  className="hover:text-blue-600 dark:hover:text-blue-400 transition-colors"
                >
                  {t("footer.catalog")}
                </Link>
              </li>
              <li>
                <Link
                  to="/categories"
                  className="hover:text-blue-600 dark:hover:text-blue-400 transition-colors"
                >
                  {t("footer.categories")}
                </Link>
              </li>
              <li>
                <Link
                  to="/loans/my"
                  className="hover:text-blue-600 dark:hover:text-blue-400 transition-colors"
                >
                  {t("footer.myLoans")}
                </Link>
              </li>
            </ul>
          </div>
        </div>

        <div className="border-t border-slate-200 dark:border-slate-800 pt-6 flex flex-col md:flex-row justify-between items-center text-xs text-slate-400 dark:text-slate-500">
          <p>&copy; {new Date().getFullYear()} {t("footer.rights")}</p>
        </div>
      </div>
    </footer>
  );
}
