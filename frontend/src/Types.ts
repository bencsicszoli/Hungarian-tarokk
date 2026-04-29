export interface User {
  id: string;
  username: string;
  email: string;
  // Add other player properties as needed
}

export interface UserContextType {
  user: User | null;
  setUser: (user: User | null) => void;
  logout: () => void;
  token: string | null;
  setToken: (token: string | null) => void;
}

export interface SeatAttributes {
    playerSeat: string | null;
    playerBalance: number;
    playerCardsNumber: number;
    playerTrickCards: number;
}

export interface Card {
  cardId: number;
  imagePath: string;
  clickable: boolean;
}

export interface TrickCard extends Card {
  x: number;
  y: number;
  rotation: number;
}