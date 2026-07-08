import type { JSX } from "react";
import { useTranslation } from "react-i18next";
import type { Card, CardImage, GameState } from "../types";

function DeclarerSkart({
  declarerSkart,
  declarerSkartLength,
  gameState,
  publicDeclarerSkart,
  onDisplayDeclarerSkartWithTarokk,
  onDisplaySkart,
  onDisplayPublicCards,
}: {
  declarerSkart: Card[];
  declarerSkartLength: number;
  gameState: GameState;
  publicDeclarerSkart: CardImage[];
  onDisplayDeclarerSkartWithTarokk: () => JSX.Element[];
  onDisplaySkart: (length: number) => JSX.Element[];
  onDisplayPublicCards: (cards: CardImage[]) => JSX.Element;
}) {
  const { t } = useTranslation();
  return (
    <div className="w-1/4 grow">
      <div className="h-1/6 flex justify-center items-end font-bold text-xl text-green-100">
        {declarerSkartLength > 0 && <p>{t("game.declarerSkart")}</p>}
      </div>
      <div className="flex justify-center items-start h-5/6 mt-2">
        {declarerSkart.length > 0 &&
          gameState !== "FINISHED" &&
          onDisplayDeclarerSkartWithTarokk()}
        {declarerSkart.length === 0 &&
          gameState !== "FINISHED" &&
          onDisplaySkart(declarerSkartLength)}
        {gameState === "FINISHED" && onDisplayPublicCards(publicDeclarerSkart)}
      </div>
    </div>
  );
}

export default DeclarerSkart;
