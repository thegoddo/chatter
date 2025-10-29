"use client";
const { useState, useEffect } = require("react");
const { useAuth } = require("../context/AuthContext");
import { useRouter } from "next/navigation";
import Link from "next/link";

const LoginPage = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { login, isAuthenticated } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (isAuthenticated) {
      router.push("/chat");
    }
  }, [isAuthenticated, router]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    setError("");
    setIsSubmitting(true);

    if (!username || !password) {
      setError("Please enter both username and password.");
      setIsSubmitting(false);
      return;
    }

    const success = await login(username, password);

    if (success) {
      router.push("/push");
    } else {
      setError("Login failed. Please check your credentials.");
    }

    setIsSubmitting(false);
  };

  if (isAuthenticated) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gray-50">
        <p className="text-xl text-gray-700">Redirecting to chat...</p>
      </div>
    );
  }

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-100 p-4">
      <div className="w-full max-w-md bg-white shadow-2xl rounded-xl p-8 transition-all duration-300 transform hover:scale-[1.02]">
        <h1 className="text-4xl font-extrabold text-center text-indigo-700 mb-6 tracking-tight">
          Chatter Login
        </h1>

        <p className="text-center text-gray-500 mb-8">
          Sign in to join conversation.
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
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500 transition duration-150 shadow-sm"
              placeholder="Your unique username"
            />
          </div>

          <div>
            <label
              htmlFor="password"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              Password
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500 transition duration-150 shadow-sm"
              placeholder="••••••••"
            />
          </div>
          {error && (
            <div className="p-3 text-sm text-red-700 bg-red-100 rounded-lg text-center font-medium">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={isSubmitting}
            className={`w-full flex justify-center items-center py-3 px-4 border border-transparent rounded-lg shadow-lg text-lg font-semibold text-white transition duration-200 
                            ${
                              isSubmitting
                                ? "bg-indigo-400 cursor-not-allowed"
                                : "bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
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
              "Sign In"
            )}
          </button>
        </form>

        <div className="mt-8 text-center text-sm">
          <p className="text-gray-600">
            Don&apos;t have an account?{" "}
            <Link
              href="/register"
              className="font-medium text-indigo-600 hover:text-indigo-500"
            >
              Create Account
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
