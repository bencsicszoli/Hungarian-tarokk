import { useState, useEffect, useRef, useCallback } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useUser } from "./context/UserContext";
import { useWebSocket } from "./context/WebSocketContext";
import InfoTable from "./pageComponents/InfoTable";
import Talon from "./pageComponents/Talon";
import Skart from "./Skart";
import type { SeatAttributes, Card } from "./Types";

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
  const [player1Balance, setPlayer1Balance] = useState<number>(
    game?.player1Balance || 0,
  );
  const [player2Balance, setPlayer2Balance] = useState<number>(
    game?.player2Balance || 0,
  );
  const [player3Balance, setPlayer3Balance] = useState<number>(
    game?.player3Balance || 0,
  );
  const [player4Balance, setPlayer4Balance] = useState<number>(
    game?.player4Balance || 0,
  );
  const initialCards = {
    [game?.player1 || ""]: game?.player1CardsNumber || 0,
    [game?.player2 || ""]: game?.player2CardsNumber || 0,
    [game?.player3 || ""]: game?.player3CardsNumber || 0,
    [game?.player4 || ""]: game?.player4CardsNumber || 0,
  };
  const [playerCards, setPlayerCards] =
    useState<Record<string, number>>(initialCards);
  const [ownCards, setOwnCards] = useState<Card[]>([]);
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
  >(game?.gameState || "NEW");
  const [tableTurned, setTableTurned] = useState(false);
  const [talonCardsNumber, setTalonCardsNumber] = useState<number>(0);
  const [levelDiscription, setLevelDescription] = useState<string>(
    game?.levelDescription || "None",
  );
  const [potentialBids, setPotentialBids] = useState<string[]>([]);
  const [declarerSkartLength, setDeclarerSkartLength] = useState<number>(0);
  const [opponentSkartLength, setOpponentSkartLength] = useState<number>(0);
  const [publicInformation, setPublicInformation] = useState<string>("");
  const [privateInformation, setPrivateInformation] = useState<string>("");
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
  const [trickCards, setTrickCards] = useState<Card[]>([]);
  const seatModifier = useRef(0);
  const cardsToDiscard = useRef<number>(0);

  const handlePublicMessage = useCallback((payload: unknown) => {
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
        setPlayer1Balance(message.player1Balance);
        setPlayer2Balance(message.player2Balance);
        setPlayer3Balance(message.player3Balance);
        setPlayer4Balance(message.player4Balance);
        setGameState(message.gameState);

        setPlayerCards({
          [message.player1]: message.player1CardsNumber || 0,
          [message.player2]: message.player2CardsNumber || 0,
          [message.player3]: message.player3CardsNumber || 0,
          [message.player4]: message.player4CardsNumber || 0,
        });

        break;
      case "game.turnTable":
        setTableTurned(true);
        console.log("Table turned, updating state accordingly");
        break;
      case "game.talon":
        setGameState(message.gameState);
        setTalonCardsNumber(message.talonCards);
        console.log("Talon cards number updated:", message.talonCards);
        break;
      case "game.cardNumber":
        console.log("Card number update received for", message.username);
        setPlayerCards((prev) => ({
          ...prev,
          [message.username]: message.cardsNumber,
        }));
        break;
      case "game.lastDeal":
        setGameState("BIDDING");
        setPublicInformation("Bidding phase has started!");
        setPlayerCards((prev) => ({
          ...prev,
          [message.username]: message.cardsNumber,
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
      case "game.publicSkartInfo":
        setDeclarerSkartLength(message.declarerSkartLength);
        setOpponentSkartLength(message.opponentSkartLength);
        setPlayerCards((prev) => ({
          ...prev,
          [message.username]: message.playerHandLength,
        }));
        setTurnPlayer(message.turnPlayer);
        setPublicInformation(message.discardedCardsInfo);
        console.log("Public skart info updated:", message);
        break;
      case "game.tarokkInSkart":
        setDeclarerSkart(message.cards);
        setPublicInformation(message.info);
        console.log("Declarer's skart with tarokk updated:", message.cards);
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
        console.log("Trick cards updated:", message.cards);
        break;
        case "game.newTrickRound":
          setTrickCards([]);
          setTrickCard(null);
          console.log(message.textContent);
          break;
      default:
        console.log("Unhandled message type:", message.type);
        break;
    }
  }, []);

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
      case "game.firstPotentialBonuses":
        console.log("First potential bonuses received:", message);
        setPotentialBonuses(message.bonuses);
        setCallableTarokks(message.callableTarokks);
        setHasEightTarokks(message.hasEightTarokks);
        setHasNineTarokks(message.hasNineTarokks);
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
      default:
        console.log("Unhandled private message type:", message.type);
        break;
    }
  }, []);

  useEffect(() => {
    subscribe({
      destination: `/topic/game.${game.gameId}`,
      callback: handlePublicMessage,
    });
    subscribe({
      destination: `/user/queue/private`,
      callback: handlePrivateMessage,
    });
  }, [game.gameId, subscribe]);

  useEffect(setSeatModifier, []);

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

  const playerSeats = [
    player1,
    player2,
    player3,
    player4,
    player1,
    player2,
    player3,
  ];

  const playerBalances = [
    player1Balance,
    player2Balance,
    player3Balance,
    player4Balance,
    player1Balance,
    player2Balance,
    player3Balance,
  ];

  const playerCardsNumbers = [
    playerCards[player1 || ""] || 0,
    playerCards[player2 || ""] || 0,
    playerCards[player3 || ""] || 0,
    playerCards[player4 || ""] || 0,
    playerCards[player1 || ""] || 0,
    playerCards[player2 || ""] || 0,
    playerCards[player3 || ""] || 0,
  ];

  function getSeatAttributes(seatModifier: number) {
    //console.log("getSeatAttributes called with seatModifier:", seatModifier);
    return [
      {
        playerSeat: playerSeats[0 + seatModifier],
        playerBalance: playerBalances[0 + seatModifier],
        playerCardsNumber: playerCardsNumbers[0 + seatModifier],
      },
      {
        playerSeat: playerSeats[1 + seatModifier],
        playerBalance: playerBalances[1 + seatModifier],
        playerCardsNumber: playerCardsNumbers[1 + seatModifier],
      },
      {
        playerSeat: playerSeats[2 + seatModifier],
        playerBalance: playerBalances[2 + seatModifier],
        playerCardsNumber: playerCardsNumbers[2 + seatModifier],
      },
      {
        playerSeat: playerSeats[3 + seatModifier],
        playerBalance: playerBalances[3 + seatModifier],
        playerCardsNumber: playerCardsNumbers[3 + seatModifier],
      },
    ];
  }

  function setSeatModifier() {
    //console.log("Setting seat modifier for user:", user?.username);
    switch (user?.username) {
      case player1:
        seatModifier.current = 0;
        console.log("Seat modifier set to 0 for player1");
        break;
      case player2:
        seatModifier.current = 1;
        //console.log("Seat modifier set to 1 for player2");
        break;
      case player3:
        seatModifier.current = 2;
        //console.log("Seat modifier set to 2 for player3");
        break;
      case player4:
        seatModifier.current = 3;
        console.log("Seat modifier set to 3 for player4");
        break;
      default:
        seatModifier.current = 0;
    }
  }

  function renderOwnHand(seatAttribute: SeatAttributes) {
    return (
      <div className="flex flex-col h-full">
        <div className="h-1/8 flex justify-center items-center font-bold text-xl">
          <p>{seatAttribute.playerSeat}</p>
        </div>
        {(gameState === "NEW" ||
          gameState === "BIDDING" ||
          gameState === "TALON_PICK_UP" ||
          gameState === "BONUS_ANNOUNCEMENT") && (
          <div className="flex justify-center items-center h-2/3">
            {displayOwnCards()}
          </div>
        )}
        {gameState === "SKART_LAY_DOWN" && turnPlayer === user?.username && (
          <div className="flex justify-center items-center h-2/3">
            {displayOwnCardsWithTalon()}
          </div>
        )}
        {gameState === "SKART_LAY_DOWN" && turnPlayer !== user?.username && (
          <div className="flex justify-center items-center h-2/3">
            {displayOwnCards()}
          </div>
        )}
        {gameState === "TRICK_PHASE" && turnPlayer === user?.username && (
          <div className="flex justify-center items-center h-2/3">
            {displayOwnPlayableCards()}
          </div>
        )}
        {gameState === "TRICK_PHASE" && turnPlayer !== user?.username && (
          <div className="flex justify-center items-center h-2/3">
            {displayOwnCards()}
          </div>
        )}
        <div className="flex justify-center items-center font-bold text-xl h-auto">
          {renderBidButtons()}
        </div>
      </div>
    );
  }

  function renderBidButtons() {
    return potentialBids.map((bid) => (
      <button
        key={bid}
        className="border-black border-2 w-1/8 h-13 rounded-lg font-semibold"
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

  function createPrivateInfo() {
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

  useEffect(() => {
    if (gameState === "BONUS_ANNOUNCEMENT" && turnPlayer === user?.username) {
      setPrivateInformation(createPrivateInfo());
    }
  }, [
    selectedTarokkNumber,
    calledTarokk,
    selectedBonuses,
    turnPlayer,
    user,
    gameState,
  ]);

  function renderTarokkNumberButton() {
    if (hasEightTarokks) {
      return (
        <button
          className="border-black border-2 w-32 h-10 ml-5 mr-5"
          onClick={() => {
            setSelectedTarokkNumber(8);
            console.log("Tarokk number selected: 8");
          }}
        >
          8 Tarokks
        </button>
      );
    } else if (hasNineTarokks) {
      return (
        <button
          className="border-black border-2 w-32 h-10 ml-5 mr-5"
          onClick={() => {
            setSelectedTarokkNumber(9);
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
        className="border-black border-2 w-32 h-10 ml-5 mr-5 font-semibold rounded-md"
        onClick={() => {
          setCalledTarokk(tarokk);
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
        className="border-black border-2 w-40 h-14 rounded-md font-semibold"
        onClick={() => {
          setSelectedBonuses((prev) =>
            prev.includes(bonus)
              ? prev.filter((b) => b !== bonus)
              : [...prev, bonus],
          );
        }}
      >
        {bonus}
      </button>
    ));
    const confirmButton = (
      <button
        className="border-black border-2 w-40 h-14 bg-green-300 rounded-md font-semibold"
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
          } else {
            if (
              firstBonusRound &&
              declarer === user?.username &&
              !calledTarokk &&
              selectedBonuses.length === 0
            ) {
              setPrivateInformation(
                "Még nem hívtál meg tarokkot és nem választottál bonust",
              );
            } else if (
              firstBonusRound &&
              declarer === user?.username &&
              !calledTarokk
            ) {
              setPrivateInformation("Még nem hívtál meg tarokkot");
            } else if (
              firstBonusRound &&
              declarer === user?.username &&
              selectedBonuses.length === 0
            ) {
              setPrivateInformation("Nem választottál bonust");
            } else {
              setPrivateInformation("Nem választottál bonust");
            }
          }
        }}
      >
        Submit
      </button>
    );
    return [...buttons, confirmButton];
  }

  function renderPlayerHand(seatAttribute: SeatAttributes) {
    return (
      <div className="flex flex-col h-full">
        <div className="h-1/6 flex justify-center items-center font-bold text-xl">
          <p>{seatAttribute.playerSeat}</p>
        </div>

        <div className="flex justify-center items-center h-2/3 relative -top-5">
          {displayHandBack(seatAttribute.playerCardsNumber)}
        </div>
        <div className="h-1/6 flex justify-center items-center font-bold text-xl">
          <p>Balance: {seatAttribute.playerBalance}</p>
        </div>
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
        className="w-20 -mx-14"
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
          className="w-20 -m-10"
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
        className="w-20 mx-1"
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
    return trickCards.map((card, index) => (
      <img
        key={index}
        src={card.imagePath}
        alt={`Trick card ${index + 1}`}
        className="w-20 mx-1"
      />
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
              className="border-black border-2 w-1/8 h-2/3 bg-green-300 text-[#2f4b3a] text-2xl font-bold rounded-lg"
              onClick={handleLogout}
            >
              Logout
            </button>
            <button className="border-black border-2 w-1/8 h-2/3 bg-green-300 text-[#2f4b3a] text-2xl font-bold rounded-lg">
              Back
            </button>
            {user?.username === dealer && gameState === "NEW" && (
              <button
                className="border-black border-2 w-1/8 h-2/3 bg-green-300 text-[#2f4b3a] text-2xl font-bold rounded-lg"
                onClick={handleDeal}
              >
                DEAL
              </button>
            )}
          </div>

          {/* Game area */}
          <div className="w-full h-11/12 flex flex-col">
            {/* Top row */}
            <div className="w-full h-1/3 flex">
              {/* Declarer's skart area */}
              <div className="w-1/4">
                <div className="h-1/6 flex justify-center items-end font-bold text-xl">
                  <p>Declarer's skart</p>
                </div>
                <div className="flex justify-center items-center h-5/6">
                  {declarerSkart.length > 0
                    ? displayDeclarerSkartWithTarokk()
                    : displaySkart(declarerSkartLength)}
                </div>
              </div>

              {/* Player 3's area */}
              <div className="w-1/2">
                {renderPlayerHand(getSeatAttributes(seatModifier.current)[2])}
              </div>

              {/* Opponent's skart area */}
              <div className="w-1/4">
                <div className="h-1/6 flex justify-center items-end font-bold text-xl">
                  <p>Opponent's skart</p>
                </div>
                <div className="flex justify-center items-center h-5/6">
                  {displaySkart(opponentSkartLength)}
                </div>
              </div>
            </div>

            {/* Middle row */}
            <div className="w-full h-1/3 flex">
              {/* Player 4's area */}
              <div className="w-1/4">
                {renderPlayerHand(getSeatAttributes(seatModifier.current)[3])}
              </div>

              {/* Talon and play area */}
              <div className="w-1/2 bg-gray-400">
                {talonCardsNumber > 0 && (
                  <Talon talonCardsNumber={talonCardsNumber} />
                )}
                {gameState === "SKART_LAY_DOWN" &&
                  (ownCards.length > 9 ||
                    temporarySelectedCards.length > 0) && (
                    <Skart
                      temporarySelectedCards={temporarySelectedCards}
                      onDisplayTemporarySelectedCards={() => (
                        <>{displayTemporarySelectedCards()}</>
                      )}
                      cardsToDiscard={cardsToDiscard}
                      sendSkartCards={sendSkartCards}
                    />
                  )}
                {gameState === "BONUS_ANNOUNCEMENT" &&
                  turnPlayer === user?.username && (
                    <div className="h-full flex flex-col justify-center items-center">
                      {hasEightTarokks || hasNineTarokks ? (
                        <div className="w-full bg-pink-300 flex justify-center items-center">
                          {renderTarokkNumberButton()}
                        </div>
                      ) : null}
                      {declarer && callableTarokks.length > 0 && (
                        <div className="w-full bg-pink-400 flex justify-center items-center">
                          {renderCallableTarokkButtons()}
                        </div>
                      )}

                      <div className="w-full bg-pink-500 flex flex-col justify-center items-center">
                        <p className="h-1/4 font-semibold mt-1 text-lg">
                          Select your bonuses:
                        </p>
                        <div className="h-3/4 w-full grid grid-flow-row grid-cols-4">
                          {renderBonusButtons()}
                        </div>
                      </div>
                    </div>
                  )}
                  {gameState === "TRICK_PHASE" && (
                    <div className="h-full flex flex-col justify-center items-center">
                      <div className="flex justify-center items-center">
                        {renderTrickCards()}
                      </div>
                    </div>
                  )}
              </div>

              {/* Player 2's area */}
              <div className="w-1/4">
                {renderPlayerHand(getSeatAttributes(seatModifier.current)[1])}
              </div>
            </div>

            {/* Bottom row */}
            <div className="w-full h-1/3">
              {renderOwnHand(getSeatAttributes(seatModifier.current)[0])}
            </div>
          </div>
        </div>

        {/*Info table */}
        <InfoTable
          bid={bid}
          declarer={declarer}
          dealer={dealer}
          bidPlayer={bidPlayer}
          turnPlayer={turnPlayer}
          publicInfo={publicInformation}
          privateInfo={privateInformation}
          selectedTarokkNumber={selectedTarokkNumber}
          gameState={gameState}
          user={user}
          calledTarokk={calledTarokk}
          selectedBonuses={selectedBonuses}
          declarerBonuses={declarerBonuses}
          opponentBonuses={opponentBonuses}
        />
      </div>
    </div>
  );
}

export default Game;
