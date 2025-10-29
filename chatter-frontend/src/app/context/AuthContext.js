"use client";

import axios from "axios";
import { createContext, useContext, useEffect, useState } from "react";

const AuthContext = createContext();

export const useAuth = () => {
  return useContext(AuthContext);
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(() => localStorage.getItem("token"));
  const [isLoading, setIsLoading] = useState(true);

  const API_BASE_URL = "http://localhost:8080/api/auth";

  useEffect(() => {
    if (token) {
      axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    } else {
      delete axios.defaults.headers.common["Authorization"];
    }
  }, [token]);

  useEffect(() => {
    const timer = setTimeout(() => {
      setIsLoading(false);
    }, 0);
    return () => clearTimeout(timer);
  }, []);

  const login = async (username, password) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/login`, {
        username,
        password,
      });

      const jwtToken = response.data;

      setToken(jwtToken);
      setUser({ username });
      localStorage.setItem("token", jwtToken);
      localStorage.setItem("username", username);
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

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    delete axios.defaults.headers.common["Authorization"];
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
      {!isLoading && children}
    </AuthContext.Provider>
  );
};
