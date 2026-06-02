import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { API_BASE_URL, tokenStore } from "./http";

export type NotificationType = "INFO" | "SUCCESS" | "WARNING" | "ERROR";

export interface NotificationPayload {
  title: string;
  message: string;
  type: NotificationType;
}

class WebSocketService {
  private client: Client;
  private onNotification: ((n: NotificationPayload) => void) | null = null;

  constructor() {
    this.client = new Client({
      webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws-endpoint`),
      debug: (str) => console.log(str),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = () => {
      console.log("Connected to WebSocket");
      this.client.subscribe("/topic/notifications", (message) => {
        if (message.body) {
          const payload = JSON.parse(message.body) as NotificationPayload;
          if (this.onNotification) {
            this.onNotification(payload);
          }
        }
      });
    };

    this.client.onStompError = (frame) => {
      console.error("Broker reported error: " + frame.headers["message"]);
      console.error("Additional details: " + frame.body);
    };
  }

  public setNotificationHandler(handler: (n: NotificationPayload) => void) {
    this.onNotification = handler;
  }

  public connect() {
    if (!this.client.active) {
      this.client.activate();
    }
  }

  public disconnect() {
    if (this.client.active) {
      this.client.deactivate();
    }
  }
}

export const wsService = new WebSocketService();
