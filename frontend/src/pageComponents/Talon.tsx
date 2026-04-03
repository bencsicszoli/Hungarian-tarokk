
function Talon({ talonCardsNumber }: { talonCardsNumber: number }) {
    
  function displayTalon(talonCardsNumber: number) {
    const cardsBack = Array(talonCardsNumber).fill("Back.png");
    return cardsBack.map((imagePath, index) => (
      <img
        key={index}
        src={imagePath}
        alt="Talon card back"
        className="w-20 -mx-14"
      />
    ));
  }

  return (
    <>
      <div className="h-1/6 flex justify-center items-end font-bold text-xl">
        <p>Talon</p>
      </div>
      <div className="flex justify-center items-center h-5/6">
        {displayTalon(talonCardsNumber)}
      </div>
    </>
  );
}

export default Talon;
