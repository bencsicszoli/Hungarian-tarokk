import type { JSX } from "react";
import { useTranslation } from "react-i18next";
import type { CardImage, GameState } from "../types.ts";

function OpponentSkart({
  opponentSkartLength,
  gameState,
  publicOpponentSkart,
  onDisplaySkart,
  onDisplayPublicCards,
}: {
  opponentSkartLength: number;
  gameState: GameState;
  publicOpponentSkart: CardImage[];
  onDisplaySkart: (length: number) => JSX.Element[];
  onDisplayPublicCards: (cards: CardImage[]) => JSX.Element;
}) {
  const { t } = useTranslation();
  return (
    <div className="w-1/4 grow">
      <div className="h-1/6 flex justify-center items-center font-bold text-xl text-green-100">
        {opponentSkartLength > 0 && <p>{t("game.opponentSkart")}</p>}
      </div>
      <div className="flex justify-center items-start h-5/6 mt-2">
        {gameState !== "FINISHED"
          ? onDisplaySkart(opponentSkartLength)
          : onDisplayPublicCards(publicOpponentSkart)}
      </div>
    </div>
  );
}

export default OpponentSkart;
