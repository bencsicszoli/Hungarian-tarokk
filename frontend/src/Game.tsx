import { useState, useEffect, useRef, useCallback } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useUser } from "./context/UserContext";
import { useWebSocket } from "./context/WebSocketContext";
import InfoTable from "./pageComponents/InfoTable";
import Talon from "./pageComponents/Talon";
import Skart from "./Skart";
import type { Card, TrickCard, PlayerData, CardImage } from "./Types";
import Bonuses from "./Bonuses";
import PublicHand from "./PublicHand";
import {
  setPlayerProperties,
  setPlayerPropertiesInTrickPhase,
  setOwnProperties,
} from "./Utils.ts";

const INITIAL_ROTATION = [
  0, 0, -7.5, -15, -22.5, -30, -37.5, -45, -52.5, -60, -67.5, -75, -82.5,
];

function Game() {
  const location = useLocation();
  const navigate = useNavigate();
  const userContext = useUser();
  const webSocketContext = useWebSocket();
  const { game } = location.state || {};
  const { user, token, setToken, setUser } = userContext || {
    user: null,
    token: null,
    setToken: () => {},
    setUser: () => {},
  };
  const { subscribe, send, connected } = webSocketContext || {
    subscribe: () => null,
    send: () => {},
    connected: false,
  };

  const [player1, setPlayer1] = useState<string | null>(game?.player1 || null);
  const [player2, setPlayer2] = useState<string | null>(game?.player2 || null);
  const [player3, setPlayer3] = useState<string | null>(game?.player3 || null);
  const [player4, setPlayer4] = useState<string | null>(game?.player4 || null);
  const [dealer, setDealer] = useState<string | null>(game?.dealer || null);
  const [startPlayer, setStartPlayer] = useState<string | null>(
    game?.startPlayer || null,
  );
  const [bidPlayer, setBidPlayer] = useState<string | null>(null);
  const [turnPlayer, setTurnPlayer] = useState<string | null>(
    game?.turnPlayer || null,
  );
  const [declarer, setDeclarer] = useState<string | null>(
    game?.declarer || null,
  );
  const [bid, setBid] = useState<"-" | "3" | "2" | "1" | "solo">("-");

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
  const [ownCards, setOwnCards] = useState<Card[]>([]);
  const [publicHand, setPublicHand] = useState<Card[]>([]);
  const [temporarySelectedCards, setTemporarySelectedCards] = useState<Card[]>(
    [],
  );
  const [declarerSkart, setDeclarerSkart] = useState<Card[]>([]);
  const [gameState, setGameState] = useState<
    | "NEW"
    | "IN_PROGRESS"
    | "BIDDING"
    | "TALON_PICK_UP"
    | "SKART_LAY_DOWN"
    | "BONUS_ANNOUNCEMENT"
    | "TRICK_PHASE"
    | "FINISHED"
  >(game?.gameState || "NEW");
  const [talonCardsNumber, setTalonCardsNumber] = useState<number>(0);
  const [levelDiscription, setLevelDescription] = useState<string>(
    game?.levelDescription || "None",
  );
  const [potentialBids, setPotentialBids] = useState<string[]>([]);
  const [declarerSkartLength, setDeclarerSkartLength] = useState<number>(0);
  const [publicDeclarerSkart, setPublicDeclarerSkart] = useState<CardImage[]>(
    [],
  );
  const [publicDeclarerTricks, setPublicDeclarerTricks] = useState<CardImage[]>(
    [],
  );
  const [opponentSkartLength, setOpponentSkartLength] = useState<number>(0);
  const [publicOpponentSkart, setPublicOpponentSkart] = useState<CardImage[]>(
    [],
  );
  const [publicOpponentTricks, setPublicOpponentTricks] = useState<CardImage[]>(
    [],
  );
  const [publicInformation, setPublicInformation] = useState<string>(
    game?.information || "",
  );
  const [privateInformation, setPrivateInformation] = useState<string>(
    game?.privateInformation || "",
  );
  const [discardInformation, setDiscardInformation] = useState<string | null>(
    null,
  );
  const [potentialBonuses, setPotentialBonuses] = useState<string[]>([]);
  const [callableTarokks, setCallableTarokks] = useState<string[]>([]);
  const [calledTarokk, setCalledTarokk] = useState<string | null>(null);
  const [selectedTarokkNumber, setSelectedTarokkNumber] = useState<number>(0);
  const [hasEightTarokks, setHasEightTarokks] = useState<boolean>(false);
  const [hasNineTarokks, setHasNineTarokks] = useState<boolean>(false);
  const [selectedBonuses, setSelectedBonuses] = useState<string[]>([]);
  const [firstBonusRound, setFirstBonusRound] = useState<boolean>(true);
  const [declarerBonuses, setDeclarerBonuses] = useState<string | null>(null);
  const [opponentBonuses, setOpponentBonuses] = useState<string | null>(null);
  const [trickCard, setTrickCard] = useState<Card | null>(null);
  const [trickCards, setTrickCards] = useState<TrickCard[]>([]);
  const [dealButtonClicked, setDealButtonClicked] = useState<boolean>(false);
  const [tarokkNumberSent, setTarokkNumberSent] = useState<boolean>(false);
  const [isGameNew, setIsGameNew] = useState<boolean>(true);
  const cardsToDiscard = useRef<number>(0);

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
          setPublicInformation(message.information);
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
          setBidPlayer(null);
          setDeclarer(null);
          setBid("-");
          setDeclarerSkartLength(0);
          setOpponentSkartLength(0);
          setDiscardInformation(null);
          setPublicHand([]);
          setDeclarerBonuses(null);
          setOpponentBonuses(null);
          setPublicInformation(message.dealerInfo);
          setIsGameNew(false);
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
          setPublicInformation("Bidding phase has started!");
          setPrivateInformation("");
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
          setPublicInformation(message.info);
          setBidPlayer(message.bidPlayer);
          console.log("Public bid info updated:", message);
          break;
        case "game.gameState":
          setGameState(message.gameState);
          console.log("Game state updated:", message.gameState);
          break;
        case "game.gameStateInfo":
          setGameState(message.gameState);
          setPublicInformation(message.info);
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
          setPublicInformation(message.discardedCardsInfo);
          console.log("Public skart info updated:", message);
          break;
        case "game.publicInfo":
          setPublicInformation(message.info);
          console.log("Public information updated:", message.info);
          break;
        case "game.discardHand":
          setPublicInformation(message.info);
          setBidPlayer(null);
          setDeclarer(null);
          setBid("-");
          setPublicHand(message.cards);
          setPublicInformation(
            message.playerName.toUpperCase() + " " + message.info,
          );
          setDeclarerSkartLength(0);
          setOpponentSkartLength(0);
          setGameState("NEW");
          setPlayerData((prev) => ({
            ...prev,
            ...(message.playerName
              ? {
                  [message.playerName]: {
                    ...prev[message.playerName],
                    playerCardsNumber: 0,
                  },
                }
              : {}),
          }));
          setDealButtonClicked(false);
          setOwnCards([]);
          setPrivateInformation("");
          console.log("Public discard hand info updated:", message.info);
          break;
        case "game.tarokkInSkart":
          setDeclarerSkart(message.cards);
          //setPublicInformation(message.info);
          //setPublicInformation((prev) => `${prev}@ ${message.info}`);
          console.log("Declarer's skart with tarokk updated:", message.cards);
          break;
        case "game.publicSkartPhaseFinishInfo":
          setPublicInformation((prev) => `${prev}@ ${message.info}`);
          console.log("Public skart phase finish info updated:", message.info);
          break;
        case "game.turnPlayer":
          setTurnPlayer(message.turnPlayer);
          console.log("Turn player updated:", message.turnPlayer);
          break;
        case "game.publicBonusInfo":
          setPublicInformation(message.info);
          setDeclarerBonuses(message.declarerBonuses.join(", "));
          setOpponentBonuses(message.opponentBonuses.join(", "));
          setTurnPlayer(message.turnPlayer);
          setFirstBonusRound(false);
          console.log("Public bonus info updated:", message.info);
          break;
        case "game.trickCards":
          setTrickCards(message.cards);
          setPlayerData((prev) => ({
            ...prev,
            ...(message.turnName
              ? {
                  [message.turnName]: {
                    ...prev[message.turnName],
                    playerCardsNumber: message.cardsInHand || 0,
                  },
                }
              : {}),
          }));
          console.log("Trick cards updated:", message);
          break;
        case "game.newTrickRound":
          setTrickCards([]);
          setTrickCard(null);
          setPlayerData((prev) => ({
            ...prev,
            ...(player1
              ? {
                  [player1]: {
                    ...prev[player1],
                    playerTrickCards: message.playerTricks[0],
                  },
                }
              : {}),
            ...(player2
              ? {
                  [player2]: {
                    ...prev[player2],
                    playerTrickCards: message.playerTricks[1],
                  },
                }
              : {}),
            ...(player3
              ? {
                  [player3]: {
                    ...prev[player3],
                    playerTrickCards: message.playerTricks[2],
                  },
                }
              : {}),
            ...(player4
              ? {
                  [player4]: {
                    ...prev[player4],
                    playerTrickCards: message.playerTricks[3],
                  },
                }
              : {}),
          }));

          console.log("Trick reset message:", message);
          break;
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
          setPublicInformation(
            `New dealer is ${message.newDealer.toUpperCase()}`,
          );
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
    [player1, player2, player3, player4],
  );

  useEffect(() => {
    if (!user || !connected) navigate(`/`);
  }, [user, connected, navigate]);

  const handlePrivateMessage = useCallback((payload: unknown) => {
    const message = JSON.parse(
      (payload as Record<string, unknown>).body as string,
    );
    console.log("Private message received:", message);
    // Handle hand updates here
    switch (message.type) {
      case "game.playerCards":
        setOwnCards(message.cards);
        console.log("Own cards updated:", message.cards);
        break;
      case "game.playerCardsWithTalon": // Function to add onClick methods is missing
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
      setPrivateInformation(
        `Select ${cardsToDiscard.current} cards to discard!`,
      );
    } else if (gameState === "SKART_LAY_DOWN") {
      setPrivateInformation("");
    } else if (
      gameState === "BONUS_ANNOUNCEMENT" &&
      privateInformation.includes("cards to discard")
    ) {
      setPrivateInformation("");
    }
  }, [gameState, turnPlayer, user]);

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
            {playerTrickCards > 0 && <p>{playerName}'s tricks</p>}
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
              <p>Balance: {playerBalance}</p>
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
              <p>Balance: {playerBalance}</p>
            </div>
          </div>
        )}
        {gameState === "SKART_LAY_DOWN" && turnPlayer !== user?.username && (
          <div className="w-full h-full flex flex-col justify-center items-center">
            <div className="h-5/6 flex justify-center items-center">
              {displayOwnCards()}
            </div>
            <div className="h-1/6 flex justify-center items-center font-bold text-xl w-full text-green-100">
              <p>Balance: {playerBalance}</p>
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
                  <p>Balance: {playerBalance}</p>
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
                  <p>Balance: {playerBalance}</p>
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
          8 Tarokks
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
          9 Tarokks
        </button>
      );
    }
    return null;
  }

  function renderCallableTarokkButtons() {
    return callableTarokks.map((tarokk) => (
      <button
        key={tarokk}
        className={`border-green-300 border-2 w-32 h-10 hover:scale-105 hover:bg-green-700 cursor-pointer transition-transform duration-200 ml-5 mr-5 font-semibold rounded-md
          ${calledTarokk === tarokk ? "bg-green-600" : ""}`}
        onClick={() => {
          setCalledTarokk(tarokk);
          send("/app/game.bonusInfo", {
            turnPlayer: turnPlayer,
            gameId: game.gameId,
            selectedTarokkNumber: selectedTarokkNumber,
            calledTarokk: tarokk,
            bonuses: selectedBonuses,
          });
          console.log("Tarokk called:", tarokk);
        }}
      >
        {tarokk}
      </button>
    ));
  }

  function renderBonusButtons() {
    const buttons = potentialBonuses.map((bonus) => (
      <button
        key={bonus}
        className={`border-green-300 border-2 w-40 h-14 hover:scale-105 hover:bg-green-700 cursor-pointer rounded-md font-semibold mt-0.5 ${
          selectedBonuses.includes(bonus) ? "bg-green-600" : ""
        }`}
        onClick={() => {
          let selectedBonusesToSend = [];
          if (selectedBonuses.includes(bonus)) {
            selectedBonusesToSend = selectedBonuses.filter((b) => b !== bonus);
          } else {
            selectedBonusesToSend = [...selectedBonuses, bonus];
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
        {bonus}
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
            setPrivateInformation("");
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
            setPrivateInformation("");
          } else {
            if (
              firstBonusRound &&
              declarer === user?.username &&
              !calledTarokk &&
              selectedBonuses.length === 0
            ) {
              setPrivateInformation(
                "You haven't selected a tarokk and a bonus!",
              );
            } else if (
              firstBonusRound &&
              declarer === user?.username &&
              !calledTarokk
            ) {
              setPrivateInformation("You haven't called a tarokk!");
            } else if (
              firstBonusRound &&
              declarer === user?.username &&
              selectedBonuses.length === 0
            ) {
              setPrivateInformation("You haven't selected a bonus!");
            } else {
              setPrivateInformation("You haven't selected a bonus!");
            }
          }
        }}
      >
        Submit
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
            <p className="text-green-100">Balance: {playerBalance}</p>
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
              <p className="text-green-100">{playerName}'s tricks</p>
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
              <p className="text-green-100">Balance: {playerBalance}</p>
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
              <p className="text-green-100">Balance: {playerBalance}</p>
            </div>
          </div>
        </div>
        <div className="w-1/3 flex flex-col">
          <div className="h-1/6 flex justify-center items-center font-bold text-xl">
            {playerTrickCards > 0 && (
              <p className="text-green-100">{playerName}'s tricks</p>
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
              <p className="text-green-100">{playerName}'s tricks</p>
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
              <p className="text-green-100">Balance: {playerBalance}</p>
            </div>
          </div>
        </div>
        <div className="w-1/4 h-full flex justify-center items-center"></div>
      </div>
    );
  }

  const handleLogout = () => {
    setToken(null);
    setUser(null);
    navigate("/");
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
        alt={`Skart card back ${index + 1}`}
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
        alt={`Trick card back ${index + 1}`}
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
          alt={`Card back ${i + 1}`}
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
        alt={`Card ${index + 1}`}
        className="w-20 mx-1"
      />
    ));
  }

  function displayDeclarerSkartWithTarokk() {
    return declarerSkart.map((card, index) => (
      <img
        key={index}
        src={card.imagePath}
        alt={`Card ${index + 1}`}
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
          alt={`Card ${index + 1}`}
          className="w-20 mx-1 hover:-translate-y-1 hover:scale-105 cursor-pointer transition-transform duration-200"
          onClick={() => handleDiscardSkart(card)}
        />
      ) : (
        <img
          key={index}
          src={card.imagePath}
          alt={`Card ${index + 1}`}
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
          alt={`Card ${index + 1}`}
          className="w-20 mx-1 hover:-translate-y-1 hover:scale-105 cursor-pointer transition-transform duration-200"
          onClick={() => handlePlay(card)}
        />
      ) : (
        <img
          key={index}
          src={card.imagePath}
          alt={`Card ${index + 1}`}
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
            alt={`Public card ${index + 1}`}
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
        alt={`Selected card ${index + 1}`}
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
        if (cardsToDiscard.current === 1) {
          setPublicInformation("You can only select 1 card for the skart.");
        } else {
          setPublicInformation(
            `You can only select ${cardsToDiscard.current} cards for the skart.`,
          );
        }
      }
    }
  }

  function renderTrickCards() {
    return (
      <div className="relative w-full h-full">
        {trickCards.map((card, index) => {
          return (
            <img
              key={index}
              src={card.imagePath}
              alt={`Trick card ${index + 1}`}
              className="w-20 absolute"
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
    return discardInformation
      .split("@")
      .filter(Boolean)
      .map((sentence, index) => (
        <span key={index}>
          {sentence.trim()}
          <br />
        </span>
      ));
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-table-background px-4 py-8">
      <div className="p-4 sm:p-6 bg-[#4B2E1F] rounded-[90px] shadow-inner w-full flex gap-4 justify-around">
        {/*Card table */}
        <div className="w-3/4 h-207.5 bg-poker-table rounded-[70px] shadow-2xl text-white px-6 sm:px-8 flex flex-col items-center justify-center">
          {/* Menu buttons */}
          <div className="w-full h-1/12 flex justify-around items-center">
            <button
              className="border-black border-2 w-1/8 h-2/3 bg-green-300 hover:scale-105 hover:bg-green-400 cursor-pointer text-[#2f4b3a] text-2xl font-bold rounded-lg"
              onClick={handleLogout}
            >
              Logout
            </button>
            <button className="border-black border-2 w-1/8 h-2/3 bg-green-300 hover:scale-105 hover:bg-green-400 cursor-pointer text-[#2f4b3a] text-2xl font-bold rounded-lg">
              Back
            </button>
            {user?.username === dealer &&
              (gameState === "NEW" || gameState === "FINISHED") &&
              !dealButtonClicked && (
                <button
                  className="border-black border-2 w-1/8 h-2/3 bg-green-300 hover:scale-105 hover:bg-green-400 cursor-pointer text-[#2f4b3a] text-2xl font-bold rounded-lg"
                  onClick={handleDeal}
                >
                  DEAL
                </button>
              )}
          </div>

          {/* Game area */}
          <div className="w-full h-11/12 flex flex-col">
            {/* Top row */}
            <div className="w-full flex flex-auto">
              {/* Declarer's skart area */}
              <div className="w-1/4 grow">
                <div className="h-1/6 flex justify-center items-end font-bold text-xl text-green-100">
                  {declarerSkartLength > 0 && <p>Declarer's skart</p>}
                </div>
                <div className="flex justify-center items-start h-5/6 mt-2">
                  {declarerSkart.length > 0 &&
                    gameState !== "FINISHED" &&
                    displayDeclarerSkartWithTarokk()}
                  {declarerSkart.length === 0 &&
                    gameState !== "FINISHED" &&
                    displaySkart(declarerSkartLength)}
                  {gameState === "FINISHED" &&
                    displayPublicCards(publicDeclarerSkart)}
                </div>
              </div>

              {/* Player 3's area */}
              {gameState !== "TRICK_PHASE" && gameState !== "FINISHED" && (
                <div className="w-1/2">{renderPlayerHand("NORTH")}</div>
              )}
              {gameState === "TRICK_PHASE" && (
                <div className="w-1/2">
                  {renderNorthernPlayerHandInTrickPhase()}
                </div>
              )}
              {gameState === "FINISHED" && <div className="w-1/4"></div>}

              {/* Opponent's skart area */}
              <div className="w-1/4 grow">
                <div className="h-1/6 flex justify-center items-center font-bold text-xl text-green-100">
                  {opponentSkartLength > 0 && <p>Opponent's skart</p>}
                </div>
                <div className="flex justify-center items-start h-5/6 mt-2">
                  {gameState !== "FINISHED"
                    ? displaySkart(opponentSkartLength)
                    : displayPublicCards(publicOpponentSkart)}
                </div>
              </div>
            </div>

            {/* Middle row */}
            {gameState === "FINISHED" ? (
              <div className="w-full flex flex-col flex-auto justify-center items-center">
                <p className="text-center text-xl font-bold text-green-100 mb-4">
                  Tricks of declarer side:
                </p>
                {displayPublicCards(publicDeclarerTricks)}
              </div>
            ) : (
              <div className="w-full h-1/3 flex">
                {/* Player 4's area */}
                {gameState !== "TRICK_PHASE" ? (
                  <div className="w-1/4">{renderPlayerHand("WEST")}</div>
                ) : (
                  <div className="w-[35%]">
                    {renderWesternPlayerHandInTrickPhase()}
                  </div>
                )}

                {/* Talon and play area */}
                {gameState !== "TRICK_PHASE" ? (
                  <div className="w-1/2">
                    {isGameNew && (
                      <div className="w-full h-full flex justify-center items-center">
                        <img src="deck.png" alt="Deck" className="h-full" />
                      </div>
                    )}
                    {talonCardsNumber > 0 && (
                      <Talon talonCardsNumber={talonCardsNumber} />
                    )}
                    {gameState === "SKART_LAY_DOWN" &&
                      discardInformation === null &&
                      (ownCards.length > 9 ||
                        temporarySelectedCards.length > 0) && (
                        <Skart
                          temporarySelectedCards={temporarySelectedCards}
                          onDisplayTemporarySelectedCards={() => (
                            <>{displayTemporarySelectedCards()}</>
                          )}
                          cardsToDiscard={cardsToDiscard}
                          sendSkartCards={sendSkartCards}
                          turnPlayer={turnPlayer}
                          user={user}
                        />
                      )}
                    {(gameState === "SKART_LAY_DOWN" ||
                      gameState === "BONUS_ANNOUNCEMENT") &&
                      discardInformation !== null && (
                        <div className="w-full h-[95%] border-black border-2 bg-green-300 text-[#2f4b3a] rounded-xl">
                          <div className="w-full h-1/2">
                            <p className="text-center text-3xl font-bold pt-4 px-2">
                              {formatDiscardInformation()}
                            </p>
                          </div>
                          <div className="w-full h-1/2 flex items-center justify-around">
                            <button
                              className="w-40 h-15 text-2xl bg-[#2f4b3a] hover:bg-green-700 cursor-pointer text-green-300 font-bold py-2 px-4 rounded-lg"
                              onClick={handleDiscardHand}
                            >
                              Yes
                            </button>
                            <button
                              className="w-40 h-15 text-2xl bg-red-600 hover:bg-red-700 cursor-pointer text-red-100 font-bold py-2 px-4 rounded-lg"
                              onClick={() => setDiscardInformation(null)}
                            >
                              No
                            </button>
                          </div>
                        </div>
                      )}
                    {gameState === "NEW" && publicHand.length > 0 && (
                      <PublicHand publicHand={publicHand} />
                    )}
                    {gameState === "BONUS_ANNOUNCEMENT" &&
                      discardInformation === null &&
                      turnPlayer === user?.username && (
                        <Bonuses
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
                      )}
                  </div>
                ) : (
                  <div className="w-[30%]">
                    <div className="h-full flex flex-col justify-center items-center">
                      <div className="relative w-full h-full">
                        {renderTrickCards()}
                      </div>
                    </div>
                  </div>
                )}

                {/* Player 2's area */}
                {gameState !== "TRICK_PHASE" ? (
                  <div className="w-1/4">{renderPlayerHand("EAST")}</div>
                ) : (
                  <div className="w-[35%]">
                    {renderEasternPlayerHandInTrickPhase()}
                  </div>
                )}
              </div>
            )}

            {/* Bottom row */}
            {gameState !== "FINISHED" ? (
              <div className="w-full h-1/3">{renderOwnHand()}</div>
            ) : (
              <div className="w-full flex flex-col flex-auto justify-center items-center">
                <p className="text-center text-xl font-bold text-green-100 mb-4">
                  Tricks of opponent side:
                </p>
                {displayPublicCards(publicOpponentTricks)}
              </div>
            )}
          </div>
        </div>

        {/*Info table */}
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
