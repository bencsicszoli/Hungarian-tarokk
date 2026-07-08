import { useTranslation } from "react-i18next";
import type { GameState } from "../types";

function BidInfo({
  bid,
  gameState,
}: {
  bid: string | null;
  gameState: GameState;
}) {
  const { t } = useTranslation();
  return (
    <div className="w-1/3 h-full flex flex-col">
      {gameState !== "NEW" && gameState !== "FINISHED" && bid !== "-" && (
        <>
          <div className="w-full h-1/4 justify-center flex items-center">
            <p className="text-xl font-semibold">{t("infoTable.bidLabel")}</p>
          </div>
          <div className="w-full h-3/4 justify-center flex items-center">
            <p className="animate-jump-in text-4xl font-bold" key={bid}>
              {bid}
            </p>
          </div>
        </>
      )}
    </div>
  );
}

export default BidInfo;
