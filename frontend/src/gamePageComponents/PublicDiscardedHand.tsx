import type { Card, GameState } from "../types";

function PublicHand({
  publicHand,
  gameState,
}: {
  publicHand: Card[];
  gameState: GameState;
}) {
  function displayPublicHand() {
    return publicHand.map((card, index) => (
      <img
        key={index}
        src={card.imagePath}
        alt={`Public card ${index + 1}`}
        className="w-20 -mx-3"
      />
    ));
  }

  if (gameState === "NEW" && publicHand.length > 0) {
    return (
      <>
        <div className="h-1/4 flex justify-center items-end font-bold text-xl pb-2">
          <p className="bg-green-300 rounded-lg p-2 text-[#2f4b3a]">Discarded hand:</p>
        </div>
        <div className="flex justify-center items-start h-3/4">
          {displayPublicHand()}
        </div>
      </>
    );
  }
}

export default PublicHand;
