import type { JSX } from "react";
import type { GameState } from "../types";

function EasternPlayerHand({
  gameState,
  onRenderPlayerHand,
  onRenderEasternPlayerHandInTrickPhase,
}: {
  gameState: GameState;
  onRenderPlayerHand: (position: string) => JSX.Element;
  onRenderEasternPlayerHandInTrickPhase: () => JSX.Element;
}) {
  if (gameState !== "TRICK_PHASE") {
    return <div className="w-1/4">{onRenderPlayerHand("EAST")}</div>;
  } else {
    return (
      <div className="w-[35%]">{onRenderEasternPlayerHandInTrickPhase()}</div>
    );
  }
}

export default EasternPlayerHand;
