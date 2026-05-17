import type { Card } from "./Types";

function PublicHand({ publicHand }: { publicHand: Card[] }) {
    
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

  return (
    <>
      <div className="h-1/6 flex justify-center items-end font-bold text-xl">
        <p>Discarded hand:</p>
      </div>
      <div className="flex justify-center items-center h-5/6">
        {displayPublicHand()}
      </div>
    </>
  );
}

export default PublicHand;
