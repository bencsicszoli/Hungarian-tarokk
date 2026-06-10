function BonusLists({
  declarerBonuses,
  opponentBonuses,
}: {
  declarerBonuses: string | null;
  opponentBonuses: string | null;
}) {
  if (declarerBonuses) {
    return (
      <div className="w-full h-auto text-2xl px-4 py-6 font-bold text-center flex flex-col">
        <div className="w-full text-2xl flex flex-col justify-center items-center">
          {declarerBonuses && <p className="font-bold">Declarer's bonuses:</p>}
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
    );
  }
}

export default BonusLists;
