import type { JSX } from "react";
import type { CardImage } from "../Types";

function OwnHand({
  gameState,
  onRenderOwnHand,
  onDisplayPublicCards,
  publicOpponentTricks,
}: {
  gameState: string;
  onRenderOwnHand: () => JSX.Element;
  onDisplayPublicCards: (cards: CardImage[]) => JSX.Element;
  publicOpponentTricks: CardImage[];
}) {
  return (
    <>
      {gameState !== "FINISHED" ? (
        onRenderOwnHand()
      ) : (
        <div className="w-full flex flex-col flex-auto justify-center items-center">
          <p className="text-center text-xl font-bold text-green-100 mb-4">
            Tricks of opponent side:
          </p>
          {onDisplayPublicCards(publicOpponentTricks)}
        </div>
      )}
    </>
  );
}

export default OwnHand;
