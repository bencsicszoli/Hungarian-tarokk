export interface LocalizedMessage {
  key: string;
  params?: Record<string, unknown>;
}

export type InfoLine = string | LocalizedMessage;

export interface User {
  id: string;
  username: string;
  email: string;
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

export interface CardImage {
  frontImagePath: string;
}

export interface PlayerData {
  playerCardsNumber: number;
  playerTrickCards: number;
  playerBalance: number;
}

export type GameState =
  | "NEW"
  | "IN_PROGRESS"
  | "BIDDING"
  | "TALON_PICK_UP"
  | "SKART_LAY_DOWN"
  | "BONUS_ANNOUNCEMENT"
  | "TRICK_PHASE"
  | "FINISHED";
