import type { User } from "../Types";

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
  gameState: string;
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
      {/* Role and bid information */}
      {dealer && (
        <div className="w-full flex flex-col justify-center items-center pb-4">
          <div className="w-full h-1/2 flex flex-col">
            <div className="w-full h-1/4 pl-3 pt-3">
              {(gameState === "NEW" || gameState === "FINISHED") &&
                startPlayer !== null && (
                  <p className="text-xl font-semibold">Dealer:</p>
                )}
              {gameState !== "NEW" &&
                gameState !== "FINISHED" &&
                turnPlayer !== null && (
                  <p className="text-xl font-semibold">Turn:</p>
                )}
            </div>
            <div className="w-full h-3/4 justify-center flex items-center">
              <p
                key={currentName}
                className="text-5xl font-bold animate-jump-in"
              >
                {currentName}
              </p>
            </div>
          </div>
          <div className="w-full h-1/2 flex">
            <div className="w-2/3 h-full">
              <div className="w-full h-1/4 flex items-center pl-3">
                {gameState !== "NEW" &&
                  gameState !== "FINISHED" &&
                  bid !== "-" && (
                    <p className="text-xl font-semibold">Declarer:</p>
                  )}
              </div>
              {gameState !== "NEW" && gameState !== "FINISHED" && (
                <div className="w-full h-3/4 justify-center flex items-center">
                  <p
                    className="animate-jump-in text-5xl font-bold"
                    key={declarer}
                  >
                    {declarer}
                  </p>
                </div>
              )}
            </div>

            <div className="w-1/3 h-full flex flex-col">
              {gameState !== "NEW" &&
                gameState !== "FINISHED" &&
                bid !== "-" && (
                  <>
                    <div className="w-full h-1/4 justify-center flex items-center">
                      <p className="text-xl font-semibold">Bid:</p>
                    </div>
                    <div className="w-full h-3/4 justify-center flex items-center">
                      <p
                        className="animate-jump-in text-4xl font-bold"
                        key={bid}
                      >
                        {bid}
                      </p>
                    </div>
                  </>
                )}
            </div>
          </div>
        </div>
      )}

      {/* Public information */}
      {publicInfo && (
        <div className="w-full text-3xl px-4 py-6 font-bold flex justify-center items-center text-center">
          <p className="animate-jump-in" key={publicInfo}>
            {displayInformation(publicInfo)}
          </p>
        </div>
      )}

      {/* Private information */}
      {privateInfo && (
        <div
          className="w-full px-4 py-6 font-bold animate-jump-in text-center flex justify-center items-center"
          key={privateInfo}
        >
          {gameState === "FINISHED" ? (
            <p className="text-2xl">{displayInformation(privateInfo)}</p>
          ) : (
            <p className="text-3xl">{displayInformation(privateInfo)}</p>
          )}
        </div>
      )}

      {/* Bonuses information */}
      {declarerBonuses && (
        <div className="w-full text-2xl px-4 py-6 font-bold text-center flex flex-col">
          <div className="w-full text-2xl flex flex-col justify-center items-center">
            {declarerBonuses && (
              <p className="font-bold">Declarer's bonuses:</p>
            )}
            <p className="animate-jump-in text-xl" key={declarerBonuses}>
              {declarerBonuses}
            </p>
          </div>
          {opponentBonuses && (
            <div className="w-full text-2xl flex flex-col justify-center items-center">
              {opponentBonuses && (
                <p className="font-bold">Opponent's bonuses:</p>
              )}
              <p className="animate-jump-in text-xl" key={opponentBonuses}>
                {opponentBonuses}
              </p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default InfoTable;
