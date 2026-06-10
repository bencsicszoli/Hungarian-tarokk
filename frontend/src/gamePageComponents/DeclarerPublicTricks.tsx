import type { JSX } from "react";
import type { CardImage } from "../types.ts";

function DeclarerPublicTricks({
  publicDeclarerTricks,
  onDisplayPublicCards,
}: {
  publicDeclarerTricks: CardImage[];
  onDisplayPublicCards: (cards: CardImage[]) => JSX.Element;
}) {
  return (
    <div className="w-full flex flex-col flex-auto justify-center items-center">
      <p className="text-center text-xl font-bold text-green-100 mb-4">
        Tricks of declarer side:
      </p>
      {onDisplayPublicCards(publicDeclarerTricks)}
    </div>
  );
}

export default DeclarerPublicTricks;
