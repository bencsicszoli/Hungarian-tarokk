import type { JSX } from "react";

interface Card {
  cardId: number;
  imagePath: string;
  clickable: boolean;
}

function Skart({
  temporarySelectedCards,
  onDisplayTemporarySelectedCards,
  cardsToDiscard,
  sendSkartCards,
}: {
  temporarySelectedCards: Card[];
  onDisplayTemporarySelectedCards: () => JSX.Element;
  cardsToDiscard: React.RefObject<number>;
  sendSkartCards: () => void;
}) {
  return (
    <>
      <div className="h-1/6 flex justify-center items-end font-bold text-xl">
        <p>Skart</p>
      </div>
      <div className="flex justify-center items-center h-2/3">
        {onDisplayTemporarySelectedCards()}
      </div>
      <div className="h-1/6 flex justify-center items-center font-bold text-xl">
        {cardsToDiscard.current === temporarySelectedCards.length && (
          <button
            className="border-black border-2 w-30 h-10 hover:scale-105 hover:bg-green-400 cursor-pointer bg-green-300 text-[#2f4b3a] rounded-md font-semibold mb-2"
            onClick={sendSkartCards}
          >
            Submit
          </button>
        )}
      </div>
    </>
  );
}

export default Skart;
