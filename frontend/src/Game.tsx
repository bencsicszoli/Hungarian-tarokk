import { useState, useEffect} from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useUser } from "./context/UserContext";
import { useWebSocket } from "./context/WebSocketContext";

function Game() {

  console.log("GamePage reached");
  const location = useLocation();
  const navigate = useNavigate();
  const userContext = useUser();
  const webSocketContext = useWebSocket();
  const { game } = location.state || {};
  const { user } = userContext || { user: null };
  const { subscribe } = webSocketContext || {
    subscribe: () => null
  };

  const [player1, setPlayer1] = useState<string | null>(game?.player1 || null);
  const [player2, setPlayer2] = useState<string | null>(game?.player2 || null);
  const [player3, setPlayer3] = useState<string | null>(game?.player3 || null);
  const [player4, setPlayer4] = useState<string | null>(game?.player4 || null);
  const [player1Balance, setPlayer1Balance] = useState<number>(game?.player1Balance || 0);
  const [player2Balance, setPlayer2Balance] = useState<number>(game?.player2Balance || 0);
  const [player3Balance, setPlayer3Balance] = useState<number>(game?.player3Balance || 0);
  const [player4Balance, setPlayer4Balance] = useState<number>(game?.player4Balance || 0);

  useEffect(() => {
    if (!user) navigate(`/`);
  }, [user, navigate]);

  useEffect(() => {
    subscribe({destination:`/topic/game.${game.gameId}`, callback: handlePublicMessage});
    subscribe({destination:`/user/queue/private`, callback: handlePrivateMessage});
  }, [game, subscribe, handlePublicMessage, handlePrivateMessage]);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  function handlePublicMessage(payload: unknown) {
    const message = JSON.parse((payload as Record<string, unknown>).body as string);
    console.log("Public message received:", message);
    switch (message.type) {
      case "game.joined":
        // Update player states based on the message content
        setPlayer1(message.player1);
        setPlayer2(message.player2);
        setPlayer3(message.player3);
        setPlayer4(message.player4);
        setPlayer1Balance(message.player1Balance);
        setPlayer2Balance(message.player2Balance);
        setPlayer3Balance(message.player3Balance);
        setPlayer4Balance(message.player4Balance);
        break;
      // Handle other message types as needed
      default:
        console.log("Unhandled message type:", message.type);
        break;
    }
    // Handle game state updates here
  }

  // eslint-disable-next-line react-hooks/exhaustive-deps
  function handlePrivateMessage(payload: unknown) {
    const message = JSON.parse((payload as Record<string, unknown>).body as string);
    console.log("Private message received:", message);
    // Handle hand updates here
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-table-background px-4 py-8">
      <div className="p-4 sm:p-6 bg-[#4B2E1F] rounded-[90px] shadow-inner w-full flex gap-4 justify-around">
        
        {/*Card table */}
        <div className="w-3/4 h-207.5 bg-poker-table rounded-[70px] shadow-2xl text-white px-6 sm:px-8 flex flex-col items-center justify-center">
          
          {/* Menu buttons */}
          <div className="w-full h-1/12 bg-green-300"></div>

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
              <p>{player3}</p>
              <p>Balance: {player3Balance}</p>
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
              <p>{player4}</p>
              <p>Balance: {player4Balance}</p>
              </div>

              {/* Talon and play area */}
              <div className="w-1/3 bg-gray-400">
              <p>Talon</p>
              </div>

              {/* Player 2's area */}
              <div className="w-1/3">
              <p>{player2}</p>
              <p>Balance: {player2Balance}</p>
              </div>
            </div>

            {/* Bottom row */}
            <div className="w-full h-1/3 bg-blue-300">
            <p>{player1}</p>
            <p>Balance: {player1Balance}</p>
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
