
function Talon({ talonCardsNumber }: { talonCardsNumber: number }) {
    
  function displayTalon(talonCardsNumber: number) {
    const cardsBack = Array(talonCardsNumber).fill("Back.png");
    return cardsBack.map((imagePath, index) => (
      <img
        key={index}
        src={imagePath}
        alt="Talon card back"
        className="w-19 -mx-15"
      />
    ));
  }

  return (
    <div>
      <div className="h-1/6 flex justify-center font-bold text-xl mt-8">
        <p className="border-green-300 border-2 rounded-md text-green-300 px-4 py-1">Talon</p>
      </div>
      <div className="flex justify-center items-center h-2/3 mt-4">
        {displayTalon(talonCardsNumber)}
      </div>
    </div>
  );
}

export default Talon;
