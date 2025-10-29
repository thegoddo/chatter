// src/hooks/useChat.js
"use client";

import { useState, useEffect, useRef, useCallback } from "react";
import { Client } from "@stomp/stompjs";
import axios from "axios";
import { useAuth } from "@/context/AuthContext";

const API_BASE_URL = "http://localhost:8080/api";
const WEBSOCKET_URL = "ws://localhost:8080/ws";

export const useChat = () => {
  const { user, token } = useAuth();

  // --- State Management ---
  const [stompClient, setStompClient] = useState(null);
  const [isConnected, setIsConnected] = useState(false);

  // UI/Conversation State
  const [activeConversation, setActiveConversation] = useState("PUBLIC");

  // Data State
  const [onlineUsers, setOnlineUsers] = useState([]);
  const [publicMessages, setPublicMessages] = useState([]);
  const [privateMessages, setPrivateMessages] = useState({});

  // Ref to prevent history fetch on every render
  const hasFetchedHistory = useRef(false);

  // --- Data Fetching Functions (Memoized) ---

  // Fetch Public History
  const fetchPublicHistory = useCallback(async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/chat/messages/public`);
      setPublicMessages(response.data);
    } catch (error) {
      console.error("Failed to fetch public history:", error);
    }
  }, []);

  // Fetch Private History for a specific user
  const fetchPrivateHistory = useCallback(
    async (recipient) => {
      if (privateMessages[recipient]) return; // Avoid refetching if already loaded

      try {
        const response = await axios.get(
          `${API_BASE_URL}/chat/messages/private/${user.username}/${recipient}`
        );
        setPrivateMessages((prev) => ({
          ...prev,
          [recipient]: response.data,
        }));
      } catch (error) {
        console.error(
          `Failed to fetch private history for ${recipient}:`,
          error
        );
      }
    },
    [user, privateMessages]
  ); // Dependent on user and privateMessages cache

  // Fetch Initial Online Users (from Redis via REST API)
  const fetchOnlineUsers = useCallback(async () => {
    try {
      // Note: This needs to be /api/presence/online if using the controller from before
      const response = await axios.get(`${API_BASE_URL}/presence/online`);
      setOnlineUsers(response.data);
    } catch (error) {
      console.error("Failed to fetch online users:", error);
    }
  }, []);

  // --- WebSocket Connection and Subscription Logic (CORE EFFECT) ---
  useEffect(() => {
    if (!user || !token) return;

    const client = new Client({
      brokerURL: WEBSOCKET_URL,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      onConnect: () => {
        console.log("STOMP Connected");
        setIsConnected(true);

        // --- 1. Subscriptions ---
        client.subscribe("/topic/public-chat", (message) => {
          setPublicMessages((prev) => [...prev, JSON.parse(message.body)]);
        });

        client.subscribe(`/user/${user.username}/queue/messages`, (message) => {
          const receivedMsg = JSON.parse(message.body);
          const otherUser =
            receivedMsg.sender === user.username
              ? receivedMsg.recipient
              : receivedMsg.sender;

          setPrivateMessages((prev) => ({
            ...prev,
            [otherUser]: [...(prev[otherUser] || []), receivedMsg],
          }));
        });

        client.subscribe("/topic/online-users", (message) => {
          setOnlineUsers(JSON.parse(message.body));
        });

        // --- 2. Initial Data Fetch & Join Message ---
        if (!hasFetchedHistory.current) {
          fetchPublicHistory();
          fetchOnlineUsers();
          hasFetchedHistory.current = true;
        }

        client.publish({
          destination: "/app/chat.addUser",
          body: JSON.stringify({ sender: user.username, type: "JOIN" }),
          headers: { Authorization: `Bearer ${token}` },
        });
      },

      onStompError: (frame) => {
        console.error("Broker reported error:", frame.headers["message"]);
        client.deactivate();
        setIsConnected(false);
      },
      reconnectDelay: 5000,
    });

    client.activate();
    setStompClient(client);

    // --- 3. Cleanup ---
    return () => {
      if (client.connected) {
        console.log("STOMP Disconnected on cleanup");
        client.deactivate();
      }
    };
  }, [user, token, fetchPublicHistory, fetchOnlineUsers]);

  // --- Message Sending Logic ---
  const sendMessage = (messageInput, recipient) => {
    if (!stompClient || !messageInput.trim() || !user) return;

    const isPublic = recipient === "PUBLIC";
    const destination = isPublic
      ? "/app/chat.sendMessage"
      : "/app/chat.sendPrivateMessage";

    const messageBody = {
      sender: user.username,
      content: messageInput.trim(),
      recipient: isPublic ? null : recipient,
      type: isPublic ? "CHAT" : "PRIVATE",
      timestamp: new Date().toISOString(),
    };

    stompClient.publish({
      destination: destination,
      body: JSON.stringify(messageBody),
      headers: { Authorization: `Bearer ${token}` },
    });

    // Optimistic update for Private Messages
    if (!isPublic) {
      setPrivateMessages((prev) => ({
        ...prev,
        [recipient]: [...(prev[recipient] || []), messageBody],
      }));
    }
  };

  // Combined list for the sidebar: Online users + users with active chats
  const privateChatUsers = Object.keys(privateMessages).filter(
    (u) => u !== user.username
  );
  const allChatUsers = Array.from(
    new Set([
      ...onlineUsers.filter((u) => u !== user?.username),
      ...privateChatUsers.filter((u) => u !== user?.username),
    ])
  ).sort();

  return {
    isConnected,
    activeConversation,
    setActiveConversation: useCallback(
      (recipient) => {
        setActiveConversation(recipient);
        if (recipient !== "PUBLIC" && !privateMessages[recipient]) {
          fetchPrivateHistory(recipient);
        }
      },
      [privateMessages, fetchPrivateHistory]
    ),
    onlineUsers,
    allChatUsers,
    publicMessages,
    privateMessages,
    sendMessage,
    fetchPrivateHistory,
  };
};
