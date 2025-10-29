"use client";

import { useState, useEffect, useRef, useCallback } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import axios from "axios";
import { useAuth } from "@/app/context/AuthContext";

const API_BASE_URL = "http://localhost:8080/api";
const SOCKJS_URL = "http://localhost:8080/chat"; // Backend must expose /chat with SockJS

export const useChat = () => {
  const { user, token } = useAuth();

  const [stompClient, setStompClient] = useState(null);
  const [isConnected, setIsConnected] = useState(false);
  const [activeConversation, setActiveConversation] = useState("PUBLIC");
  const [onlineUsers, setOnlineUsers] = useState([]);
  const [publicMessages, setPublicMessages] = useState([]);
  const [privateMessages, setPrivateMessages] = useState({});
  const hasFetchedHistory = useRef(false);

  const fetchPublicHistory = useCallback(async () => {
    try {
      const res = await axios.get(`${API_BASE_URL}/messages/history`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setPublicMessages(res.data);
    } catch (err) {
      console.error("Public history fetch failed:", err);
    }
  }, [token]);

  const fetchPrivateHistory = useCallback(
    async (recipient) => {
      if (privateMessages[recipient]) return;
      try {
        const res = await axios.get(
          `${API_BASE_URL}/messages/history/private/${user?.username}/${recipient}`,
          {
            headers: { Authorization: `Bearer ${token}` },
          }
        );
        setPrivateMessages((prev) => ({
          ...prev,
          [recipient]: res.data,
        }));
      } catch (err) {
        console.error("Private history fetch failed:", err);
      }
    },
    [token, user, privateMessages]
  );

  const fetchOnlineUsers = useCallback(async () => {
    try {
      const res = await axios.get(`${API_BASE_URL}/presence/online`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setOnlineUsers(res.data);
    } catch (err) {
      console.error("Online users fetch failed:", err);
    }
  }, [token]);

  const onMessageReceived = useCallback(
    (message) => {
      const msg = JSON.parse(message.body);
      if (["CHAT", "JOIN", "LEAVE"].includes(msg.type)) {
        setPublicMessages((prev) => [...prev, msg]);
        if (msg.type === "JOIN" || msg.type === "LEAVE") {
          fetchOnlineUsers();
        }
      } else if (msg.type === "PRIVATE") {
        const otherUser =
          msg.sender === user?.username ? msg.recipient : msg.sender;
        setPrivateMessages((prev) => ({
          ...prev,
          [otherUser]: [...(prev[otherUser] || []), msg],
        }));
      }
    },
    [user, fetchOnlineUsers]
  );

  useEffect(() => {
    if (!user || !token) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(SOCKJS_URL),
      connectHeaders: { Authorization: `Bearer ${token}` },
      onConnect: () => {
        console.log("Connected to Websocket");

        client.subscribe("/topic/public-chat", onMessageReceived);
        client.subscribe("/user/queue/messages", onMessageReceived);
        // client.subscribe("/user/queue/private", onMessageReceived);
        setIsConnected(true);
      },
      onStompError: (frame) => console.error("STOMP error:", frame),
      onWebSocketClose: () => setIsConnected(false),
    });

    setStompClient(client);
    client.activate();

    return () => {
      if (client && client.connected) {
        try {
          client.publish({
            destination: "/app/chat.addUser",
            headers: { Authorization: `Bearer ${token}` },
            body: JSON.stringify({ sender: user?.username, type: "LEAVE" }),
          });
        } catch (err) {
          console.warn("LEAVE publish failed:", err);
        }
      }
      client.deactivate();
    };
  }, [user, token, onMessageReceived]);

  useEffect(() => {
    if (stompClient && isConnected && user?.username) {
      stompClient.publish({
        destination: "/app/chat.addUser",
        headers: { Authorization: `Bearer ${token}` },
        body: JSON.stringify({ sender: user.username, type: "JOIN" }),
      });

      if (!hasFetchedHistory.current) {
        fetchPublicHistory();
        fetchOnlineUsers();
        hasFetchedHistory.current = true;
      }
    }
  }, [
    stompClient,
    isConnected,
    user,
    token,
    fetchPublicHistory,
    fetchOnlineUsers,
  ]);

  useEffect(() => {
    const client = stompClient;
    return () => {
      if (client?.connected) {
        client.deactivate();
      }
    };
  }, [stompClient]);
  const sendMessage = (
    messageInput,
    recipient,
    type = "CHAT",
    mediaUrl = null
  ) => {
    if (!stompClient || !messageInput.trim() || !user) return;

    const isPublic = recipient === "PUBLIC";
    const destination = isPublic
      ? "/app/chat.sendMessage"
      : "/app/chat.sendPrivateMessage";
    console.log(destination);

    const messageBody = {
      sender: user.username,
      content: messageInput?.trim() || "",
      recipient: isPublic ? null : recipient,
      type,
      mediaUrl,
      timestamp: new Date().toISOString(),
    };

    stompClient.publish({
      destination,
      body: JSON.stringify(messageBody),
      headers: { Authorization: `Bearer ${token}` },
    });
  };

  const privateChatUsers = Object.keys(privateMessages).filter(
    (u) => u !== user?.username
  );
  const allChatUsers = Array.from(
    new Set([
      ...onlineUsers.filter((u) => u !== user?.username),
      ...privateChatUsers,
    ])
  ).sort();

  const handleSetActiveConversation = useCallback(
    (recipient) => {
      setActiveConversation(recipient);
      if (recipient !== "PUBLIC" && !privateMessages[recipient]) {
        fetchPrivateHistory(recipient);
      }
    },
    [fetchPrivateHistory, privateMessages]
  );

  return {
    isConnected,
    activeConversation,
    setActiveConversation: handleSetActiveConversation,
    onlineUsers,
    allChatUsers,
    publicMessages,
    privateMessages,
    sendMessage,
    currentMessages:
      activeConversation === "PUBLIC"
        ? publicMessages
        : privateMessages[activeConversation] || [],
  };
};
