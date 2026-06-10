import type { JSX } from "react";
import type { GameState } from "../types";

function WesternPlayerHand({
  gameState,
  onRenderPlayerHand,
  onRenderWesternPlayerHandInTrickPhase,
}: {
  gameState: GameState;
  onRenderPlayerHand: (position: string) => JSX.Element;
  onRenderWesternPlayerHandInTrickPhase: () => JSX.Element;
}) {
  if (gameState !== "TRICK_PHASE") {
    return <div className="w-1/4">{onRenderPlayerHand("WEST")}</div>;
  } else {
    return (
      <div className="w-[35%]">
        {onRenderWesternPlayerHandInTrickPhase()}
      </div>
    );
  }
}

export default WesternPlayerHand;