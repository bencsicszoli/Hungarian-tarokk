import type { GameState } from "../types";

function NextPlayerIndicator({
  gameState,
  turnPlayer,
  startPlayer,
  currentName,
}: {
  turnPlayer: string | null;
  startPlayer: string | null;
  currentName: string | null;
  gameState: GameState;
}) {
  return (
    <>
      <div className="w-full h-auto pl-3 flex items-center">
        {(gameState === "NEW" || gameState === "FINISHED") &&
          startPlayer !== null && (
            <p className="text-xl font-semibold ">Dealer:</p>
          )}
        {gameState !== "NEW" &&
          gameState !== "FINISHED" &&
          turnPlayer !== null && <p className="text-xl font-semibold">Turn:</p>}
      </div>
      <div className="w-full h-auto flex items-center px-20">
        <p key={currentName} className="text-5xl font-bold animate-jump-in">
          {currentName}
        </p>
      </div>
    </>
  );
}

export default NextPlayerIndicator;
