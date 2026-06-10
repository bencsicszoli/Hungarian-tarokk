import type { GameState } from "../types.ts";

function DisposableHandInfo({
  gameState,
  discardInformation,
  onFormatDiscardInformation,
  onHandleDiscardHand,
  onSetDiscardInformation,
} : {
  gameState: GameState;
  discardInformation: string | null;
  onFormatDiscardInformation: () => React.JSX.Element[] | null;
  onHandleDiscardHand: () => void;
  onSetDiscardInformation: (info: string | null) => void;
}) {
    
  if (                           // Why do we need BONUS_ANNOUNCEMENT?
    (gameState === "SKART_LAY_DOWN" || gameState === "BONUS_ANNOUNCEMENT") &&
    discardInformation !== null
  ) {
    return (
      <div className="w-full h-[95%] border-black border-2 bg-green-300 text-[#2f4b3a] rounded-xl">
        <div className="w-full h-1/2">
          <p className="text-center text-3xl font-bold pt-4 px-2">
            {onFormatDiscardInformation()}
          </p>
        </div>
        <div className="w-full h-1/2 flex items-center justify-around">
          <button
            className="w-40 h-15 text-2xl bg-[#2f4b3a] hover:bg-green-700 cursor-pointer text-green-300 font-bold py-2 px-4 rounded-lg"
            onClick={onHandleDiscardHand}
          >
            Yes
          </button>
          <button
            className="w-40 h-15 text-2xl bg-red-600 hover:bg-red-700 cursor-pointer text-red-100 font-bold py-2 px-4 rounded-lg"
            onClick={() => onSetDiscardInformation(null)}
          >
            No
          </button>
        </div>
      </div>
    );
  }
  return null;
}

export default DisposableHandInfo;
