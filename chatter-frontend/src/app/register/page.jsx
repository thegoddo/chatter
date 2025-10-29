"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "../context/AuthContext";

const RegisterPage = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [phoneNumber, setPhoneNumber] = useState(""); // Optional phone number
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { register } = useAuth();
  const router = useRouter();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");
    setIsSubmitting(true);

    if (password.length < 6) {
      setError("Password must be at least 6 characters long.");
      setIsSubmitting(false);
      return;
    }

    const success = await register(username, password, phoneNumber);

    if (success) {
      setMessage("Registration successful! Please log in.");
      // After successful registration, gently push them back to login after 2 seconds
      setTimeout(() => {
        router.push("/login");
      }, 2000);
    } else {
      // Error handling for existing user, etc.
      setError(
        "Registration failed. Username may already be taken or server error occurred."
      );
    }

    setIsSubmitting(false);
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-100 p-4">
      <div className="w-full max-w-md bg-white shadow-2xl rounded-xl p-8">
        <h1 className="text-4xl font-extrabold text-center text-green-700 mb-6 tracking-tight">
          Chatter Sign Up
        </h1>
        <p className="text-center text-gray-500 mb-8">
          Join Chatter and start connecting.
        </p>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label
              htmlFor="username"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              Username
            </label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500 transition duration-150 shadow-sm"
              placeholder="Choose your username"
            />
          </div>

          <div>
            <label
              htmlFor="password"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              Password (Min 6 chars)
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500 transition duration-150 shadow-sm"
              placeholder="••••••••"
            />
          </div>

          <div>
            <label
              htmlFor="phoneNumber"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              Phone Number (Optional)
            </label>
            <input
              id="phoneNumber"
              type="text"
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500 transition duration-150 shadow-sm"
              placeholder="+1 555-555-5555"
            />
          </div>

          {error && (
            <div className="p-3 text-sm text-red-700 bg-red-100 rounded-lg text-center font-medium">
              {error}
            </div>
          )}

          {message && (
            <div className="p-3 text-sm text-green-700 bg-green-100 rounded-lg text-center font-medium">
              {message}
            </div>
          )}

          <button
            type="submit"
            disabled={isSubmitting || message}
            className={`w-full flex justify-center items-center py-3 px-4 border border-transparent rounded-lg shadow-lg text-lg font-semibold text-white transition duration-200 
                            ${
                              isSubmitting || message
                                ? "bg-green-400 cursor-not-allowed"
                                : "bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
                            }`}
          >
            {isSubmitting ? (
              <svg
                className="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
              >
                <circle
                  className="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  strokeWidth="4"
                ></circle>
                <path
                  className="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                ></path>
              </svg>
            ) : (
              "Create Account"
            )}
          </button>
        </form>

        <div className="mt-8 text-center text-sm">
          <p className="text-gray-600">
            Already have an account?{" "}
            <Link
              href="/login"
              className="font-medium text-green-600 hover:text-green-500"
            >
              Sign In
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
