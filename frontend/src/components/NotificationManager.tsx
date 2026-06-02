import { useEffect } from "react";
import { toast } from "sonner";
import { wsService } from "@/services/websocket";
import { useAuth } from "@/context/auth";
import { useQueryClient } from "@tanstack/react-query";

export function NotificationManager() {
  const { user } = useAuth();
  const qc = useQueryClient();

  useEffect(() => {
    if (user) {
      wsService.connect();
      wsService.setNotificationHandler((notification) => {
        switch (notification.type) {
          case "SUCCESS":
            toast.success(notification.title, { id: notification.title, description: notification.message });
            break;
          case "ERROR":
            toast.error(notification.title, { id: notification.title, description: notification.message });
            break;
          case "WARNING":
            toast.warning(notification.title, { id: notification.title, description: notification.message });
            break;
          default:
            toast.info(notification.title, { id: notification.title, description: notification.message });
        }
        
        // Rafraîchir les données en temps réel (Dashboard, Emprunts, Livres)
        qc.invalidateQueries({ queryKey: ["dashboard-stats"] });
        qc.invalidateQueries({ queryKey: ["loans"] });
        qc.invalidateQueries({ queryKey: ["books"] });
      });
    } else {
      wsService.disconnect();
    }

    return () => {
      wsService.disconnect();
    };
  }, [user]);

  return null;
}
