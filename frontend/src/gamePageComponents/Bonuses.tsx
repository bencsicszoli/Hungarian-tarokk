import type { JSX } from "react";
import { useTranslation } from "react-i18next";
import type { User, GameState, InfoLine, LocalizedMessage } from "../types";

function Bonuses({
  gameState,
  discardInformation,
  turnPlayer,
  user,
  hasEightTarokks,
  hasNineTarokks,
  declarer,
  callableTarokks,
  onRenderTarokkNumberButton,
  onRenderCallableTarokkButtons,
  onRenderBonusButtons,
}: {
  gameState: GameState;
  discardInformation: InfoLine[] | null;
  turnPlayer: string | null;
  user: User | null;
  hasEightTarokks: boolean;
  hasNineTarokks: boolean;
  declarer: string | null;
  callableTarokks: LocalizedMessage[];
  onRenderTarokkNumberButton: () => JSX.Element | null;
  onRenderCallableTarokkButtons: () => JSX.Element[];
  onRenderBonusButtons: () => JSX.Element[];
}) {
  const { t } = useTranslation();
  if (
    gameState === "BONUS_ANNOUNCEMENT" &&
    discardInformation === null &&
    turnPlayer === user?.username
  ) {
    return (
      <div className="h-full flex flex-col justify-center items-center text-green-200">
        {hasEightTarokks || hasNineTarokks ? (
          <div className="w-full flex justify-center items-center mb-1">
            {onRenderTarokkNumberButton()}
          </div>
        ) : null}
        {declarer && callableTarokks.length > 0 && (
          <div className="w-full flex justify-center items-center">
            {onRenderCallableTarokkButtons()}
          </div>
        )}

        <div className="w-full flex flex-col justify-center items-center">
          <p className="h-1/4 font-semibold mt-1 mb-1 text-lg">
            {t("game.selectBonusesTitle")}
          </p>
          <div className="h-3/4 w-full grid grid-flow-row grid-cols-4">
            {onRenderBonusButtons()}
          </div>
        </div>
      </div>
    );
  }
}

export default Bonuses;
