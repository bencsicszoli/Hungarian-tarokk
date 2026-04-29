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
    if (info.indexOf("!") !== info.lastIndexOf("!")) {
      return info
        .split("!")
        .filter(Boolean)
        .map((sentence, index) => (
          <span key={index}>
            {sentence.trim()}
            <br />
          </span>
        ));
    } else {
      return info;
    }
  }

  return (
    <div className="w-100 h-207.5 bg-info-table rounded-[70px] shadow-2xl text-[#2f4b3a] px-6 sm:px-8 flex flex-col">
      <div className="w-full h-1/4 flex flex-col">
        
        <div className="w-full h-1/2 flex flex-col">
          <div className="w-full h-1/4 pl-3">
            {gameState === "NEW" && startPlayer !== null && (
              <p className="text-xl font-semibold">Dealer:</p>
            )}
            {gameState !== "NEW" && turnPlayer !== null && (
              <p className="text-xl font-semibold">Turn:</p>
            )}
          </div>
          <div className="w-full h-3/4 justify-center flex items-center">
            {gameState === "NEW" ? (
              <p className="text-5xl font-bold">{dealer}</p>
            ) : (
              <p className="text-5xl font-bold">{turnPlayer}</p>
            )}
          </div>
        </div>
        <div className="w-full h-1/2 flex">
          <div className="w-2/3 h-full">
            <div className="w-full h-1/4 flex items-center pl-3">
              {gameState !== "NEW" && bid !== "-" && (
                <p className="text-xl font-semibold">Declarer:</p>
              )}
            </div>
            <div className="w-full h-3/4 justify-center flex items-center">
              <p className="text-5xl font-bold">{declarer}</p>
            </div>
          </div>

          <div className="w-1/3 h-full flex flex-col">
            {gameState !== "NEW" && bid !== "-" && (
              <>
                <div className="w-full h-1/4 justify-center flex items-center">
                  <p className="text-xl font-semibold">Bid:</p>
                </div>
                <div className="w-full h-3/4 justify-center flex items-center">
                  <p className="text-4xl font-bold">{bid}</p>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
      <div className="w-full h-1/4 bg-blue-500 items-center justify-center flex">
      
        <p className="text-3xl font-bold text-center">
          {displayInformation(publicInfo)}
        </p>
      </div>
      <div className="w-full h-1/4 bg-blue-300">
        <p className="text-3xl font-bold text-center">
          {displayInformation(privateInfo)}
        </p>
      </div>
      <div className="w-full h-1/4 bg-blue-500 flex flex-col">
        <div className="w-full text-2xl flex flex-col justify-center items-center">
          {declarerBonuses && <p className="font-bold">Declarer's bonuses:</p>}
          <p className="font-normal">{declarerBonuses}</p>
        </div>
        <div className="w-full text-2xl flex flex-col justify-center items-center">
          {opponentBonuses && <p className="font-bold">Opponent's bonuses:</p>}
          <p className="font-normal">{opponentBonuses}</p>
        </div>
      </div>
    </div>
  );
}

export default InfoTable;
