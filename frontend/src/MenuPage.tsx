import { useNavigate } from "react-router-dom";
import { useCallback, useEffect } from "react";
import { useUser } from "./context/UserContext";
import { useWebSocket } from "./context/WebSocketContext";
import CardTableDecoration from "./pageComponents/CardTableDecoration";
import LinkButton from "./pageComponents/LinkButton";

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
  /*
  useEffect(() => {
    if (!connected) return;

    const subscription = subscribe({ destination: '/user/queue/private', callback: setGameStateFirst });

    return () => subscription?.unsubscribe?.();
  }, [connected, setGameStateFirst, subscribe]);
*/

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
  /*
  // eslint-disable-next-line react-hooks/exhaustive-deps
  function setGameStateFirst(payload: unknown) {
    console.log("Raw private message received in MenuPage");
    if (typeof payload === "object" && payload !== null && "body" in payload && typeof (payload as Record<string, unknown>).body === "string") {
      const message: PayloadType = JSON.parse((payload as Record<string, unknown>).body as string);
      console.log("Private message received in MenuPage:", message);
      if (message.type === "game.joined" && message.gameId) {
        console.log("Joined successfully:", message);
        navigate("/game", { state: { game: message } });
      }
    }
  }
*/

  function joinGame() {
    if (!connected) {
      console.warn("WebSocket not connected yet, cannot join game.");
      return;
    }
    console.log("username:", user?.username);
    send("/app/game.join", { username: user?.username });
    console.log("Join game message sent", { username: user?.username });
    //navigate("/game");
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
    <div className="min-h-screen flex items-center justify-center bg-table-background px-4 py-8">
      <div className="p-4 sm:p-6 bg-[#4B2E1F] rounded-[90px] shadow-inner w-full max-w-7xl">
        <div className="w-full h-168 bg-poker-table rounded-[70px] shadow-2xl flex flex-col items-center justify-center relative text-white px-6 sm:px-8">
          <CardTableDecoration />
          <h2 className="text-3xl font-extrabold mb-11 drop-shadow-lg text-center text-white md:text-4xl">
            Select an option:
          </h2>

          <div className="w-full bg-white border border-gray-200 rounded-lg shadow dark:border-gray-700 dark:bg-gray-800 md:mt-0 sm:max-w-md xl:p-0">
            <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
              <LinkButton
                buttonText="PLAY"
                fontStyle="font-extrabold text-xl"
                onHandleClick={joinGame}
              />
              <LinkButton
                whereToLink={`/statistics`}
                buttonText="Statistics"
                fontStyle="font-medium text-lg"
              />
              <LinkButton
                whereToLink={`/editpage`}
                buttonText="Edit profile"
                fontStyle="font-medium text-lg"
              />
              <LinkButton
                whereToLink={`/`}
                buttonText="Bug report"
                fontStyle="font-medium text-lg"
              />
              <LinkButton
                buttonText="Rules"
                onHandleClick={handleHelpClick}
                fontStyle="font-medium text-lg"
              />
              <LinkButton
                whereToLink={`/`}
                buttonText="Logout"
                onHandleClick={handleLogout}
                fontStyle="font-medium text-lg"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default MenuPage;
