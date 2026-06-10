import type { JSX } from "react";
import type { CardImage, GameState } from "../types";

function OwnHand({
  gameState,
  renderOwnHand,
  displayPublicCards,
  publicOpponentTricks,
}: {
  gameState: GameState;
  renderOwnHand: () => JSX.Element;
  displayPublicCards: (cards: CardImage[]) => JSX.Element;
  publicOpponentTricks: CardImage[];
}): JSX.Element {
  return (
    <div
      className={`w-full ${gameState !== "FINISHED" ? "h-1/3" : "flex flex-col flex-auto justify-center items-center"}`}
    >
      {gameState !== "FINISHED" ? (
        renderOwnHand()
      ) : (
        <>
          <p className="text-center text-xl font-bold text-green-100 mb-4">
            Tricks of opponent side:
          </p>
          {displayPublicCards(publicOpponentTricks)}
        </>
      )}
    </div>
  );
}

export default OwnHand;
