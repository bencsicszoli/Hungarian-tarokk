import { createContext, useContext, useEffect, useState, useRef } from "react";
import type { ReactNode } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { useUser } from "./UserContext.tsx";

const WebSocketContext = createContext<{ connected: boolean; subscribe: ({ destination, callback }: { destination: string; callback: (message: any) => void; }) => { unsubscribe: () => void }; send: (destination: string, body: object) => void } | null>(null);

export function WebSocketProvider({ children }: { children: ReactNode }) {
  const userContext = useUser();
  const token = userContext?.token;
  const [connected, setConnected] = useState(false);
  const stompClientRef = useRef<Client | null>(null);

  const subscriptionQueueRef = useRef<
    { destination: string; callback: (message: unknown) => void }[]
  >([]);

  useEffect(() => {
    if (!token) {
      console.warn("No JWT token found. WebSocket connection aborted.");
      return;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS("http://localhost:8080/ws"),

      connectHeaders: {
        Authorization: "Bearer " + token,
      },

      reconnectDelay: 5000,

      onConnect: () => {
        console.log("✅ WebSocket connected");
        setConnected(true);

        // Deferred subscriptionök aktiválása
        subscriptionQueueRef.current.forEach(({ destination, callback }) => {
          client.subscribe(destination, callback);
        });

        subscriptionQueueRef.current = [];
      },

      onStompError: (frame) => {
        console.error("❌ Broker error:", frame.headers["message"]);
        console.error("Details:", frame.body);
      },

      onWebSocketError: (error) => {
        console.error("❌ WebSocket error:", error);
      },

      onDisconnect: () => {
        console.log("🔌 WebSocket disconnected");
        setConnected(false);
      },
    });

    stompClientRef.current = client;
    client.activate();

    return () => {
      client.deactivate();
      setConnected(false);
    };
  }, [token]);

  function subscribe({ destination, callback }: { destination: string; callback: (message: unknown) => void }) {
    const client = stompClientRef.current;

    if (client && client.connected) {
      console.log(`📩 Subscribing to ${destination}`);
      return client.subscribe(destination, callback);
    } else {
      console.warn("Not connected yet. Subscription deferred.");
      subscriptionQueueRef.current.push({ destination, callback });
      return { unsubscribe: () => {} };
    }
  }

  function send(destination: string, body: object) {
    const client = stompClientRef.current;

    if (client && client.connected) {
      client.publish({
        destination,
        body: JSON.stringify(body),
      });
    } else {
      console.warn("Not connected yet. Message not sent.");
    }
  }

  return (
    <WebSocketContext.Provider value={{ connected, subscribe, send }}>
      {children}
    </WebSocketContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useWebSocket() {
  return useContext(WebSocketContext);
}

