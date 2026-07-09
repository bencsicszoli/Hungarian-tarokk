import { useState, useEffect, useRef, useCallback } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useLocation } from "react-router-dom";
import { useUser } from "./context/UserContext";
import { useWebSocket } from "./context/WebSocketContext";
import InfoTable from "./InfoTable.tsx";
import Talon from "./gamePageComponents/Talon.tsx";
import Skart from "./gamePageComponents/Skart.tsx";
import type {
  Card,
  TrickCard,
  PlayerData,
  CardImage,
  GameState,
  InfoLine,
  LocalizedMessage,
  BonusOption,
} from "./types.ts";
import {
  translateInfoLines,
  translateMessage,
  translateMessages,
} from "./i18n/translateMessage.ts";
import Bonuses from "./gamePageComponents/Bonuses.tsx";
import PublicDescardedHand from "./gamePageComponents/PublicDiscardedHand.tsx";
import MenuButtons from "./gamePageComponents/MenuButtons.tsx";
import {
  setPlayerProperties,
  setPlayerPropertiesInTrickPhase,
  setOwnProperties,
} from "./utils.ts";
import DeclarerSkart from "./gamePageComponents/DeclarerSkart.tsx";
import OpponentSkart from "./gamePageComponents/OpponentSkart.tsx";
import OwnHand from "./gamePageComponents/OwnHand.tsx";
import DisposableHandInfo from "./gamePageComponents/DisposableHandInfo.tsx";
import NorthernPlayerHand from "./gamePageComponents/NorthernPlayerhand.tsx";
import EasternPlayerHand from "./gamePageComponents/EasternPlayerHand.tsx";
import WesternPlayerHand from "./gamePageComponents/WesternPlayerHand.tsx";
import DeclarerPublicTricks from "./gamePageComponents/DeclarerPublicTricks.tsx";
import OpeningImage from "./gamePageComponents/OpeningImage.tsx";
import TrickPlace from "./gamePageComponents/TrickPlace.tsx";
import LogoutWarning from "./gamePageComponents/LogoutWarning.tsx";

const INITIAL_ROTATION = [
  0, 0, -7.5, -15, -22.5, -30, -37.5, -45, -52.5, -60, -67.5, -75, -82.5,
];

function Game() {
  const { t } = useTranslation();
  const location = useLocation();
  const navigate = useNavigate();
  const userContext = useUser();
  const webSocketContext = useWebSocket();
  const { game } = location.state || {};
  const { user, setToken, setUser } = userContext || {
    user: null,
    setToken: () => {},
    setUser: () => {},
  };
  const { subscribe, send, connected } = webSocketContext || {
    subscribe: () => null,
    send: () => {},
    connected: false,
  };

  const [bid, setBid] = useState<"-" | "3" | "2" | "1" | "solo">("-");
  const [callableTarokks, setCallableTarokks] = useState<LocalizedMessage[]>(
    [],
  );
  const [calledTarokk, setCalledTarokk] = useState<string | null>(null);
  const [dealButtonClicked, setDealButtonClicked] = useState<boolean>(false);
  const [dealer, setDealer] = useState<string | null>(game?.dealer || null);
  const [declarer, setDeclarer] = useState<string | null>(
    game?.declarer || null,
  );
  const [declarerBonuses, setDeclarerBonuses] = useState<string | null>(null);
  const [declarerSkart, setDeclarerSkart] = useState<Card[]>([]);
  const [declarerSkartLength, setDeclarerSkartLength] = useState<number>(0);
  const [discardInformation, setDiscardInformation] = useState<
    InfoLine[] | null
  >(null);
  const [firstBonusRound, setFirstBonusRound] = useState<boolean>(true);
  const [gameState, setGameState] = useState<GameState>(
    game?.gameState || "NEW",
  );
  const [hasEightTarokks, setHasEightTarokks] = useState<boolean>(false);
  const [hasNineTarokks, setHasNineTarokks] = useState<boolean>(false);
  const [isGameNew, setIsGameNew] = useState<boolean>(true);
  const [opponentBonuses, setOpponentBonuses] = useState<string | null>(null);
  const [opponentSkartLength, setOpponentSkartLength] = useState<number>(0);
  const [ownCards, setOwnCards] = useState<Card[]>([]);
  const [logoutWarning, setLogoutWarning] = useState<InfoLine[] | null>(null);
  const initialPlayerData: Record<string, PlayerData> = {};
  if (game?.player1) {
    initialPlayerData[game.player1] = {
      playerCardsNumber: game.player1CardsNumber || 0,
      playerTrickCards: 0,
      playerBalance: game.player1Balance || 0,
    };
  }
  if (game?.player2) {
    initialPlayerData[game.player2] = {
      playerCardsNumber: game.player2CardsNumber || 0,
      playerTrickCards: 0,
      playerBalance: game.player2Balance || 0,
    };
  }
  if (game?.player3) {
    initialPlayerData[game.player3] = {
      playerCardsNumber: game.player3CardsNumber || 0,
      playerTrickCards: 0,
      playerBalance: game.player3Balance || 0,
    };
  }
  if (game?.player4) {
    initialPlayerData[game.player4] = {
      playerCardsNumber: game.player4CardsNumber || 0,
      playerTrickCards: 0,
      playerBalance: game.player4Balance || 0,
    };
  }
  const [playerData, setPlayerData] =
    useState<Record<string, PlayerData>>(initialPlayerData);
  const [player1, setPlayer1] = useState<string | null>(game?.player1 || null);
  const [player2, setPlayer2] = useState<string | null>(game?.player2 || null);
  const [player3, setPlayer3] = useState<string | null>(game?.player3 || null);
  const [player4, setPlayer4] = useState<string | null>(game?.player4 || null);
  const [potentialBids, setPotentialBids] = useState<string[]>([]);
  const [potentialBonuses, setPotentialBonuses] = useState<BonusOption[]>([]);
  const [privateInformation, setPrivateInformation] = useState<InfoLine[]>(
    game?.privateInformation || [],
  );
  const [publicDeclarerSkart, setPublicDeclarerSkart] = useState<CardImage[]>(
    [],
  );
  const [publicDeclarerTricks, setPublicDeclarerTricks] = useState<CardImage[]>(
    [],
  );
  const [publicDiscardedHand, setPublicDiscardedHand] = useState<Card[]>([]);

  const [publicOpponentSkart, setPublicOpponentSkart] = useState<CardImage[]>(
    [],
  );
  const [publicOpponentTricks, setPublicOpponentTricks] = useState<CardImage[]>(
    [],
  );
  const [publicInformation, setPublicInformation] = useState<InfoLine[]>(
    game?.information || [],
  );
  const [selectedTarokkNumber, setSelectedTarokkNumber] = useState<number>(0);
  const [selectedBonuses, setSelectedBonuses] = useState<string[]>([]);
  const [startPlayer, setStartPlayer] = useState<string | null>(
    game?.startPlayer || null,
  );
  const [talonCardsNumber, setTalonCardsNumber] = useState<number>(0);
  const [tarokkNumberSent, setTarokkNumberSent] = useState<boolean>(false);
  const [temporarySelectedCards, setTemporarySelectedCards] = useState<Card[]>(
    [],
  );
  const [trickCard, setTrickCard] = useState<Card | null>(null);
  const [trickCards, setTrickCards] = useState<TrickCard[]>([]);
  const [trickWinnerDirection, setTrickWinnerDirection] = useState<
    "north" | "east" | "south" | "west" | null
  >(null);
  const [turnPlayer, setTurnPlayer] = useState<string | null>(
    game?.turnPlayer || null,
  );

  const cardsToDiscard = useRef<number>(0);
  const discardPromptShown = useRef<boolean>(false);
  const playerTrickCountsRef = useRef<Record<string, number>>({});
  const trickCardIdsRef = useRef<Set<number>>(new Set());
  const cardAnimationsRef = useRef<Map<number, string>>(new Map());

  const handlePublicMessage = useCallback(
    (payload: unknown) => {
      const message = JSON.parse(
        (payload as Record<string, unknown>).body as string,
      );
      console.log("Public message received:", message);
      switch (message.type) {
        case "game.joined":
          setPlayer1(message.player1);
          setPlayer2(message.player2);
          setPlayer3(message.player3);
          setPlayer4(message.player4);
          setDealer(message.dealer);
          setStartPlayer(message.startPlayer);
          setTurnPlayer(message.turnPlayer);
          setGameState(message.gameState);
          setPublicInformation(message.information || []);
          setPlayerData({
            ...(message.player1
              ? {
                  [message.player1]: {
                    playerCardsNumber: message.player1CardsNumber || 0,
                    playerTrickCards: 0,
                    playerBalance: message.player1Balance || 0,
                  },
                }
              : {}),
            ...(message.player2
              ? {
                  [message.player2]: {
                    playerCardsNumber: message.player2CardsNumber || 0,
                    playerTrickCards: 0,
                    playerBalance: message.player2Balance || 0,
                  },
                }
              : {}),
            ...(message.player3
              ? {
                  [message.player3]: {
                    playerCardsNumber: message.player3CardsNumber || 0,
                    playerTrickCards: 0,
                    playerBalance: message.player3Balance || 0,
                  },
                }
              : {}),
            ...(message.player4
              ? {
                  [message.player4]: {
                    playerCardsNumber: message.player4CardsNumber || 0,
                    playerTrickCards: 0,
                    playerBalance: message.player4Balance || 0,
                  },
                }
              : {}),
          });

          break;
        case "game.newRound":
          setFirstBonusRound(true);
          setDeclarer(null);
          setBid("-");
          setDeclarerSkart([]);
          setDeclarerSkartLength(0);
          setOpponentSkartLength(0);
          setDiscardInformation(null);
          setPublicDiscardedHand([]);
          setDeclarerBonuses(null);
          setOpponentBonuses(null);
          setPublicInformation([message.dealerInfo]);
          setIsGameNew(false);
          setPublicDeclarerSkart([]);
          setPublicOpponentSkart([]);
          setPublicDeclarerTricks([]);
          setPublicOpponentTricks([]);
          setPlayerData((prev) => ({
            ...prev,
            ...(player1
              ? {
                  [player1]: { ...prev[player1], playerTrickCards: 0 },
                }
              : {}),
            ...(player2
              ? {
                  [player2]: { ...prev[player2], playerTrickCards: 0 },
                }
              : {}),
            ...(player3
              ? {
                  [player3]: { ...prev[player3], playerTrickCards: 0 },
                }
              : {}),
            ...(player4
              ? {
                  [player4]: { ...prev[player4], playerTrickCards: 0 },
                }
              : {}),
          }));
          break;
        case "game.talon":
          setFirstBonusRound(true);
          setGameState(message.gameState);
          setTalonCardsNumber(message.talonCards);
          console.log("Talon cards number updated:", message.talonCards);
          break;
        case "game.cardNumber":
          console.log("Card number update received for", message.username);
          setPlayerData((prev) => ({
            ...prev,
            ...(message.username
              ? {
                  [message.username]: {
                    ...prev[message.username],
                    playerCardsNumber: message.cardsNumber || 0,
                  },
                }
              : {}),
          }));
          break;
        case "game.lastDeal":
          setGameState("BIDDING");
          setPublicInformation([t("game.biddingPhaseStarted")]);
          setPrivateInformation([]);
          setPlayerData((prev) => ({
            ...prev,
            ...(message.username
              ? {
                  [message.username]: {
                    ...prev[message.username],
                    playerCardsNumber: message.cardsNumber || 0,
                  },
                }
              : {}),
          }));
          console.log("Game state updated to BIDDING");
          break;
        case "game.publicBidInfo":
          setBid(message.bid);
          setDeclarer(message.declarer);
          setTurnPlayer(message.turnPlayer);
          setPublicInformation([message.info]);
          console.log("Public bid info updated:", message);
          break;
        case "game.fourPasses":
          setPublicInformation(message.info);
          setTurnPlayer(message.turnPlayer);
          setGameState("NEW");
          setPlayerData((prev) => ({
            ...prev,
            ...(player1
              ? { [player1]: { ...prev[player1], playerCardsNumber: 0 } }
              : {}),
            ...(player2
              ? { [player2]: { ...prev[player2], playerCardsNumber: 0 } }
              : {}),
            ...(player3
              ? { [player3]: { ...prev[player3], playerCardsNumber: 0 } }
              : {}),
            ...(player4
              ? { [player4]: { ...prev[player4], playerCardsNumber: 0 } }
              : {}),
          }));
          setDealButtonClicked(false);
          setOwnCards([]);
          setPrivateInformation([]);
          setTalonCardsNumber(0);
          setIsGameNew(true);
          break;
        case "game.confirmLeaving":
          setPlayerData(message.playersData);
          setGameState("NEW");
          setPotentialBids([]);
          setDealer(null);
          setDealButtonClicked(false);
          setTurnPlayer(null);
          setStartPlayer(null);
          setFirstBonusRound(true);
          setDeclarer(null);
          setBid("-");
          setDeclarerSkart([]);
          setDeclarerSkartLength(0);
          setOpponentSkartLength(0);
          setDiscardInformation(null);
          setPublicDiscardedHand([]);
          setDeclarerBonuses(null);
          setOpponentBonuses(null);
          setPublicInformation([message.info]);
          setIsGameNew(true);
          setPublicDeclarerSkart([]);
          setPublicOpponentSkart([]);
          setPublicDeclarerTricks([]);
          setPublicOpponentTricks([]);
          setPrivateInformation([message.info]);
          setOwnCards([]);
          setTalonCardsNumber(0);
          switch (message.playerName) {
            case player1:
              setPlayer1(null);
              break;
            case player2:
              setPlayer2(null);
              break;
            case player3:
              setPlayer3(null);
              break;
            case player4:
              setPlayer4(null);
              break;
          }
          break;
        case "game.gameState":
          setGameState(message.gameState);
          console.log("Game state updated:", message.gameState);
          break;
        case "game.gameStateInfo":
          setGameState(message.gameState);
          setPublicInformation([message.info]);
          console.log(
            "Game state and info updated:",
            message.gameState,
            message.info,
          );
          break;
        case "game.publicSkartInfo":
          setDeclarerSkartLength(message.declarerSkartLength);
          setOpponentSkartLength(message.opponentSkartLength);
          setPlayerData((prev) => ({
            ...prev,
            ...(message.username
              ? {
                  [message.username]: {
                    ...prev[message.username],
                    playerCardsNumber: message.playerHandLength || 0,
                  },
                }
              : {}),
          }));
          setTurnPlayer(message.turnPlayer);
          setPublicInformation([message.discardedCardsInfo]);
          console.log("Public skart info updated:", message);
          break;
        case "game.publicInfo":
          setPublicInformation(message.info);
          console.log("Public information updated:", message.info);
          break;
        case "game.discardHand":
          setDeclarer(null);
          setTurnPlayer(message.turnPlayer);
          setBid("-");
          setPublicDiscardedHand(message.cards);
          setPublicInformation(message.info);
          setDeclarerSkart([]);
          setDeclarerSkartLength(0);
          setOpponentSkartLength(0);
          setGameState("NEW");
          setPlayerData((prev) => ({
            ...prev,
            ...(player1
              ? { [player1]: { ...prev[player1], playerCardsNumber: 0 } }
              : {}),
            ...(player2
              ? { [player2]: { ...prev[player2], playerCardsNumber: 0 } }
              : {}),
            ...(player3
              ? { [player3]: { ...prev[player3], playerCardsNumber: 0 } }
              : {}),
            ...(player4
              ? { [player4]: { ...prev[player4], playerCardsNumber: 0 } }
              : {}),
          }));
          setDealButtonClicked(false);
          setOwnCards([]);
          setPrivateInformation([]);
          console.log("Public discard hand info updated:", message.info);
          break;
        case "game.tarokkInSkart":
          setDeclarerSkart(message.cards);
          console.log("Declarer's skart with tarokk updated:", message.cards);
          break;
        case "game.publicSkartPhaseFinishInfo":
          setPublicInformation((prev) => [...prev, ...message.info]);
          console.log("Public skart phase finish info updated:", message.info);
          break;
        case "game.turnPlayer":
          setTurnPlayer(message.turnPlayer);
          console.log("Turn player updated:", message.turnPlayer);
          break;
        case "game.publicBonusInfo":
          setPublicInformation(message.info);
          setDeclarerBonuses(translateMessages(message.declarerBonuses).join(", "));
          setOpponentBonuses(translateMessages(message.opponentBonuses).join(", "));
          setTurnPlayer(message.turnPlayer);
          setFirstBonusRound(false);
          console.log("Public bonus info updated:", message.info);
          break;
        case "game.trickCards": {
          const newCards: TrickCard[] = message.cards;
          const turnName: string | null = message.turnName ?? null;

          if (turnName && user) {
            const newCard = newCards.find(
              (c) => !trickCardIdsRef.current.has(c.cardId),
            );
            if (newCard) {
              let dir: "north" | "east" | "south" | "west" | null = null;
              if (turnName === user.username) {
                dir = "south";
              } else {
                switch (user.username) {
                  case player1:
                    if (turnName === player2) dir = "east";
                    else if (turnName === player3) dir = "north";
                    else if (turnName === player4) dir = "west";
                    break;
                  case player2:
                    if (turnName === player3) dir = "east";
                    else if (turnName === player4) dir = "north";
                    else if (turnName === player1) dir = "west";
                    break;
                  case player3:
                    if (turnName === player4) dir = "east";
                    else if (turnName === player1) dir = "north";
                    else if (turnName === player2) dir = "west";
                    break;
                  case player4:
                    if (turnName === player1) dir = "east";
                    else if (turnName === player2) dir = "north";
                    else if (turnName === player3) dir = "west";
                    break;
                }
              }
              if (dir)
                cardAnimationsRef.current.set(
                  newCard.cardId,
                  `card-from-${dir}`,
                );
            }
          }

          trickCardIdsRef.current = new Set(newCards.map((c) => c.cardId));
          setTrickCards(newCards);
          setPlayerData((prev) => ({
            ...prev,
            ...(turnName
              ? {
                  [turnName]: {
                    ...prev[turnName],
                    playerCardsNumber: message.cardsInHand || 0,
                  },
                }
              : {}),
          }));
          console.log("Trick cards updated:", message);
          break;
        }
        case "game.newTrickRound": {
          const trickPlayers = [player1, player2, player3, player4];
          const newCounts: number[] = message.playerTricks;

          const winnerIndex = newCounts.findIndex((newCount, i) => {
            const name = trickPlayers[i];
            return (
              name !== null &&
              newCount > (playerTrickCountsRef.current[name] ?? 0)
            );
          });

          trickPlayers.forEach((name, i) => {
            if (name) playerTrickCountsRef.current[name] = newCounts[i];
          });

          if (winnerIndex >= 0) {
            const winnerName = trickPlayers[winnerIndex];
            if (winnerName && user) {
              let dir: "north" | "east" | "south" | "west" | null = null;
              if (winnerName === user.username) {
                dir = "south";
              } else {
                switch (user.username) {
                  case player1:
                    if (winnerName === player2) dir = "east";
                    else if (winnerName === player3) dir = "north";
                    else if (winnerName === player4) dir = "west";
                    break;
                  case player2:
                    if (winnerName === player3) dir = "east";
                    else if (winnerName === player4) dir = "north";
                    else if (winnerName === player1) dir = "west";
                    break;
                  case player3:
                    if (winnerName === player4) dir = "east";
                    else if (winnerName === player1) dir = "north";
                    else if (winnerName === player2) dir = "west";
                    break;
                  case player4:
                    if (winnerName === player1) dir = "east";
                    else if (winnerName === player2) dir = "north";
                    else if (winnerName === player3) dir = "west";
                    break;
                }
              }
              if (dir) setTrickWinnerDirection(dir);
            }
          }

          setPlayerData((prev) => ({
            ...prev,
            ...(player1
              ? {
                  [player1]: {
                    ...prev[player1],
                    playerTrickCards: newCounts[0],
                  },
                }
              : {}),
            ...(player2
              ? {
                  [player2]: {
                    ...prev[player2],
                    playerTrickCards: newCounts[1],
                  },
                }
              : {}),
            ...(player3
              ? {
                  [player3]: {
                    ...prev[player3],
                    playerTrickCards: newCounts[2],
                  },
                }
              : {}),
            ...(player4
              ? {
                  [player4]: {
                    ...prev[player4],
                    playerTrickCards: newCounts[3],
                  },
                }
              : {}),
          }));

          setTimeout(() => {
            setTrickCards([]);
            setTrickCard(null);
            setTrickWinnerDirection(null);
            trickCardIdsRef.current.clear();
            cardAnimationsRef.current.clear();
          }, 600);

          console.log("Trick reset message:", message);
          break;
        }
        case "game.newBalances":
          setPlayerData((prev) => ({
            ...prev,
            ...(player1
              ? {
                  [player1]: {
                    ...prev[player1],
                    playerBalance: message.player1Balance,
                  },
                }
              : {}),
            ...(player2
              ? {
                  [player2]: {
                    ...prev[player2],
                    playerBalance: message.player2Balance,
                  },
                }
              : {}),
            ...(player3
              ? {
                  [player3]: {
                    ...prev[player3],
                    playerBalance: message.player3Balance,
                  },
                }
              : {}),
            ...(player4
              ? {
                  [player4]: {
                    ...prev[player4],
                    playerBalance: message.player4Balance,
                  },
                }
              : {}),
          }));
          setDealer(message.newDealer);
          setStartPlayer(message.newStartPlayer);
          setTurnPlayer(message.newStartPlayer);
          setPublicInformation([
            t("game.newDealer", { name: message.newDealer.toUpperCase() }),
          ]);
          setDealButtonClicked(false);
          console.log("Balances updated:", message);
          break;
        case "game.declarerSkartImages":
          setPublicDeclarerSkart(message.images);
          break;
        case "game.opponentSkartImages":
          setPublicOpponentSkart(message.images);
          break;
        case "game.declarerTrickImages":
          setPublicDeclarerTricks(message.images);
          break;
        case "game.opponentTrickImages":
          setPublicOpponentTricks(message.images);
          break;
        default:
          console.log("Unhandled message type:", message.type);
          break;
      }
    },
    [player1, player2, player3, player4, user, t],
  );

  useEffect(() => {
    if (!user || !connected) navigate(`/`);
  }, [user, connected, navigate]);

  const handlePrivateMessage = useCallback((payload: unknown) => {
    const message = JSON.parse(
      (payload as Record<string, unknown>).body as string,
    );
    console.log("Private message received:", message);

    switch (message.type) {
      case "game.playerCards":
        setOwnCards(message.cards);
        console.log("Own cards updated:", message.cards);
        break;
      case "game.playerCardsWithTalon":
        setOwnCards(message.cards);
        cardsToDiscard.current = message.cards.length - 9;
        console.log("Own cards with talon updated:", message.cards);
        break;
      case "game.bid":
        console.log("Bid message received:", message);
        break;
      case "game.firstPotentialBids":
        console.log("First potential bids received:", message);
        setPotentialBids(message.potentialBids);
        break;
      case "game.potentialBids":
        console.log("Potential bids received:", message);
        setPotentialBids(message.potentialBids);
        break;
      case "game.discardHand":
        setDiscardInformation(message.info);
        console.log("Discard hand message received:", message);
        break;
      case "game.firstPotentialBonuses":
        console.log("First potential bonuses received:", message);
        setPotentialBonuses(message.bonuses);
        setCallableTarokks(message.callableTarokks);
        setHasEightTarokks(message.hasEightTarokks);
        setHasNineTarokks(message.hasNineTarokks);
        setPrivateInformation(message.info);
        break;
      case "game.privateInfo":
        setPrivateInformation(message.info);
        break;
      case "game.firstTurnPlayerBonuses":
        setHasEightTarokks(message.hasEightTarokks);
        setHasNineTarokks(message.hasNineTarokks);
        setPotentialBonuses(message.bonuses);
        setPrivateInformation(message.info);
        break;
      case "game.turnPlayerBonuses":
        setHasEightTarokks(message.hasEightTarokks);
        setHasNineTarokks(message.hasNineTarokks);
        setPotentialBonuses(message.bonuses);
        setPrivateInformation(message.info);
        break;
      case "game.ultimoValidation":
        setSelectedTarokkNumber(0);
        setCalledTarokk(null);
        setCallableTarokks([]);
        setSelectedBonuses([]);
        setFirstBonusRound(false);
        break;
      case "game.validation":
        setSelectedTarokkNumber(0);
        setSelectedBonuses([]);
        setFirstBonusRound(false);
        break;
      case "game.logoutWarning":
        setLogoutWarning(message.info);
        console.log("Logout warning: ", message.info);
        break;
      case "game.privateResult":
        setPrivateInformation(message.info);
        console.log("Private result message received:", message);
        break;
      default:
        console.log("Unhandled private message type:", message.type);
        break;
    }
  }, []);

  useEffect(() => {
    const publicSub = subscribe({
      destination: `/topic/game.${game.gameId}`,
      callback: handlePublicMessage,
    });
    const privateSub = subscribe({
      destination: `/user/queue/private`,
      callback: handlePrivateMessage,
    });
    return () => {
      publicSub?.unsubscribe();
      privateSub?.unsubscribe();
    };
  }, [game.gameId, subscribe, handlePublicMessage, handlePrivateMessage]);

  function getFirstPotentialBids() {
    send("/app/game.firstPotentialBids", {
      username: user?.username,
      gameId: game.gameId,
    });
    console.log(
      "Request for potential bids sent:",
      user?.username,
      game.gameId,
    );
  }

  function getFirstPotentialBonuses() {
    send("/app/game.firstPotentialBonuses", {
      username: user?.username,
      gameId: game.gameId,
    });
    console.log(
      "Request for potential bonuses sent:",
      user?.username,
      game.gameId,
    );
  }

  function sendSkartCards() {
    send("/app/game.discardSkart", {
      username: user?.username,
      gameId: game.gameId,
      cardsToSkart: temporarySelectedCards,
    });
    console.log("Skart cards sent:", temporarySelectedCards);
    cardsToDiscard.current = 0;
    setTemporarySelectedCards([]);
  }

  useEffect(() => {
    if (gameState === "BIDDING" && user?.username === startPlayer) {
      getFirstPotentialBids();
    }
  }, [gameState, user, startPlayer]);

  useEffect(() => {
    if (gameState === "BONUS_ANNOUNCEMENT" && user?.username === declarer) {
      getFirstPotentialBonuses();
    }
  }, [gameState, user, declarer]);

  useEffect(() => {
    if (
      gameState === "SKART_LAY_DOWN" &&
      turnPlayer === user?.username &&
      cardsToDiscard.current > 0
    ) {
      setPrivateInformation([
        t("game.selectCardsToDiscard", { count: cardsToDiscard.current }),
      ]);
      discardPromptShown.current = true;
    } else if (gameState === "SKART_LAY_DOWN") {
      setPrivateInformation([]);
      discardPromptShown.current = false;
    } else if (
      gameState === "BONUS_ANNOUNCEMENT" &&
      discardPromptShown.current
    ) {
      setPrivateInformation([]);
      discardPromptShown.current = false;
    }
  }, [gameState, turnPlayer, user, t]);

  function dealTalonToPlayers() {
    send("/app/game.dealTalonToPlayers", {
      username: user?.username,
      declarer: declarer,
      gameId: game.gameId,
      bid: bid,
    });
    console.log("Request to deal talon to players sent");
  }

  useEffect(() => {
    if (gameState === "TALON_PICK_UP" && declarer === user?.username) {
      dealTalonToPlayers();
    }
  }, [gameState, declarer]);

  function renderOwnHand() {
    const { playerName, playerTrickCards, playerBalance } = setOwnProperties(
      player1,
      player2,
      player3,
      player4,
      user,
      playerData,
    );
    return (
      <div className="flex flex-col h-full">
        <div className="h-1/8 flex justify-center items-center font-bold text-xl">
          <div className="w-1/8 flex justify-center items-center text-green-100">
            {playerTrickCards > 0 && (
              <p>{t("game.playerTricks", { name: playerName })}</p>
            )}
          </div>
          <div className="w-3/4 flex justify-center items-center mt-1">
            {playerName === turnPlayer ? (
              <p className="bg-green-300 text-[#2f4b3a] text-2xl rounded-md w-auto pt-0.5 pb-0.5 pl-3 pr-3">
                {playerName}
              </p>
            ) : (
              <p className="text-green-100">{playerName}</p>
            )}
          </div>
          <div className="w-1/8"></div>
        </div>
        {(gameState === "NEW" ||
          gameState === "TALON_PICK_UP" ||
          gameState === "BONUS_ANNOUNCEMENT") && (
          <div className="flex flex-col justify-center items-center h-full">
            <div className="flex justify-center items-center h-5/6">
              {displayOwnCards()}
            </div>
            <div className="h-1/6 flex justify-center items-center font-bold text-xl w-full text-green-100">
              <p>{t("game.balance", { balance: playerBalance })}</p>
            </div>
          </div>
        )}
        {gameState === "BIDDING" && (
          <div className="flex flex-col justify-center items-center h-full">
            <div className="flex justify-center items-center h-5/6">
              {displayOwnCards()}
            </div>
            <div className="flex justify-center items-center font-bold text-xl w-full h-1/6 -mt-3 gap-1">
              {renderBidButtons()}
            </div>
          </div>
        )}
        {gameState === "SKART_LAY_DOWN" && turnPlayer === user?.username && (
          <div className="w-full h-full flex flex-col justify-center items-center">
            <div className="h-5/6 flex justify-center items-center">
              {displayOwnCardsWithTalon()}
            </div>
            <div className="h-1/6 flex justify-center items-center font-bold text-xl w-full text-green-100">
              <p>{t("game.balance", { balance: playerBalance })}</p>
            </div>
          </div>
        )}
        {gameState === "SKART_LAY_DOWN" && turnPlayer !== user?.username && (
          <div className="w-full h-full flex flex-col justify-center items-center">
            <div className="h-5/6 flex justify-center items-center">
              {displayOwnCards()}
            </div>
            <div className="h-1/6 flex justify-center items-center font-bold text-xl w-full text-green-100">
              <p>{t("game.balance", { balance: playerBalance })}</p>
            </div>
          </div>
        )}
        {(gameState === "TRICK_PHASE" || gameState === "FINISHED") &&
          turnPlayer === user?.username && (
            <div className="flex h-7/8">
              <div className="w-1/8 flex justify-center items-start mt-3">
                {displayTricksBack(playerTrickCards)}
              </div>
              <div className="w-3/4 h-full flex flex-col justify-center items-center">
                <div className="h-5/6 flex justify-center items-center">
                  {displayOwnPlayableCards()}
                </div>
                <div className="h-1/6 flex justify-center items-center font-bold text-xl w-full text-green-100">
                  <p>{t("game.balance", { balance: playerBalance })}</p>
                </div>
              </div>

              <div className="w-1/8 h-2/3"></div>
            </div>
          )}
        {(gameState === "TRICK_PHASE" || gameState === "FINISHED") &&
          turnPlayer !== user?.username && (
            <div className="flex h-7/8">
              <div className="w-1/8 flex justify-center items-start mt-3">
                {displayTricksBack(playerTrickCards)}
              </div>
              <div className="w-3/4 h-full flex flex-col justify-center items-center">
                <div className="h-5/6 flex justify-center items-center">
                  {displayOwnCards()}
                </div>
                <div className="h-1/6 flex justify-center items-center font-bold text-xl w-full text-green-100">
                  <p>{t("game.balance", { balance: playerBalance })}</p>
                </div>
              </div>
              <div className="w-1/8 h-2/3"></div>
            </div>
          )}
      </div>
    );
  }

  function renderBidButtons() {
    return potentialBids.map((bid) => (
      <button
        key={bid}
        className="border-green-300 text-green-100 border-2 w-32 h-12 hover:scale-105 hover:bg-green-700 cursor-pointer rounded-md font-semibold mt-0.5"
        onClick={() => {
          send("/app/game.bid", {
            username: user?.username,
            gameId: game.gameId,
            newLevel: bid,
          });
          console.log("Bid sent:", user?.username, game.gameId, bid);
          setPotentialBids([]);
        }}
      >
        {bid}
      </button>
    ));
  }

  function renderTarokkNumberButton() {
    if (hasEightTarokks) {
      return (
        <button
          className={`border-green-300 border-2 w-32 h-10 hover:scale-105 hover:bg-green-700 cursor-pointer transition-transform duration-200 ml-5 mr-5 font-semibold rounded-md
            ${selectedTarokkNumber === 8 ? "bg-green-500" : ""}`}
          onClick={() => {
            setSelectedTarokkNumber(selectedTarokkNumber === 8 ? 0 : 8);
            send("/app/game.bonusInfo", {
              turnPlayer: turnPlayer,
              gameId: game.gameId,
              selectedTarokkNumber: tarokkNumberSent ? 0 : 8,
              calledTarokk: calledTarokk,
              bonuses: selectedBonuses,
            });
            setTarokkNumberSent((prev) => !prev);
            console.log("Tarokk number selected: 8");
          }}
        >
          {t("game.eightTarokks")}
        </button>
      );
    } else if (hasNineTarokks) {
      return (
        <button
          className={`border-green-300 border-2 w-32 h-10 hover:scale-105 hover:bg-green-700 cursor-pointer transition-transform duration-200 ml-5 mr-5 font-semibold rounded-md
            ${selectedTarokkNumber === 9 ? "bg-green-600" : ""}`}
          onClick={() => {
            setSelectedTarokkNumber(selectedTarokkNumber === 9 ? 0 : 9);
            send("/app/game.bonusInfo", {
              turnPlayer: turnPlayer,
              gameId: game.gameId,
              selectedTarokkNumber: tarokkNumberSent ? 0 : 9,
              calledTarokk: calledTarokk,
              bonuses: selectedBonuses,
            });
            setTarokkNumberSent((prev) => !prev);
            console.log("Tarokk number selected: 9");
          }}
        >
          {t("game.nineTarokks")}
        </button>
      );
    }
    return null;
  }

  function renderCallableTarokkButtons() {
    return callableTarokks.map((tarokk) => {
      const romanNumeral = tarokk.params?.romanNumeral as string;
      return (
        <button
          key={romanNumeral}
          className={`border-green-300 border-2 w-32 h-10 hover:scale-105 hover:bg-green-700 cursor-pointer transition-transform duration-200 ml-5 mr-5 font-semibold rounded-md
            ${calledTarokk === romanNumeral ? "bg-green-600" : ""}`}
          onClick={() => {
            setCalledTarokk(romanNumeral);
            send("/app/game.bonusInfo", {
              turnPlayer: turnPlayer,
              gameId: game.gameId,
              selectedTarokkNumber: selectedTarokkNumber,
              calledTarokk: romanNumeral,
              bonuses: selectedBonuses,
            });
            console.log("Tarokk called:", romanNumeral);
          }}
        >
          {translateMessage(tarokk)}
        </button>
      );
    });
  }

  function renderBonusButtons() {
    const buttons = potentialBonuses.map((bonus) => (
      <button
        key={bonus.bonusName}
        className={`border-green-300 border-2 w-40 h-14 hover:scale-105 hover:bg-green-700 cursor-pointer rounded-md font-semibold mt-0.5 ${
          selectedBonuses.includes(bonus.bonusName) ? "bg-green-600" : ""
        }`}
        onClick={() => {
          let selectedBonusesToSend = [];
          if (selectedBonuses.includes(bonus.bonusName)) {
            selectedBonusesToSend = selectedBonuses.filter(
              (b) => b !== bonus.bonusName,
            );
          } else {
            selectedBonusesToSend = [...selectedBonuses, bonus.bonusName];
          }
          send("/app/game.bonusInfo", {
            turnPlayer: turnPlayer,
            gameId: game.gameId,
            selectedTarokkNumber: selectedTarokkNumber,
            calledTarokk: calledTarokk,
            bonuses: selectedBonusesToSend,
          });
          setSelectedBonuses(selectedBonusesToSend);
        }}
      >
        {translateMessage(bonus.label)}
      </button>
    ));
    const confirmButton = (
      <button
        className="border-black border-2 w-40 h-14 hover:scale-105 hover:bg-green-400 cursor-pointer bg-green-300 text-[#2f4b3a] rounded-md font-semibold mt-0.5"
        onClick={() => {
          if (
            declarer === user?.username &&
            firstBonusRound &&
            calledTarokk &&
            selectedBonuses.length > 0
          ) {
            send("/app/game.announceFirstBonuses", {
              declarer: user?.username,
              gameId: game.gameId,
              selectedTarokkNumber: selectedTarokkNumber,
              calledTarokk: calledTarokk,
              bonuses: selectedBonuses,
            });
            console.log(
              "Selected bonuses sent:",
              selectedBonuses,
              calledTarokk,
              selectedTarokkNumber,
            );
            setPrivateInformation([]);
          } else if (
            turnPlayer === user?.username &&
            selectedBonuses.length > 0 &&
            !firstBonusRound
          ) {
            send("/app/game.announceBonuses", {
              username: user?.username,
              gameId: game.gameId,
              selectedTarokkNumber: selectedTarokkNumber,
              bonuses: selectedBonuses,
            });
            console.log(
              "Selected bonuses sent:",
              selectedBonuses,
              calledTarokk,
              selectedTarokkNumber,
            );
            setPrivateInformation([]);
          } else {
            if (
              firstBonusRound &&
              declarer === user?.username &&
              !calledTarokk &&
              selectedBonuses.length === 0
            ) {
              setPrivateInformation([t("game.noTarokkAndBonusSelected")]);
            } else if (
              firstBonusRound &&
              declarer === user?.username &&
              !calledTarokk
            ) {
              setPrivateInformation([t("game.noTarokkCalled")]);
            } else if (
              firstBonusRound &&
              declarer === user?.username &&
              selectedBonuses.length === 0
            ) {
              setPrivateInformation([t("game.noBonusSelected")]);
            } else {
              setPrivateInformation([t("game.noBonusSelected")]);
            }
          }
        }}
      >
        {t("game.submit")}
      </button>
    );
    return [...buttons, confirmButton];
  }

  function renderPlayerHand(place: "NORTH" | "EAST" | "WEST") {
    const { playerName, playerCardsNumber, playerBalance } =
      setPlayerProperties(
        place,
        user,
        player1,
        player2,
        player3,
        player4,
        playerData,
      );

    return (
      <div className="flex flex-col h-full">
        <div className="h-1/6 flex justify-center items-center font-bold text-xl">
          {playerName === turnPlayer && turnPlayer !== null ? (
            <p className="bg-green-300 text-[#2f4b3a] text-2xl rounded-md w-auto pt-0.5 pb-0.5 pl-3 pr-3">
              {playerName}
            </p>
          ) : (
            <p className="text-green-100">{playerName}</p>
          )}
        </div>

        <div className="flex justify-center items-center h-2/3 relative -top-5">
          {displayHandBack(playerCardsNumber)}
        </div>
        <div className="h-1/6 flex justify-center items-center font-bold text-xl">
          {playerName && (
            <p className="text-green-100">{t("game.balance", { balance: playerBalance })}</p>
          )}
        </div>
      </div>
    );
  }

  function renderWesternPlayerHandInTrickPhase() {
    const { playerName, playerCardsNumber, playerTrickCards, playerBalance } =
      setPlayerPropertiesInTrickPhase(
        player1,
        player2,
        player3,
        player4,
        player4,
        player1,
        player2,
        player3,
        user,
        playerData,
      );
    return (
      <div className="flex justify-around h-full">
        <div className="w-1/3 flex flex-col">
          <div className="h-1/6 flex justify-center items-center font-bold text-xl">
            {playerTrickCards > 0 && (
              <p className="text-green-100">{t("game.playerTricks", { name: playerName })}</p>
            )}
          </div>

          <div className="h-5/6 flex justify-center items-start">
            {displayTricksBack(playerTrickCards)}
          </div>
        </div>

        <div className="w-2/3 flex justify-center items-center">
          <div className="flex flex-col h-full">
            <div className="h-1/6 flex justify-center items-center font-bold text-xl">
              {playerName === turnPlayer ? (
                <p className="bg-green-300 text-[#2f4b3a] text-2xl rounded-md w-auto pt-0.5 pb-0.5 pl-3 pr-3">
                  {playerName}
                </p>
              ) : (
                <p className="text-green-100">{playerName}</p>
              )}
            </div>

            <div className="flex justify-center items-center h-2/3 relative -top-5">
              {displayHandBack(playerCardsNumber)}
            </div>
            <div className="h-1/6 flex justify-center items-center font-bold text-xl">
              <p className="text-green-100">{t("game.balance", { balance: playerBalance })}</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  function renderEasternPlayerHandInTrickPhase() {
    const { playerName, playerCardsNumber, playerTrickCards, playerBalance } =
      setPlayerPropertiesInTrickPhase(
        player1,
        player2,
        player3,
        player4,
        player2,
        player3,
        player4,
        player1,
        user,
        playerData,
      );

    return (
      <div className="flex justify-around h-full">
        <div className="w-2/3 flex justify-center items-center">
          <div className="flex flex-col h-full">
            <div className="h-1/6 flex justify-center items-center font-bold text-xl">
              {playerName === turnPlayer ? (
                <p className="bg-green-300 text-[#2f4b3a] text-2xl rounded-md w-auto pt-0.5 pb-0.5 pl-3 pr-3">
                  {playerName}
                </p>
              ) : (
                <p className="text-green-100">{playerName}</p>
              )}
            </div>

            <div className="flex justify-center items-center h-2/3 relative -top-5">
              {displayHandBack(playerCardsNumber)}
            </div>
            <div className="h-1/6 flex justify-center items-center font-bold text-xl">
              <p className="text-green-100">{t("game.balance", { balance: playerBalance })}</p>
            </div>
          </div>
        </div>
        <div className="w-1/3 flex flex-col">
          <div className="h-1/6 flex justify-center items-center font-bold text-xl">
            {playerTrickCards > 0 && (
              <p className="text-green-100">{t("game.playerTricks", { name: playerName })}</p>
            )}
          </div>
          <div className="h-5/6 flex justify-center items-start">
            {displayTricksBack(playerTrickCards)}
          </div>
        </div>
      </div>
    );
  }

  function renderNorthernPlayerHandInTrickPhase() {
    const { playerName, playerCardsNumber, playerTrickCards, playerBalance } =
      setPlayerPropertiesInTrickPhase(
        player1,
        player2,
        player3,
        player4,
        player3,
        player4,
        player1,
        player2,
        user,
        playerData,
      );
    return (
      <div className="flex justify-around h-full">
        <div className="w-1/4 flex flex-col">
          <div className="h-1/6 flex justify-center items-center font-bold text-xl">
            {playerTrickCards > 0 && (
              <p className="text-green-100">{t("game.playerTricks", { name: playerName })}</p>
            )}
          </div>
          <div className="h-5/6 flex justify-center items-start">
            {displayTricksBack(playerTrickCards)}
          </div>
        </div>
        <div className="w-1/2 flex justify-center items-center">
          <div className="flex flex-col h-full">
            <div className="h-1/6 flex justify-center items-center font-bold text-xl">
              {playerName === turnPlayer ? (
                <p className="bg-green-300 text-[#2f4b3a] text-2xl rounded-md w-auto pt-0.5 pb-0.5 pl-3 pr-3">
                  {playerName}
                </p>
              ) : (
                <p className="text-green-100">{playerName}</p>
              )}
            </div>

            <div className="flex justify-center items-center h-2/3 relative -top-5">
              {displayHandBack(playerCardsNumber)}
            </div>
            <div className="h-1/6 flex justify-center items-center font-bold text-xl">
              <p className="text-green-100">{t("game.balance", { balance: playerBalance })}</p>
            </div>
          </div>
        </div>
        <div className="w-1/4 h-full flex justify-center items-center"></div>
      </div>
    );
  }

  const handleLogout = () => {
    send("/app/game.logoutRequest", {
      username: user?.username,
      gameId: game.gameId,
    });
  };

  function handleDeal() {
    send("/app/game.deal", { username: user?.username, gameId: game.gameId });
    setDealButtonClicked(true);
    console.log("Deal action sent:", user?.username, game.gameId);
    //navigate("/game");
  }

  function displaySkart(length: number) {
    const cardsBack = Array(length).fill("Back.png");
    return cardsBack.map((imagePath, index) => (
      <img
        key={index}
        src={imagePath}
        alt={t("game.alt.skartCardBack", { index: index + 1 })}
        className="w-19 -mx-14"
      />
    ));
  }

  function displayTricksBack(length: number) {
    const cardsBack = Array(length).fill("Back.png");
    return cardsBack.map((imagePath, index) => (
      <img
        key={index}
        src={imagePath}
        alt={t("game.alt.trickCardBack", { index: index + 1 })}
        className="w-19 -mx-9.75"
      />
    ));
  }

  function displayHandBack(cardsNumber: number) {
    let rotation = INITIAL_ROTATION[cardsNumber];
    const rotatedCards = [];
    for (let i = 0; i < cardsNumber; i++) {
      rotatedCards.push(
        <img
          key={i}
          src="Back.png"
          alt={t("game.alt.cardBack", { index: i + 1 })}
          className="w-19 -m-9.5"
          style={{
            transform: `rotate(${rotation}deg)`,
            transformOrigin: "bottom center",
          }}
        />,
      );
      rotation += 15;
    }
    return rotatedCards;
  }

  function displayOwnCards() {
    return ownCards.map((card, index) => (
      <img
        key={index}
        src={card.imagePath}
        alt={t("game.alt.card", { index: index + 1 })}
        className="w-20 mx-1"
      />
    ));
  }

  function displayDeclarerSkartWithTarokk() {
    return declarerSkart.map((card, index) => (
      <img
        key={index}
        src={card.imagePath}
        alt={t("game.alt.card", { index: index + 1 })}
        className="w-19 mx-1"
      />
    ));
  }

  function displayOwnCardsWithTalon() {
    return ownCards.map((card, index) =>
      card.clickable ? (
        <img
          key={index}
          src={card.imagePath}
          alt={t("game.alt.card", { index: index + 1 })}
          className="w-20 mx-1 hover:-translate-y-1 hover:scale-105 cursor-pointer transition-transform duration-200"
          onClick={() => handleDiscardSkart(card)}
        />
      ) : (
        <img
          key={index}
          src={card.imagePath}
          alt={t("game.alt.card", { index: index + 1 })}
          className="w-20 mx-1"
        />
      ),
    );
  }

  function displayOwnPlayableCards() {
    return ownCards.map((card, index) =>
      card.clickable ? (
        <img
          key={index}
          src={card.imagePath}
          alt={t("game.alt.card", { index: index + 1 })}
          className="w-20 mx-1 hover:-translate-y-1 hover:scale-105 cursor-pointer transition-transform duration-200"
          onClick={() => handlePlay(card)}
        />
      ) : (
        <img
          key={index}
          src={card.imagePath}
          alt={t("game.alt.card", { index: index + 1 })}
          className="w-20 mx-1"
        />
      ),
    );
  }

  function handlePlay(card: Card) {
    if (trickCard === null) {
      setTrickCard(card);
      setOwnCards((prev) => prev.filter((c) => c.imagePath !== card.imagePath));
      send("/app/game.playCard", {
        username: user?.username,
        gameId: game.gameId,
        cardId: card.cardId,
      });
    }
  }

  function displayPublicCards(cards: CardImage[]) {
    return (
      <div className="flex flex-wrap justify-center items-center">
        {cards.map((card, index) => (
          <img
            key={index}
            src={card.frontImagePath}
            alt={t("game.alt.publicCard", { index: index + 1 })}
            className="w-16 mx-1"
          />
        ))}
      </div>
    );
  }

  function displayTemporarySelectedCards() {
    return temporarySelectedCards.map((card, index) => (
      <img
        key={index}
        src={card.imagePath}
        alt={t("game.alt.selectedCard", { index: index + 1 })}
        className="w-20 mx-1 hover:-translate-y-1 hover:scale-105 cursor-pointer transition-transform duration-200"
        onClick={() => handleDiscardSkart(card)}
      />
    ));
  }

  function handleDiscardSkart(card: Card) {
    if (temporarySelectedCards.includes(card)) {
      setTemporarySelectedCards((prev) =>
        prev.filter((c) => c.imagePath !== card.imagePath),
      );
      setOwnCards((prev) =>
        [...prev, card].sort((a, b) => a.cardId - b.cardId),
      );
    } else {
      if (temporarySelectedCards.length < cardsToDiscard.current) {
        setTemporarySelectedCards((prev) => [...prev, card]);
        setOwnCards((prev) =>
          prev.filter((c) => c.imagePath !== card.imagePath),
        );
      } else {
        setPublicInformation([
          t("game.skartLimit", { count: cardsToDiscard.current }),
        ]);
      }
    }
  }

  function renderTrickCards() {
    return (
      <div className="relative w-full h-full">
        {trickCards.map((card, index) => {
          const animClass = trickWinnerDirection
            ? `card-fly-${trickWinnerDirection}`
            : (cardAnimationsRef.current.get(card.cardId) ?? "");
          return (
            <img
              key={trickWinnerDirection ? `fly-${card.cardId}` : card.cardId}
              src={card.imagePath}
              alt={t("game.alt.trickCard", { index: index + 1 })}
              className={`w-20 absolute ${animClass}`}
              style={{
                left: `calc(50% + ${card?.x ?? 0}%)`,
                top: `calc(50% + ${card?.y ?? 0}%)`,
                transform: `translate(-50%, -50%) rotate(${card?.rotation ?? 0}deg)`,
                zIndex: index,
              }}
            />
          );
        })}
      </div>
    );
  }

  function handleDiscardHand() {
    send("/app/game.discardHand", {
      username: user?.username,
      gameId: game.gameId,
    });
    console.log("Discard hand action sent:", user?.username, game.gameId);
    setDiscardInformation(null);
  }

  function formatDiscardInformation() {
    if (!discardInformation) return null;
    return translateInfoLines(discardInformation).map((sentence, index) => (
      <span key={index}>
        {sentence}
        <br />
      </span>
    ));
  }

  function formatLogoutWarning() {
    if (!logoutWarning) return null;
    return translateInfoLines(logoutWarning).map((sentence, index) => (
      <span key={index}>
        {sentence}
        <br />
      </span>
    ));
  }

  function confirmLogout() {
    send("/app/game.confirmLogout", {
      username: user?.username,
      gameId: game.gameId,
    });
    setLogoutWarning(null);
    setToken(null);
    setUser(null);
    navigate("/");
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-table-background px-4 py-8">
      <div className="p-4 sm:p-6 bg-[#4B2E1F] rounded-[90px] shadow-inner w-full flex gap-4 justify-around">
        {/*Card table */}
        <div className="w-3/4 h-207.5 bg-poker-table rounded-[70px] shadow-2xl text-white px-6 sm:px-8 flex flex-col items-center justify-center">
          <MenuButtons
            onHandleLogout={handleLogout}
            onHandleDeal={handleDeal}
            dealButtonClicked={dealButtonClicked}
            gameState={gameState}
            user={user}
            dealer={dealer}
          />

          {/* Game area */}
          <div className="w-full h-11/12 flex flex-col">
            {/* Top row */}
            <div className="w-full flex flex-auto">
              <DeclarerSkart
                declarerSkart={declarerSkart}
                declarerSkartLength={declarerSkartLength}
                gameState={gameState}
                publicDeclarerSkart={publicDeclarerSkart}
                onDisplayDeclarerSkartWithTarokk={
                  displayDeclarerSkartWithTarokk
                }
                onDisplaySkart={displaySkart}
                onDisplayPublicCards={displayPublicCards}
              />

              <NorthernPlayerHand
                gameState={gameState}
                onRenderPlayerHand={() => renderPlayerHand("NORTH")}
                onRenderNorthernPlayerHandInTrickPhase={
                  renderNorthernPlayerHandInTrickPhase
                }
              />

              <OpponentSkart
                opponentSkartLength={opponentSkartLength}
                gameState={gameState}
                publicOpponentSkart={publicOpponentSkart}
                onDisplaySkart={displaySkart}
                onDisplayPublicCards={displayPublicCards}
              />
            </div>
            {/* Middle row */}
            {gameState === "FINISHED" && !logoutWarning && (
              <DeclarerPublicTricks
                publicDeclarerTricks={publicDeclarerTricks}
                onDisplayPublicCards={displayPublicCards}
              />
            )}
            {gameState === "FINISHED" && logoutWarning && (
              <div className="w-full h-1/4 flex">
                <div className="w-1/4"></div>
                <div className="w-1/2">
                  <LogoutWarning
                    onFormatLogoutWarning={formatLogoutWarning}
                    onSetLogoutWarning={setLogoutWarning}
                    onConfirmLeaving={confirmLogout}
                  />
                </div>

                <div className="w-1/4"></div>
              </div>
            )}
            {gameState !== "FINISHED" && (
              <div className="w-full h-1/3 flex">
                <WesternPlayerHand
                  gameState={gameState}
                  onRenderPlayerHand={() => renderPlayerHand("WEST")}
                  onRenderWesternPlayerHandInTrickPhase={
                    renderWesternPlayerHandInTrickPhase
                  }
                />

                {/* Talon and play area */}
                {logoutWarning ? (
                  <LogoutWarning
                    onFormatLogoutWarning={formatLogoutWarning}
                    onSetLogoutWarning={setLogoutWarning}
                    onConfirmLeaving={confirmLogout}
                  />
                ) : gameState !== "TRICK_PHASE" ? (
                  <div className="w-1/2">
                    <OpeningImage isGameNew={isGameNew} />

                    <Talon talonCardsNumber={talonCardsNumber} />

                    <Skart
                      temporarySelectedCards={temporarySelectedCards}
                      onDisplayTemporarySelectedCards={() => (
                        <>{displayTemporarySelectedCards()}</>
                      )}
                      gameState={gameState}
                      discardInformation={discardInformation}
                      ownCards={ownCards}
                      cardsToDiscard={cardsToDiscard}
                      sendSkartCards={sendSkartCards}
                      turnPlayer={turnPlayer}
                      user={user}
                    />

                    <DisposableHandInfo
                      gameState={gameState}
                      discardInformation={discardInformation}
                      onFormatDiscardInformation={formatDiscardInformation}
                      onHandleDiscardHand={handleDiscardHand}
                      onSetDiscardInformation={setDiscardInformation}
                    />

                    <PublicDescardedHand
                      publicHand={publicDiscardedHand}
                      gameState={gameState}
                    />

                    <Bonuses
                      gameState={gameState}
                      discardInformation={discardInformation}
                      turnPlayer={turnPlayer}
                      user={user}
                      hasEightTarokks={hasEightTarokks}
                      hasNineTarokks={hasNineTarokks}
                      declarer={declarer}
                      callableTarokks={callableTarokks}
                      onRenderTarokkNumberButton={() =>
                        renderTarokkNumberButton()
                      }
                      onRenderCallableTarokkButtons={() =>
                        renderCallableTarokkButtons()
                      }
                      onRenderBonusButtons={() => renderBonusButtons()}
                    />
                  </div>
                ) : (
                  <TrickPlace onRenderTrickCards={() => renderTrickCards()} />
                )}

                {/* Player 2's area */}
                <EasternPlayerHand
                  gameState={gameState}
                  onRenderPlayerHand={() => renderPlayerHand("EAST")}
                  onRenderEasternPlayerHandInTrickPhase={
                    renderEasternPlayerHandInTrickPhase
                  }
                />
              </div>
            )}

            {/* Bottom row */}
            <OwnHand
              gameState={gameState}
              renderOwnHand={renderOwnHand}
              displayPublicCards={displayPublicCards}
              publicOpponentTricks={publicOpponentTricks}
            />
          </div>
        </div>

        <InfoTable
          bid={bid}
          declarer={declarer}
          dealer={dealer}
          turnPlayer={turnPlayer}
          publicInfo={publicInformation}
          privateInfo={privateInformation}
          gameState={gameState}
          declarerBonuses={declarerBonuses}
          opponentBonuses={opponentBonuses}
          startPlayer={startPlayer}
        />
      </div>
    </div>
  );
}

export default Game;
