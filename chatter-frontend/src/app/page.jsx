// src/app/page.jsx
"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "./context/AuthContext";
import Link from "next/link";

const HomePage = () => {
  const { isAuthenticated, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading) {
      if (isAuthenticated) {
        // If authenticated, go straight to the chat app
        router.push("/chat");
      } else {
        // If not authenticated, prompt them to log in
        router.push("/login");
      }
    }
  }, [isAuthenticated, isLoading, router]);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gray-100">
        <div className="text-xl font-medium text-indigo-600 animate-pulse">
          Loading Application...
        </div>
      </div>
    );
  }

  // This section is technically unreachable due to the redirect in useEffect,
  // but acts as a robust fallback view.
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100 p-8">
      <h1 className="text-5xl font-extrabold text-indigo-700 mb-4">
        Welcome to Chatter
      </h1>
      <p className="text-xl text-gray-600 mb-8">
        Your real-time, responsive chat application.
      </p>
      <div className="space-x-4">
        <Link
          href="/login"
          className="px-6 py-3 bg-indigo-600 text-white font-semibold rounded-lg shadow-lg hover:bg-indigo-700 transition duration-150"
        >
          Log In
        </Link>
        <Link
          href="/register"
          className="px-6 py-3 bg-green-600 text-white font-semibold rounded-lg shadow-lg hover:bg-green-700 transition duration-150"
        >
          Register
        </Link>
      </div>
    </div>
  );
};

export default HomePage;
