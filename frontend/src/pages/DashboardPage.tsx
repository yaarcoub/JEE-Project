import { useAuth } from "@/context/auth";
import { AdminDashboard } from "./AdminDashboard";
import { UserDashboard } from "./UserDashboard";

export function DashboardPage() {
  const { hasRole } = useAuth();
  
  if (hasRole("ROLE_ADMIN", "ROLE_MANAGER")) {
    return <AdminDashboard />;
  }
  
  return <UserDashboard />;
}
