import { createContext, useContext, useState, useEffect } from "react";
import type { ReactNode } from "react";

interface Player {
  id: string;
  name: string;
  email: string;
  // Add other player properties as needed
}

interface PlayerContextType {
  player: Player | null;
  setPlayer: (player: Player | null) => void;
  logout: () => void;
  token: string | null;
  setToken: (token: string | null) => void;
}

const PlayerContext = createContext<PlayerContextType | null>(null);

export function PlayerProvider({ children }: { children: ReactNode }) {
  const [player, setPlayer] = useState<Player | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem("jwtToken") || null);
  const logout = () => setPlayer(null);

  useEffect(() => {
    if (token) {
      localStorage.setItem("jwtToken", token);
    } else {
      localStorage.removeItem("jwtToken");
    }
  }, [token]);

  return (
    <PlayerContext.Provider
      value={{ player, setPlayer, logout, token, setToken }}
    >
      {children}
    </PlayerContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function usePlayer() {
  return useContext(PlayerContext);
}
