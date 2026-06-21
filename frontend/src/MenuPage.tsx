import { useNavigate } from "react-router-dom";
import { useCallback, useEffect, useState } from "react";
import { useUser } from "./context/UserContext";
import { useWebSocket } from "./context/WebSocketContext";
import LinkButton from "./menuPageComponents/LinkButton";

function MenuPage() {
  const navigate = useNavigate();
  const userContext = useUser();
  const webSocketContext = useWebSocket();
  const { user, setUser, token, setToken } = userContext || {
    user: null,
    setUser: () => {},
    token: null,
    setToken: () => {},
  };
  const { connected, subscribe, send } = webSocketContext || {
    connected: false,
    subscribe: () => null,
    send: () => {},
  };
  const [customGameId, setCustomGameId] = useState<string>("");
  const [joinWithIdClicked, setJoinWithIdClicked] = useState<boolean>(false);
  const [copiedId, setCopiedId] = useState<string | null>(null);
  const [getGameIdClicked, setGetGameIdClicked] = useState<boolean>(false);
  const [gameIdToSend, setGameIdToSend] = useState<string | null>(null);
  const [feedbackOpen, setFeedbackOpen] = useState<boolean>(false);
  const [feedbackText, setFeedbackText] = useState<string>("");
  const [feedbackStatus, setFeedbackStatus] = useState<
    "idle" | "sending" | "sent" | "error"
  >("idle");

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

  async function getGameId() {
    setGetGameIdClicked(true);
    const response = await fetch(`/api/auth/customId`, {
      headers: { Authorization: "Bearer " + token },
    });
    if (!response.ok) throw new Error("Could not get a game ID");
    const id = await response.json();
    console.log("Custom game ID: ", id);
    setCustomGameId(id);
  }

  function joinGameWithId() {
    if (!connected) {
      console.warn("WebSocket not connected yet, cannot join game.");
      return;
    }
    setJoinWithIdClicked(true);
  }

  function submitId(e: { preventDefault: () => void }) {
    e.preventDefault();
    send("/app/game.joinWithId", {
      username: user?.username,
      gameId: gameIdToSend,
    });
    setJoinWithIdClicked(false);
    console.log("Join request sent with id ", customGameId);
  }

  const handleLogout = () => {
    setToken(null);
    setUser(null);
    navigate("/");
  };

  const handleHelpClick = () => {
    window.open("https://www.pagat.com/tarot/xx-hivas.html");
  };

  async function submitFeedback(e: { preventDefault: () => void }) {
    e.preventDefault();
    if (!feedbackText.trim()) return;
    setFeedbackStatus("sending");
    try {
      const response = await fetch(`/api/feedback`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer " + token,
        },
        body: JSON.stringify({
          message: feedbackText,
          userAgent: navigator.userAgent,
        }),
      });
      if (!response.ok) throw new Error("Failed to send feedback");
      setFeedbackStatus("sent");
      setFeedbackText("");
    } catch (err) {
      console.error("Could not send feedback:", err);
      setFeedbackStatus("error");
    }
  }

  function closeFeedback() {
    setFeedbackOpen(false);
    setFeedbackStatus("idle");
    setFeedbackText("");
  }

  return (
    <div className="w-full h-screen bg-[#2f4b3a] text-white px-6 sm:px-8 flex justify-center items-center">
      <div className="w-1/5 h-1/5">
      {getGameIdClicked && (<div className=" flex justify-center items-center border border-green-300 rounded-lg">
          <div className="flex flex-col w-4/5">
            <div className="m-2">
              <label htmlFor="" className="text-lg font-medium text-green-50">
                Your game ID:
              </label>
            </div>

            <input
              type="text"
              placeholder="Waiting for the ID..."
              value={customGameId}
              className="h-12 font-bold text-2xl text-center text-[#2f4b3a] bg-green-300 rounded-lg m-2 placeholder:text-[#2f4b3a] placeholder:font-normal placeholder: text-md"
            />
            <div className="m-2 flex justify-center items-center">
              <button
                type="submit"
                className="bg-green-700 rounded-lg w-42 h-10 border border-green-300 hover:scale-105 hover:bg-green-900 cursor-pointer"
                onClick={async () => {
                  await navigator.clipboard.writeText(customGameId);
                  setCopiedId("write-text");
                }}
              >
                {copiedId === "write-text" ? "Copied" : "Copy ID to clipboard"}
              </button>
            </div>
          </div>
        </div>)}
        
      </div>
      <div className="w-1/3 flex flex-col justify-center items-center">
        <h2 className="text-3xl font-extrabold mb-11 drop-shadow-lg text-green-100 md:text-4xl">
          Select an option:
        </h2>

        <div className="w-full bg-[#2f4b3a] border border-green-300 rounded-lg shadow md:mt-0 sm:max-w-md xl:p-0">
          <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
            <LinkButton
              buttonText="Join a random game"
              fontStyle="font-extrabold text-xl"
              onHandleClick={joinGame}
            />
            <LinkButton
              buttonText="Join a game with ID"
              fontStyle="font-extrabold text-xl"
              onHandleClick={joinGameWithId}
            />
            <LinkButton
              buttonText="Get an ID to invite players"
              fontStyle="font-semibold text-lg"
              onHandleClick={getGameId}
            />
            <LinkButton
              whereToLink={`/edit`}
              buttonText="Edit profile"
              fontStyle="font-semibold text-lg"
            />
            <LinkButton
              buttonText="Feedback"
              onHandleClick={() => setFeedbackOpen(true)}
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
      <div className="w-1/5 h-1/5">
        {joinWithIdClicked && (
          <div className=" border border-green-300 rounded-lg flex justify-center items-center">
            <form onSubmit={submitId} className="flex flex-col w-4/5">
              <div className="m-2">
                <label htmlFor="" className="text-lg font-medium text-green-50">
                  Enter the ID:
                </label>
              </div>

              <input
                type="text"
                placeholder="Enter the ID here:"
                onChange={(e) => setGameIdToSend(e.target.value)}
                className="h-12 font-bold text-2xl text-center text-[#2f4b3a] bg-green-300 rounded-lg m-2 placeholder:text-[#2f4b3a] placeholder:font-normal placeholder: text-md"
              />
              <div className="m-2 flex justify-center items-center">
                <button
                  type="submit"
                  className="bg-green-700 rounded-lg w-40 h-10 border border-green-300 hover:scale-105 hover:bg-green-900 cursor-pointer"
                >
                  Send
                </button>
              </div>
            </form>
          </div>
        )}
      </div>

      {feedbackOpen && (
        <div
          className="fixed inset-0 z-50 flex justify-center items-center bg-black/60"
          onClick={closeFeedback}
        >
          <div
            className="w-11/12 max-w-md bg-[#2f4b3a] border border-green-300 rounded-lg p-6 shadow-lg"
            onClick={(e) => e.stopPropagation()}
          >
            {feedbackStatus === "sent" ? (
              <div className="flex flex-col items-center gap-4 text-center">
                <p className="text-lg font-medium text-green-50">
                  Thanks for your feedback!
                </p>
                <button
                  onClick={closeFeedback}
                  className="bg-green-700 rounded-lg w-32 h-10 border border-green-300 hover:scale-105 hover:bg-green-900 cursor-pointer"
                >
                  Close
                </button>
              </div>
            ) : (
              <form onSubmit={submitFeedback} className="flex flex-col gap-3">
                <label className="text-lg font-medium text-green-50">
                  Found a bug or have a suggestion?
                </label>
                <textarea
                  value={feedbackText}
                  onChange={(e) => setFeedbackText(e.target.value)}
                  placeholder="Describe the bug or your idea..."
                  rows={6}
                  className="w-full p-3 text-[#2f4b3a] bg-green-300 rounded-lg resize-none placeholder:text-[#2f4b3a]/70 focus:outline-none focus:ring-4 focus:ring-green-400"
                />
                {feedbackStatus === "error" && (
                  <p className="text-red-300 text-sm">
                    Could not send your feedback. Please try again later.
                  </p>
                )}
                <div className="flex justify-end gap-2">
                  <button
                    type="button"
                    onClick={closeFeedback}
                    className="rounded-lg px-4 h-10 border border-green-300 text-green-50 hover:bg-green-900 cursor-pointer"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={
                      feedbackStatus === "sending" || !feedbackText.trim()
                    }
                    className="bg-green-700 rounded-lg px-4 h-10 border border-green-300 hover:scale-105 hover:bg-green-900 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100"
                  >
                    {feedbackStatus === "sending" ? "Sending..." : "Send"}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default MenuPage;
