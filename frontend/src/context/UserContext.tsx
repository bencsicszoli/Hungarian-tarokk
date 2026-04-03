import { createContext, useContext, useState, useEffect } from "react";
import type { ReactNode } from "react";
import type {User, UserContextType} from "../Types";

const UserContext = createContext<UserContextType | null>(null);

export function UserProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem("jwtToken") || null);
  const logout = () => setUser(null);

  useEffect(() => {
    if (token) {
      localStorage.setItem("jwtToken", token);
    } else {
      localStorage.removeItem("jwtToken");
    }
  }, [token]);

  return (
    <UserContext.Provider
      value={{ user, setUser, logout, token, setToken }}
    >
      {children}
    </UserContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useUser() {
  return useContext(UserContext);
}
