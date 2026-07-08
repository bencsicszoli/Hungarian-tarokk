import type { GameState, InfoLine } from "../types";
import type { JSX } from "react";

function PrivateInfo({
  privateInfo,
  onDisplayInformation,
  gameState,
} : {
  privateInfo: InfoLine[];
  onDisplayInformation: (privateInfo: InfoLine[]) => JSX.Element[];
  gameState: GameState;
}) {
    if (privateInfo.length > 0) {
      return (
        <div
          className="w-full h-auto px-4 py-6 font-bold animate-jump-in text-center flex justify-center items-center"
          key={JSON.stringify(privateInfo)}
        >
          {gameState === "FINISHED" ? (
            <p className="text-2xl">{onDisplayInformation(privateInfo)}</p>
          ) : (
            <p className="text-3xl">{onDisplayInformation(privateInfo)}</p>
          )}
        </div>
      );
    }
    return null;
}

export default PrivateInfo;
