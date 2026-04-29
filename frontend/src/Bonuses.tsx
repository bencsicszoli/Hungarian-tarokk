import type { JSX } from "react";

function Bonuses({
  hasEightTarokks,
  hasNineTarokks,
  declarer,
  callableTarokks,
  onRenderTarokkNumberButton,
  onRenderCallableTarokkButtons,
  onRenderBonusButtons,
}: {
  hasEightTarokks: boolean;
  hasNineTarokks: boolean;
  declarer: string | null;
  callableTarokks: string[];
  onRenderTarokkNumberButton: () => JSX.Element | null;
  onRenderCallableTarokkButtons: () => JSX.Element[];
  onRenderBonusButtons: () => JSX.Element[];
}) {
  return (
    <div className="h-full flex flex-col justify-center items-center">
      {hasEightTarokks || hasNineTarokks ? (
        <div className="w-full bg-pink-300 flex justify-center items-center">
          {onRenderTarokkNumberButton()}
        </div>
      ) : null}
      {declarer && callableTarokks.length > 0 && (
        <div className="w-full bg-pink-400 flex justify-center items-center">
          {onRenderCallableTarokkButtons()}
        </div>
      )}

      <div className="w-full bg-pink-500 flex flex-col justify-center items-center">
        <p className="h-1/4 font-semibold mt-1 text-lg">Select your bonuses:</p>
        <div className="h-3/4 w-full grid grid-flow-row grid-cols-4">
          {onRenderBonusButtons()}
        </div>
      </div>
    </div>
  );
}

export default Bonuses;
