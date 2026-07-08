import { useTranslation } from "react-i18next";
import type { User } from "../types";

function MenuButtons({
  onHandleLogout,
  onHandleDeal,
  dealButtonClicked,
  gameState,
  user,
  dealer,
}: {
  onHandleLogout: () => void;
  onHandleDeal: () => void;
  dealButtonClicked: boolean;
  gameState:
    | "NEW"
    | "IN_PROGRESS"
    | "BIDDING"
    | "TALON_PICK_UP"
    | "SKART_LAY_DOWN"
    | "BONUS_ANNOUNCEMENT"
    | "TRICK_PHASE"
    | "FINISHED";
  user: User | null;
  dealer: string | null;
}) {
  const { t } = useTranslation();
  return (
    <div className="w-full h-1/12 flex justify-around items-center">
      <button
        className="border-black border-2 w-1/8 h-2/3 bg-green-300 hover:scale-105 hover:bg-green-400 cursor-pointer text-[#2f4b3a] text-2xl font-bold rounded-lg"
        onClick={onHandleLogout}
      >
        {t("game.logout")}
      </button>
      <button className="border-black border-2 w-1/8 h-2/3 bg-green-300 hover:scale-105 hover:bg-green-400 cursor-pointer text-[#2f4b3a] text-2xl font-bold rounded-lg">
        {t("game.back")}
      </button>
      {user?.username === dealer &&
        (gameState === "NEW" || gameState === "FINISHED") &&
        !dealButtonClicked && (
          <button
            className="border-black border-2 w-1/8 h-2/3 bg-green-300 hover:scale-105 hover:bg-green-400 cursor-pointer text-[#2f4b3a] text-2xl font-bold rounded-lg"
            onClick={onHandleDeal}
          >
            {t("game.deal")}
          </button>
        )}
    </div>
  );
}

export default MenuButtons;
