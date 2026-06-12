import BonusLists from "./infoTableComponents/BonusLists";
import DealerTurnAndBidInfo from "./infoTableComponents/DealerTurnAndBidInfo";
import PrivateInfo from "./infoTableComponents/PrivateInfo";
import PublicInfo from "./infoTableComponents/PublicInfo";
import type { GameState } from "./types";

function InfoTable({
  bid,
  declarer,
  dealer,
  turnPlayer,
  publicInfo,
  privateInfo,
  gameState,
  declarerBonuses,
  opponentBonuses,
  startPlayer,
}: {
  bid: string;
  declarer: string | null;
  dealer: string | null;
  turnPlayer: string | null;
  publicInfo: string;
  privateInfo: string;
  gameState: GameState;
  declarerBonuses: string | null;
  opponentBonuses: string | null;
  startPlayer: string | null;
}) {
  function displayInformation(info: string) {
    return info
      .split("@")
      .filter(Boolean)
      .map((sentence, index) => (
        <span key={index}>
          {sentence.trim()}
          <br />
        </span>
      ));
  }

  const currentName =
    gameState === "NEW" || gameState === "FINISHED" ? dealer : turnPlayer;

  
  return (
    <div className="w-100 h-207.5 bg-info-table rounded-[70px] shadow-2xl text-[#2f4b3a] px-6 sm:px-8 flex flex-col justify-center items-center">

      <DealerTurnAndBidInfo
        dealer={dealer}
        turnPlayer={turnPlayer}
        startPlayer={startPlayer}
        gameState={gameState}
        bid={bid}
        declarer={declarer}
        currentName={currentName}
      />

      <PublicInfo
        publicInfo={publicInfo}
        onDisplayInformation={(publicInfo) => displayInformation(publicInfo)}
      />

      <PrivateInfo
        privateInfo={privateInfo}
        onDisplayInformation={(privateInfo) => displayInformation(privateInfo)}
        gameState={gameState}
      />

      <BonusLists
        declarerBonuses={declarerBonuses}
        opponentBonuses={opponentBonuses}
      />
    </div>
  );
}

export default InfoTable;