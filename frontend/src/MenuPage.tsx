import { useNavigate } from "react-router-dom";
import { useCallback, useEffect } from "react";
import { useUser } from "./context/UserContext";
import { useWebSocket } from "./context/WebSocketContext";
import CardTableDecoration from "./gamePageComponents/CardTableDecoration";
import LinkButton from "./menuPageComponents/LinkButton";

function MenuPage() {
  const navigate = useNavigate();
  const userContext = useUser();
  const webSocketContext = useWebSocket();
  const { user, setUser, setToken } = userContext || {
    user: null,
    setUser: () => {},
    setToken: () => {},
  };
  const { connected, subscribe, send } = webSocketContext || {
    connected: false,
    subscribe: () => null,
    send: () => {},
  };

  console.log("User: ", user);
  useEffect(() => {
    if (!user) navigate(`/`);
  }, [user, navigate]);

  const setGameStateFirst = useCallback(
    (payload: unknown) => {
      if (
        typeof payload === "object" &&
        payload !== null &&
        "body" in payload &&
        typeof (payload as Record<string, unknown>).body === "string"
      ) {
        const message: PayloadType = JSON.parse(
          (payload as Record<string, unknown>).body as string,
        );
        console.log("Private message received in MenuPage:", message);
        if (message.type === "game.joined" && message.gameId) {
          navigate("/game", { state: { game: message } });
        }
      }
    },
    [navigate],
  );

  useEffect(() => {
    if (!connected) return;
    const sub = subscribe({
      destination: "/user/queue/private",
      callback: setGameStateFirst,
    });
    return () => sub.unsubscribe();
  }, [connected, subscribe, setGameStateFirst]);

  interface PayloadType {
    type: string;
    gameId?: number;
    [key: string]: unknown;
  }

  function joinGame() {
    if (!connected) {
      console.warn("WebSocket not connected yet, cannot join game.");
      return;
    }
    send("/app/game.join", { username: user?.username });
  }

  const handleLogout = () => {
    setToken(null);
    setUser(null);
    navigate("/");
  };

  const handleHelpClick = () => {
    window.open("https://www.pagat.com/tarot/xx-hivas.html");
  };

  return (
    <div className="w-full h-screen bg-[#2f4b3a] flex flex-col items-center justify-center text-white px-6 sm:px-8">
      <CardTableDecoration />
      <h2 className="text-3xl font-extrabold mb-11 drop-shadow-lg text-center text-green-100 md:text-4xl">
        Select an option:
      </h2>

      <div className="w-full bg-[#2f4b3a] border border-green-300 rounded-lg shadow md:mt-0 sm:max-w-md xl:p-0">
        <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
          <LinkButton
            buttonText="PLAY"
            fontStyle="font-extrabold text-xl"
            onHandleClick={joinGame}
          />
          <LinkButton
            whereToLink={`/statistics`}
            buttonText="Statistics"
            fontStyle="font-semibold text-lg"
          />
          <LinkButton
            whereToLink={`/editpage`}
            buttonText="Edit profile"
            fontStyle="font-semibold text-lg"
          />
          <LinkButton
            whereToLink={`/`}
            buttonText="Bug report"
            fontStyle="font-semibold text-lg"
          />
          <LinkButton
            buttonText="Rules"
            onHandleClick={handleHelpClick}
            fontStyle="font-semibold text-lg"
          />
          <LinkButton
            whereToLink={`/`}
            buttonText="Logout"
            onHandleClick={handleLogout}
            fontStyle="font-semibold text-lg"
          />
        </div>
      </div>
    </div>
  );
}

export default MenuPage;
