import type { User, PlayerData } from "./Types";

export function createPrivateInfo(
  selectedTarokkNumber: number,
  calledTarokk: string | null,
  selectedBonuses: string[],
) {
  let privateInfo = "";
  if (selectedTarokkNumber > 0) {
    privateInfo += `You announced ${selectedTarokkNumber} tarokks!`;
  }
  if (calledTarokk) {
    privateInfo += ` ${calledTarokk}!`;
  }
  if (selectedBonuses.length > 0) {
    privateInfo += ` Selected bonuses: ${selectedBonuses.join(", ")}!`;
  }
  return privateInfo;
}

export function setPlayerProperties(
  place: "NORTH" | "EAST" | "WEST",
  user: User | null,
  player1: string | null,
  player2: string | null,
  player3: string | null,
  player4: string | null,
  playerData: Record<string, PlayerData>,
) {
  let playerName;
  let playerCardsNumber = 0;
  let playerBalance = 0;
  switch (place) {
    case "NORTH":
      switch (user?.username) {
        case player1:
          playerName = player3;
          playerCardsNumber = player3
            ? playerData[player3]?.playerCardsNumber || 0
            : 0;
          playerBalance = player3 ? playerData[player3]?.playerBalance || 0 : 0;
          break;
        case player2:
          playerName = player4;
          playerCardsNumber = player4
            ? playerData[player4]?.playerCardsNumber || 0
            : 0;
          playerBalance = player4 ? playerData[player4]?.playerBalance || 0 : 0;
          console.log("PlayerName: ", playerName);
          console.log("PlayerCardsNumber: ", playerCardsNumber);
          console.log("PlayerBalance: ", playerBalance);
          break;
        case player3:
          playerName = player1;
          playerCardsNumber = player1
            ? playerData[player1]?.playerCardsNumber || 0
            : 0;
          playerBalance = player1 ? playerData[player1]?.playerBalance || 0 : 0;
          console.log("PlayerName: ", playerName);
          console.log("PlayerCardsNumber: ", playerCardsNumber);
          console.log("PlayerBalance: ", playerBalance);
          break;
        case player4:
          playerName = player2;
          playerCardsNumber = player2
            ? playerData[player2]?.playerCardsNumber || 0
            : 0;
          playerBalance = player2 ? playerData[player2]?.playerBalance || 0 : 0;
          console.log("PlayerName: ", playerName);
          console.log("PlayerCardsNumber: ", playerCardsNumber);
          console.log("PlayerBalance: ", playerBalance);
          break;
      }
      break;
    case "EAST":
      switch (user?.username) {
        case player1:
          playerName = player2;
          playerCardsNumber = player2
            ? playerData[player2]?.playerCardsNumber || 0
            : 0;
          playerBalance = player2 ? playerData[player2]?.playerBalance || 0 : 0;
          break;
        case player2:
          playerName = player3;
          playerCardsNumber = player3
            ? playerData[player3]?.playerCardsNumber || 0
            : 0;
          playerBalance = player3 ? playerData[player3]?.playerBalance || 0 : 0;
          break;
        case player3:
          playerName = player4;
          playerCardsNumber = player4
            ? playerData[player4]?.playerCardsNumber || 0
            : 0;
          playerBalance = player4 ? playerData[player4]?.playerBalance || 0 : 0;
          break;
        case player4:
          playerName = player1;
          playerCardsNumber = player1
            ? playerData[player1]?.playerCardsNumber || 0
            : 0;
          playerBalance = player1 ? playerData[player1]?.playerBalance || 0 : 0;
          break;
      }
      break;
    case "WEST":
      switch (user?.username) {
        case player1:
          playerName = player4;
          playerCardsNumber = player4
            ? playerData[player4]?.playerCardsNumber || 0
            : 0;
          playerBalance = player4 ? playerData[player4]?.playerBalance || 0 : 0;
          break;
        case player2:
          playerName = player1;
          playerCardsNumber = player1
            ? playerData[player1]?.playerCardsNumber || 0
            : 0;
          playerBalance = player1 ? playerData[player1]?.playerBalance || 0 : 0;
          break;
        case player3:
          playerName = player2;
          playerCardsNumber = player2
            ? playerData[player2]?.playerCardsNumber || 0
            : 0;
          playerBalance = player2 ? playerData[player2]?.playerBalance || 0 : 0;
          break;
        case player4:
          playerName = player3;
          playerCardsNumber = player3
            ? playerData[player3]?.playerCardsNumber || 0
            : 0;
          playerBalance = player3 ? playerData[player3]?.playerBalance || 0 : 0;
          break;
      }
      break;
  }
  return { playerName, playerCardsNumber, playerBalance };
}

export function setPlayerPropertiesInTrickPhase(
  player1: string | null,
  player2: string | null,
  player3: string | null,
  player4: string | null,
  firstPlayer: string | null,
  secondPlayer: string | null,
  thirdPlayer: string | null,
  fourthPlayer: string | null,
  user: User | null,
  playerData: Record<string, PlayerData>,
) {
  let playerName;
  let playerCardsNumber = 0;
  let playerTrickCards = 0;
  let playerBalance = 0;
  switch (user?.username) {
    case player1:
      playerName = firstPlayer;
      playerCardsNumber = firstPlayer
        ? playerData[firstPlayer]?.playerCardsNumber || 0
        : 0;
      playerTrickCards = firstPlayer
        ? playerData[firstPlayer]?.playerTrickCards || 0
        : 0;
      playerBalance = firstPlayer ? playerData[firstPlayer]?.playerBalance || 0 : 0;
      break;
    case player2:
      playerName = secondPlayer;
      playerCardsNumber = secondPlayer
        ? playerData[secondPlayer]?.playerCardsNumber || 0
        : 0;
      playerTrickCards = secondPlayer
        ? playerData[secondPlayer]?.playerTrickCards || 0
        : 0;
      playerBalance = secondPlayer ? playerData[secondPlayer]?.playerBalance || 0 : 0;
      break;
    case player3:
      playerName = thirdPlayer;
      playerCardsNumber = thirdPlayer
        ? playerData[thirdPlayer]?.playerCardsNumber || 0
        : 0;
      playerTrickCards = thirdPlayer
        ? playerData[thirdPlayer]?.playerTrickCards || 0
        : 0;
      playerBalance = thirdPlayer ? playerData[thirdPlayer]?.playerBalance || 0 : 0;
      break;
    case player4:
      playerName = fourthPlayer;
      playerCardsNumber = fourthPlayer
        ? playerData[fourthPlayer]?.playerCardsNumber || 0
        : 0;
      playerTrickCards = fourthPlayer
        ? playerData[fourthPlayer]?.playerTrickCards || 0
        : 0;
      playerBalance = fourthPlayer ? playerData[fourthPlayer]?.playerBalance || 0 : 0;
      break;
  }
  return { playerName, playerCardsNumber, playerTrickCards, playerBalance };
}
