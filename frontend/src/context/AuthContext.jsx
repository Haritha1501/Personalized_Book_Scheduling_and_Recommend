import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('readquest_user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  
  const [token, setToken] = useState(() => {
    return localStorage.getItem('readquest_token') || null;
  });

  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Setup Axios request interception
    const requestInterceptor = axios.interceptors.request.use(
      (config) => {
        const storedToken = localStorage.getItem('readquest_token');
        if (storedToken) {
          config.headers['Authorization'] = `Bearer ${storedToken}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Setup Axios response interception to handle 401s (token renewal)
    const responseInterceptor = axios.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config;
        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;
          const refreshToken = localStorage.getItem('readquest_refresh_token');
          if (refreshToken) {
            try {
              const res = await axios.post('/api/auth/refreshtoken', { refreshToken });
              const { accessToken, refreshToken: newRefreshToken } = res.data;
              
              localStorage.setItem('readquest_token', accessToken);
              localStorage.setItem('readquest_refresh_token', newRefreshToken);
              setToken(accessToken);

              originalRequest.headers['Authorization'] = `Bearer ${accessToken}`;
              return axios(originalRequest);
            } catch (refreshError) {
              // Refresh token failed, perform logout
              logout();
            }
          } else {
            logout();
          }
        }
        return Promise.reject(error);
      }
    );

    setLoading(false);

    return () => {
      axios.interceptors.request.eject(requestInterceptor);
      axios.interceptors.response.eject(responseInterceptor);
    };
  }, []);

  const login = async (username, password) => {
    const res = await axios.post('/api/auth/login', { username, password });
    const data = res.data;
    
    localStorage.setItem('readquest_token', data.token);
    localStorage.setItem('readquest_refresh_token', data.refreshToken);
    localStorage.setItem('readquest_user', JSON.stringify(data));
    
    setToken(data.token);
    setUser(data);
    return data;
  };

  const signup = async (username, email, password) => {
    await axios.post('/api/auth/signup', { username, email, password });
  };

  const logout = async () => {
    try {
      await axios.post('/api/auth/logout');
    } catch (e) {
      // Ignore network errors on logout
    }
    localStorage.removeItem('readquest_token');
    localStorage.removeItem('readquest_refresh_token');
    localStorage.removeItem('readquest_user');
    setToken(null);
    setUser(null);
  };

  const updateUserProfile = (updatedDetails) => {
    const updated = { ...user, ...updatedDetails };
    localStorage.setItem('readquest_user', JSON.stringify(updated));
    setUser(updated);
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, signup, logout, updateUserProfile }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
