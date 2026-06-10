import type { GameState } from "../types";
import type { JSX } from "react";

function PrivateInfo({
  privateInfo,
  onDisplayInformation,
  gameState,
} : {
  privateInfo: string;
  onDisplayInformation: (privateInfo: string) => JSX.Element[];
  gameState: GameState;
}) {
    if (privateInfo) {
      return (
        <div
          className="w-full h-auto px-4 py-6 font-bold animate-jump-in text-center flex justify-center items-center"
          key={privateInfo}
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