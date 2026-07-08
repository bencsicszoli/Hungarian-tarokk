import type { JSX } from "react";
import { useTranslation } from "react-i18next";
import type { Card, GameState, InfoLine } from "../types";

function Skart({
  gameState,
  discardInformation,
  ownCards,
  temporarySelectedCards,
  onDisplayTemporarySelectedCards,
  cardsToDiscard,
  sendSkartCards,
  turnPlayer,
  user,
}: {
  gameState: GameState;
  discardInformation: InfoLine[] | null;
  ownCards: Card[];
  temporarySelectedCards: Card[];
  onDisplayTemporarySelectedCards: () => JSX.Element;
  cardsToDiscard: React.RefObject<number>;
  sendSkartCards: () => void;
  turnPlayer: string | null;
  user: { username: string } | null;
}) {
  const { t } = useTranslation();
  if (
    gameState === "SKART_LAY_DOWN" &&
    discardInformation === null &&
    (ownCards.length > 9 || temporarySelectedCards.length > 0)
  ) {
    return (
      <div className="flex h-full justify-center gap-4">
        <div className="w-auto flex justify-center items-center font-bold text-xl">
          {turnPlayer === user?.username && (
            <p className="border-green-300 border-2 rounded-md text-green-300 px-4 py-1">
              {t("game.skartHereLabel")}
            </p>
          )}
        </div>
        <div className="flex justify-center items-center w-auto">
          {onDisplayTemporarySelectedCards()}
        </div>
        <div className="w-auto flex justify-center items-center font-bold text-xl">
          {cardsToDiscard.current === temporarySelectedCards.length && (
            <button
              className="border-black border-2 w-30 h-10 hover:scale-105 hover:bg-green-400 cursor-pointer bg-green-300 text-[#2f4b3a] rounded-md font-semibold mb-2"
              onClick={sendSkartCards}
            >
              {t("game.submit")}
            </button>
          )}
        </div>
      </div>
    );
  }
}

export default Skart;
