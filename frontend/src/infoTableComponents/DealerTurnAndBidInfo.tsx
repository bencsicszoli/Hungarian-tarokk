import type { GameState } from "../types";
import BidInfo from "./BidInfo";
import DeclarerIndicator from "./DeclarerIndicator";
import NextPlayerIndicator from "./NextPlayerIndicator";

function DealerTurnAndBidInfo({
  dealer,
  turnPlayer,
  startPlayer,
  gameState,
  bid,
  declarer,
  currentName,
}: {
  dealer: string | null;
  turnPlayer: string | null;
  startPlayer: string | null;
  gameState: GameState;
  bid: string;
  declarer: string | null;
  currentName: string | null;
}) {
  if (dealer) {
    return (
      <div className="w-full h-40 flex flex-col justify-center items-center pb-4 gap-2">
        <div className="w-full h-1/2 flex flex-col">
        <NextPlayerIndicator
          gameState={gameState}
          turnPlayer={turnPlayer}
          startPlayer={startPlayer}
          currentName={currentName}
        />
        </div>

        <div className="w-full h-1/2 flex">
          <DeclarerIndicator
            declarer={declarer}
            gameState={gameState}
            bid={bid}
          />

          <BidInfo bid={bid} gameState={gameState} />
        </div>
      </div>
    );
  } else {
    return null;
  }
}

export default DealerTurnAndBidInfo;
