// src/app/chat/page.jsx
"use client";

import React, { useState, useEffect, useRef } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { useChat } from "@/hooks/useChat"; // <-- Import the new hook
import {
  Send,
  LogOut,
  MessageSquare,
  Users,
  User,
  ArrowLeft,
} from "lucide-react";

const ChatPage = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const router = useRouter();

  // --- Use the Custom Chat Hook ---
  const {
    isConnected,
    activeConversation,
    setActiveConversation,
    onlineUsers,
    allChatUsers,
    publicMessages,
    privateMessages,
    sendMessage,
  } = useChat();

  // --- Local State for Input ---
  const [messageInput, setMessageInput] = useState("");
  const messagesEndRef = useRef(null);

  // --- Routing & Scroll Effects (Kept Local) ---
  useEffect(() => {
    if (!isAuthenticated) {
      router.push("/login");
    }
  }, [isAuthenticated, router]);

  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [publicMessages, privateMessages, activeConversation]);

  // --- UI Helper Functions ---
  const handleSubmit = (e) => {
    e.preventDefault();
    sendMessage(messageInput, activeConversation);
    setMessageInput("");
  };

  const currentMessages =
    activeConversation === "PUBLIC"
      ? publicMessages
      : privateMessages[activeConversation] || [];

  // --- Render Logic ---
  if (!isAuthenticated || !user)
    return (
      <div className="flex justify-center items-center h-screen bg-gray-100">
        Loading user data...
      </div>
    );

  // The rest of the return block (JSX) remains largely the same,
  // but the handlers are simplified to use the hook's functions.
  return (
    <div className="flex h-screen antialiased text-gray-800">
      <div className="flex flex-row h-full w-full overflow-x-hidden">
        {/* Left Sidebar (User List) */}
        <div className="flex flex-col py-8 pl-6 pr-2 w-64 bg-white flex-shrink-0 shadow-lg border-r border-gray-200">
          {/* Header/Logo */}
          <div className="flex flex-row items-center justify-center h-12 w-full">
            <div className="flex items-center justify-center rounded-2xl text-indigo-700 bg-indigo-100 h-10 w-10">
              <MessageSquare size={20} />
            </div>
            <div className="ml-2 font-bold text-2xl text-indigo-700">
              Chatter
            </div>
          </div>

          {/* Conversation List */}
          <div className="flex flex-col mt-4">
            <div className="flex flex-row items-center justify-between text-xs mb-1 font-semibold text-gray-500 uppercase">
              <span>Conversations</span>
              <span className="flex items-center">
                <Users size={14} className="mr-1" /> ({onlineUsers.length})
              </span>
            </div>

            <div className="flex flex-col space-y-2 mt-4 overflow-y-auto max-h-96">
              {/* Public Chat Option */}
              <button
                onClick={() => setActiveConversation("PUBLIC")}
                className={`flex flex-row items-center p-3 rounded-xl transition duration-150 ease-in-out cursor-pointer ${
                  activeConversation === "PUBLIC"
                    ? "bg-indigo-100 border border-indigo-300"
                    : "hover:bg-gray-100"
                }`}
              >
                <div className="flex items-center justify-center h-8 w-8 bg-indigo-500 rounded-full text-white font-bold">
                  <MessageSquare size={16} />
                </div>
                <div className="ml-2 text-sm font-semibold">Public Chat</div>
              </button>

              {/* Private Chat List */}
              {allChatUsers.map((recipient) => (
                <button
                  key={recipient}
                  onClick={() => setActiveConversation(recipient)}
                  className={`flex flex-row items-center p-3 rounded-xl transition duration-150 ease-in-out cursor-pointer ${
                    activeConversation === recipient
                      ? "bg-indigo-100 border border-indigo-300"
                      : "hover:bg-gray-100"
                  }`}
                >
                  <div className="relative">
                    <div
                      className={`flex items-center justify-center h-8 w-8 rounded-full ${
                        onlineUsers.includes(recipient)
                          ? "bg-green-500"
                          : "bg-gray-400"
                      } text-white font-bold`}
                    >
                      {recipient.charAt(0).toUpperCase()}
                    </div>
                    <span
                      className={`absolute right-0 bottom-0 block h-2.5 w-2.5 rounded-full ring-2 ring-white ${
                        onlineUsers.includes(recipient)
                          ? "bg-green-400"
                          : "bg-red-400"
                      }`}
                    ></span>
                  </div>
                  <div className="ml-2 text-sm font-semibold truncate max-w-[100px]">
                    {recipient}
                  </div>
                  <div className="flex-grow text-right text-xs text-gray-500">
                    {onlineUsers.includes(recipient) ? "Online" : "Offline"}
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* User Profile & Logout */}
          <div className="mt-auto pt-4 border-t border-gray-200">
            <div className="flex flex-row items-center justify-between">
              <div className="flex flex-row items-center">
                <div className="flex items-center justify-center h-10 w-10 bg-indigo-50 rounded-full text-indigo-700 font-bold">
                  <User size={20} />
                </div>
                <div className="flex flex-col ml-2">
                  <span className="text-sm font-semibold">{user.username}</span>
                  <span
                    className={`text-xs ${
                      isConnected ? "text-green-500" : "text-red-500"
                    }`}
                  >
                    {isConnected ? "Connected" : "Connecting..."}
                  </span>
                </div>
              </div>
              <button
                onClick={logout}
                title="Logout"
                className="p-2 rounded-full text-gray-500 hover:bg-gray-100 hover:text-red-500 transition duration-150"
              >
                <LogOut size={20} />
              </button>
            </div>
          </div>
        </div>

        {/* Main Chat Area */}
        <div className="flex flex-col flex-auto h-full p-6">
          <div className="flex flex-col flex-auto flex-shrink-0 rounded-xl bg-white h-full shadow-xl">
            {/* Chat Header */}
            <div className="flex items-center justify-between p-4 border-b border-gray-200 bg-gray-50 rounded-t-xl">
              <div className="flex items-center">
                {activeConversation !== "PUBLIC" && (
                  <button
                    onClick={() => setActiveConversation("PUBLIC")}
                    className="mr-3 text-gray-600 hover:text-indigo-600"
                  >
                    <ArrowLeft size={20} />
                  </button>
                )}
                <h2 className="text-xl font-bold text-gray-700">
                  {activeConversation === "PUBLIC"
                    ? "Public Global Chat"
                    : `Chatting with ${activeConversation}`}
                </h2>
              </div>
            </div>

            {/* Message Display Area */}
            <div className="chat-messages flex flex-col h-full overflow-x-hidden overflow-y-auto p-4 space-y-4">
              {currentMessages.length > 0 ? (
                currentMessages.map((msg, index) => (
                  <div
                    key={index}
                    className={`flex w-full ${
                      msg.sender === user.username
                        ? "justify-end"
                        : "justify-start"
                    }`}
                  >
                    <div
                      className={`flex flex-col space-y-2 max-w-xs mx-2 items-${
                        msg.sender === user.username ? "end" : "start"
                      }`}
                    >
                      <div className="text-xs text-gray-500">
                        {msg.sender === user.username ? "You" : msg.sender}
                        <span className="ml-2 text-[10px] opacity-70">
                          {new Date(msg.timestamp).toLocaleTimeString()}
                        </span>
                      </div>
                      <div
                        className={`px-4 py-2 rounded-xl inline-block shadow-md ${
                          msg.sender === user.username
                            ? "bg-indigo-600 text-white rounded-br-none"
                            : "bg-gray-200 text-gray-800 rounded-tl-none"
                        }`}
                      >
                        {msg.content}
                      </div>
                    </div>
                  </div>
                ))
              ) : (
                <p className="text-center text-gray-500 mt-10">
                  {isConnected
                    ? "Start the conversation!"
                    : "Connecting to chat server..."}
                </p>
              )}
              <div ref={messagesEndRef} />
            </div>

            {/* Message Input Area */}
            <div className="flex flex-row items-center h-16 rounded-b-xl w-full border-t border-gray-200 p-4 bg-gray-50">
              <form onSubmit={handleSubmit} className="flex w-full space-x-3">
                <input
                  type="text"
                  placeholder={`Type message to ${
                    activeConversation === "PUBLIC"
                      ? "Public Chat"
                      : activeConversation
                  }...`}
                  value={messageInput}
                  onChange={(e) => setMessageInput(e.target.value)}
                  className="flex-grow w-full border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500 p-3 shadow-sm"
                  disabled={!isConnected}
                />
                <button
                  type="submit"
                  disabled={!isConnected || messageInput.trim() === ""}
                  className="flex items-center justify-center p-3 rounded-full bg-indigo-600 text-white shadow-lg hover:bg-indigo-700 transition duration-150 disabled:bg-indigo-300 disabled:cursor-not-allowed"
                >
                  <Send size={20} />
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ChatPage;
