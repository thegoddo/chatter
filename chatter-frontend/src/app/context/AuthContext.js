"use client";

import axios from "axios";
import { createContext, useContext, useEffect, useState } from "react";

const AuthContext = createContext();

export const useAuth = () => {
  return useContext(AuthContext);
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);

  // Safely initialize token by checking for the browser environment (window)
  const [token, setToken] = useState(() => {
    if (typeof window !== "undefined") {
      return localStorage.getItem("token");
    }
    return null;
  });

  const [isLoading, setIsLoading] = useState(true);

  const API_BASE_URL = "http://localhost:8080/api/auth";

  // Define logout here so the useEffect can use it without adding it to dependencies
  const logout = () => {
    setUser(null);
    setToken(null);
    if (typeof window !== "undefined") {
      localStorage.removeItem("token");
      localStorage.removeItem("username");
    }
    delete axios.defaults.headers.common["Authorization"];
  };

  // Effect to manage the global axios header and load initial user data
  useEffect(() => {
    if (token) {
      axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;

      const storedUsername = localStorage.getItem("username");

      if (storedUsername) {
        // If token and username exist, set the user object
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setUser({ username: storedUsername });
      } else {
        // If token exists but username is missing (bad state), force cleanup
        console.warn(
          "Token exists but username is missing from storage. Logging out."
        );
        logout();
      }
    } else {
      // If no token, ensure environment is clean
      delete axios.defaults.headers.common["Authorization"];
      if (user) {
        setUser(null);
      }
    }

    // Auth status is ready once this effect runs on mount or token change
    setIsLoading(false);
    // FIX: Dependency array now relies only on 'token' to run on initial load and token changes.
    // This prevents race conditions where the check might run before 'user' is fully set.
  }, [token]);

  const login = async (username, password) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/login`, {
        username,
        password,
      });

      const jwtToken = response.data;

      setToken(jwtToken);
      setUser({ username });

      if (typeof window !== "undefined") {
        localStorage.setItem("token", jwtToken);
        localStorage.setItem("username", username);
      }

      // Set the header immediately
      axios.defaults.headers.common["Authorization"] = `Bearer ${jwtToken}`;

      return true;
    } catch (error) {
      console.error("Login failed: ", error);
      return false;
    }
  };

  const register = async (username, password, phoneNumber) => {
    try {
      await axios.post(`${API_BASE_URL}/register`, {
        username,
        password,
        phoneNumber,
        status: "OFFLINE",
      });

      return true;
    } catch (error) {
      console.error("Registration failed: ", error);
      return false;
    }
  };

  const value = {
    user,
    token,
    isLoading,
    login,
    register,
    logout,
    isAuthenticated: !!token,
  };

  return (
    <AuthContext.Provider value={value}>
      {/* Ensure children are only rendered once we've finished the initial loading check */}
      {!isLoading && children}
    </AuthContext.Provider>
  );
};
