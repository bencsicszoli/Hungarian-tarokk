import { useTranslation } from "react-i18next";
import type { GameState } from "../types";

function DeclarerIndicator({
  declarer,
  gameState,
  bid,
}: {
  declarer: string | null;
  gameState: GameState;
  bid: string;
}) {
  const { t } = useTranslation();
  return (
    <div className="w-2/3 h-full">
      <div className="w-full h-1/4 flex items-center pl-3">
        {gameState !== "NEW" && gameState !== "FINISHED" && bid !== "-" && (
          <p className="text-xl font-semibold">{t("infoTable.declarerLabel")}</p>
        )}
      </div>
      {gameState !== "NEW" && gameState !== "FINISHED" && (
        <div className="w-full h-3/4 flex items-center px-20">
          <p className="animate-jump-in text-5xl font-bold" key={declarer}>
            {declarer}
          </p>
        </div>
      )}
    </div>
  );
}

export default DeclarerIndicator;
