import type { JSX } from "react";
import type { GameState } from "../types.ts";

function NorthernPlayerhand({
  gameState,
  onRenderPlayerHand,
  onRenderNorthernPlayerHandInTrickPhase,
}: {
  gameState: GameState;
  onRenderPlayerHand: (position: string) => JSX.Element;
  onRenderNorthernPlayerHandInTrickPhase: () => JSX.Element;
}) {
    
  if (gameState !== "TRICK_PHASE" && gameState !== "FINISHED") {
    return <div className="w-1/2">{onRenderPlayerHand("NORTH")}</div>;
  } else if (gameState === "TRICK_PHASE") {
    return (
      <div className="w-1/2">{onRenderNorthernPlayerHandInTrickPhase()}</div>
    );
  }
}

export default NorthernPlayerhand;

