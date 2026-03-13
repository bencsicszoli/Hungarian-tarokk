import { useState, useEffect, useRef } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useUser } from "./context/UserContext";
import { useWebSocket } from "./context/WebSocketContext";

const INITIAL_ROTATION = [0, 0, 22.5, -30, -37.5, -45, -52.5, -60, -67.5, -75];

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
  const [turnPlayer, setTurnPlayer] = useState<string | null>(
    game?.turnPlayer || null,
  );
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
  const [player1CardsNumber, setPlayer1CardsNumber] = useState<number>(
    game?.player1CardsNumber || 0,
  );
  const [player2CardsNumber, setPlayer2CardsNumber] = useState<number>(
    game?.player2CardsNumber || 0,
  );
  const [player3CardsNumber, setPlayer3CardsNumber] = useState<number>(
    game?.player3CardsNumber || 0,
  );
  const [player4CardsNumber, setPlayer4CardsNumber] = useState<number>(
    game?.player4CardsNumber || 0,
  );
  const [ownCards, setOwnCards] = useState<{ imagePath: string }[]>([]);
  const [gameState, setGameState] = useState<"NEW" | "IN_PROGRESS" | "BIDDING">(
    game?.gameState || "NEW",
  );
  const [tableTurned, setTableTurned] = useState(false);
  const [talonCardsNumber, setTalonCardsNumber] = useState<number>(0);
  const [levelDiscription, setLevelDescription] = useState<string>(
    game?.levelDescription || "None",
  );
  const [potentialBids, setPotentialBids] = useState<string[]>([]);

  const seatModifier = useRef(0);

  useEffect(() => {
    if (!user || !connected) navigate(`/`);
  }, [user, connected, navigate]);

  useEffect(() => {
    subscribe({
      destination: `/topic/game.${game.gameId}`,
      callback: handlePublicMessage,
    });
    subscribe({
      destination: `/user/queue/private`,
      callback: handlePrivateMessage,
    });
  }, [game, subscribe, handlePublicMessage, handlePrivateMessage]);

  useEffect(setSeatModifier, []);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  function handlePublicMessage(payload: unknown) {
    const message = JSON.parse(
      (payload as Record<string, unknown>).body as string,
    );
    console.log("Public message received:", message);
    switch (message.type) {
      case "game.joined":
        // Update player states based on the message content
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

        break;
      // Handle other message types as needed
      case "game.turnTable":
        setTableTurned(true);
        console.log("Table turned, updating state accordingly");
        break;
      case "game.talon":
        setTalonCardsNumber(message.talonCards);
        setGameState(message.gameState);
        console.log("Talon cards number updated:", message.talonCards);
        break;
      case "game.cardNumber":
        console.log("Card number update received for", message.username);
        setPlayerCardNumber({
          username: message.username,
          cardsNumber: message.cardsNumber,
        });
        break;
      case "game.lastDeal":
        setGameState("BIDDING");
        setPlayerCardNumber({
          username: message.username,
          cardsNumber: message.cardsNumber,
        });
        console.log("Game state updated to BIDDING");
        break;
      default:
        console.log("Unhandled message type:", message.type);
        break;
    }
    // Handle game state updates here
  }

  // eslint-disable-next-line react-hooks/exhaustive-deps
  function handlePrivateMessage(payload: unknown) {
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
      case "game.bid":
        console.log("Bid message received:", message);
        break;
      case "game.firstPotentialBids":
        console.log("First potential bids received:", message);
        setPotentialBids(message.potentialBids);
        break;
      default:
        console.log("Unhandled private message type:", message.type);
        break;
    }
  }

  useEffect(() => {
    if (gameState === "BIDDING" && user?.username === startPlayer) {
      getFirstPotentialBids();
    }
  }, [gameState, user, startPlayer]);

  function setPlayerCardNumber(message: {
    username: string;
    cardsNumber: number;
  }) {
    if (message.username === player1)
      setPlayer1CardsNumber(message.cardsNumber);
    if (message.username === player2)
      setPlayer2CardsNumber(message.cardsNumber);
    if (message.username === player3)
      setPlayer3CardsNumber(message.cardsNumber);
    if (message.username === player4)
      setPlayer4CardsNumber(message.cardsNumber);
  }

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
    player1CardsNumber,
    player2CardsNumber,
    player3CardsNumber,
    player4CardsNumber,
    player1CardsNumber,
    player2CardsNumber,
    player3CardsNumber,
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

  interface SeatAttributes {
    playerSeat: string | null;
    playerBalance: number;
    playerCardsNumber: number;
  }

  function renderOwnHand(seatAttribute: SeatAttributes) {
    //console.log("Rendering player hand for seat:", seatAttribute.playerSeat);
    //console.log("Player balance:", seatAttribute.playerBalance);
    return (
      <div className="flex flex-col h-full">
        <div className="h-1/6 flex justify-center items-center font-bold text-xl">
          <p>{seatAttribute.playerSeat}</p>
        </div>

        <div className="flex justify-center items-center h-2/3">
          {displayOwnCards()}
        </div>
        <div className="h-1/6 flex justify-center items-center font-bold text-xl">
          {renderBidButtons()}
        </div>
      </div>
    );
  }

  function renderBidButtons() {
    return potentialBids.map((bid) => (
      <button
        key={bid}
        className="border-black border-2 w-1/8 h-2/3"
        
        onClick={() => {
          send("/app/game.bid", {
            username: user?.username,
            gameId: game.gameId,
            bid: bid,
          });
          console.log("Bid sent:", user?.username, game.gameId, bid);
          setPotentialBids([]);
        }}
      >
        {bid}
      </button>
    ));
  }

  function renderPlayerHand(seatAttribute: SeatAttributes) {
    //console.log("Rendering player hand for seat:", seatAttribute.playerSeat);
    //console.log("Player balance:", seatAttribute.playerBalance);
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

  function displayTalon(talonCardsNumber: number) {
    const cardsBack = Array(talonCardsNumber).fill("Back.png");
    return cardsBack.map((imagePath, index) => (
      <img
        key={index}
        src={imagePath}
        alt="Talon card back"
        className="w-20 -mx-14"
      />
    ));
  }

  function displayHandBack(cardsNumber: number) {
    // the rotation utility classes are computed at runtime so Tailwind can’t
    // generate them; switch to inline styles instead.
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
      rotation += 20;
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

  return (
    <div className="min-h-screen flex items-center justify-center bg-table-background px-4 py-8">
      <div className="p-4 sm:p-6 bg-[#4B2E1F] rounded-[90px] shadow-inner w-full flex gap-4 justify-around">
        {/*Card table */}
        <div className="w-3/4 h-207.5 bg-poker-table rounded-[70px] shadow-2xl text-white px-6 sm:px-8 flex flex-col items-center justify-center">
          {/* Menu buttons */}
          <div className="w-full h-1/12 bg-green-300 flex justify-around items-center">
            <button
              className="border-black border-2 w-1/8 h-2/3"
              onClick={handleLogout}
            >
              Logout
            </button>
            <button className="border-black border-2 w-1/8 h-2/3">Back</button>
            {user?.username === dealer && gameState === "NEW" && (
              <button
                className="border-black border-2 w-1/8 h-2/3"
                onClick={handleDeal}
              >
                DEAL
              </button>
            )}
          </div>

          {/* Game area */}
          <div className="w-full h-11/12 flex flex-col">
            {/* Top row */}
            <div className="w-full h-1/3 bg-gray-400 flex">
              {/* Declarer's skart area */}
              <div className="w-1/3">
                <p>Declarer's skart</p>
              </div>

              {/* Player 3's area */}
              <div className="w-1/3 bg-gray-600">
                {/*
                <p>{player3}</p>
                <p>Balance: {player3Balance}</p>*/}
                {renderPlayerHand(getSeatAttributes(seatModifier.current)[2])}
              </div>

              {/* Opponent's skart area */}
              <div className="w-1/3">
                <p>Opponent's skart</p>
              </div>
            </div>

            {/* Middle row */}
            <div className="w-full h-1/3 bg-orange-300 flex">
              {/* Player 4's area */}
              <div className="w-1/3">
                {/*
                <p>{player4}</p>
                <p>Balance: {player4Balance}</p>*/}
                {renderPlayerHand(getSeatAttributes(seatModifier.current)[3])}
              </div>

              {/* Talon and play area */}
              <div className="w-1/3 bg-gray-400">
                <div className="h-1/6 flex justify-center items-end font-bold text-xl">
                  <p>Talon</p>
                </div>
                <div className="flex justify-center items-center h-5/6">
                  {displayTalon(talonCardsNumber)}
                </div>
              </div>

              {/* Player 2's area */}
              <div className="w-1/3">
                {/*
                <p>{player2}</p>
                <p>Balance: {player2Balance}</p>*/}
                {renderPlayerHand(getSeatAttributes(seatModifier.current)[1])}
              </div>
            </div>

            {/* Bottom row */}
            <div className="w-full h-1/3">
              {/*
              <p>{player1}</p>
              <p>Balance: {player1Balance}</p>*/}
              {renderOwnHand(getSeatAttributes(seatModifier.current)[0])}
            </div>
          </div>
        </div>

        {/*Info table */}
        <div className="w-100 h-207.5 bg-info-table rounded-[70px] shadow-2xl text-white px-6 sm:px-8 flex flex-col">
          <div className="w-full h-1/4 bg-blue-300"></div>
          <div className="w-full h-1/4 bg-blue-500"></div>
          <div className="w-full h-1/4 bg-blue-300"></div>
          <div className="w-full h-1/4 bg-blue-500"></div>
        </div>
      </div>
    </div>
  );
}

export default Game;
