import type { User,  } from "../Types";
import type { Card } from "./types";

function GameTable({
  onHandleLogout,
  user,
  dealer,
  gameState,
  dealButtonClicked,
  onHandleDeal,
  declarerSkartLength,
  declarerSkart,
}: {
  onHandleLogout: () => void;
  user: User;
  dealer: string | null;
  gameState: string;
  dealButtonClicked: boolean;
  onHandleDeal: () => void;
  declarerSkartLength: number;
  declarerSkart: Card[];
}) {
  return (
    <div className="w-3/4 h-207.5 bg-poker-table rounded-[70px] shadow-2xl text-white px-6 sm:px-8 flex flex-col items-center justify-center">
      {/* Menu buttons */}
      <div className="w-full h-1/12 flex justify-around items-center">
        <button
          className="border-black border-2 w-1/8 h-2/3 bg-green-300 text-[#2f4b3a] text-2xl font-bold rounded-lg"
          onClick={onHandleLogout}
        >
          Logout
        </button>
        <button className="border-black border-2 w-1/8 h-2/3 bg-green-300 text-[#2f4b3a] text-2xl font-bold rounded-lg">
          Back
        </button>
        {user?.username === dealer &&
          (gameState === "NEW" || gameState === "FINISHED") &&
          !dealButtonClicked && (
            <button
              className="border-black border-2 w-1/8 h-2/3 bg-green-300 text-[#2f4b3a] text-2xl font-bold rounded-lg"
              onClick={onHandleDeal}
            >
              DEAL
            </button>
          )}
      </div>

      {/* Game area */}
      <div className="w-full h-11/12 flex flex-col">
        {/* Top row */}
        <div className="w-full h-1/3 flex">
          {/* Declarer's skart area */}
          <div className="w-1/4">
            <div className="h-1/6 flex justify-center items-end font-bold text-xl">
              {declarerSkartLength > 0 && <p>Declarer's skart</p>}
            </div>
            <div className="flex justify-center items-start h-5/6 mt-2">
              {declarerSkart.length > 0
                ? displayDeclarerSkartWithTarokk()
                : displaySkart(declarerSkartLength)}
            </div>
          </div>

          {/* Player 3's area */}
          {gameState !== "TRICK_PHASE" && gameState !== "FINISHED" ? (
            <div className="w-1/2">
              {renderPlayerHand(getSeatAttributes(seatModifier.current)[2])}
            </div>
          ) : (
            <div className="w-1/2">
              {renderNorthernPlayerHandInTrickPhase(
                getSeatAttributes(seatModifier.current)[2],
              )}
            </div>
          )}

          {/* Opponent's skart area */}
          <div className="w-1/4">
            <div className="h-1/6 flex justify-center items-center font-bold text-xl">
              {opponentSkartLength > 0 && <p>Opponent's skart</p>}
            </div>
            <div className="flex justify-center items-start h-5/6 mt-2">
              {displaySkart(opponentSkartLength)}
            </div>
          </div>
        </div>

        {/* Middle row */}
        <div className="w-full h-1/3 flex">
          {/* Player 4's area */}
          {gameState !== "TRICK_PHASE" && gameState !== "FINISHED" ? (
            <div className="w-1/4">
              {renderPlayerHand(getSeatAttributes(seatModifier.current)[3])}
            </div>
          ) : (
            <div className="w-[35%]">
              {renderPlayerHandInTrickPhase(
                getSeatAttributes(seatModifier.current)[3],
              )}
            </div>
          )}

          {/* Talon and play area */}
          {gameState !== "TRICK_PHASE" && gameState !== "FINISHED" ? (
            <div className="w-1/2">
              {talonCardsNumber > 0 && (
                <Talon talonCardsNumber={talonCardsNumber} />
              )}
              {gameState === "SKART_LAY_DOWN" &&
                discardInformation === null &&
                (ownCards.length > 9 || temporarySelectedCards.length > 0) && (
                  <Skart
                    temporarySelectedCards={temporarySelectedCards}
                    onDisplayTemporarySelectedCards={() => (
                      <>{displayTemporarySelectedCards()}</>
                    )}
                    cardsToDiscard={cardsToDiscard}
                    sendSkartCards={sendSkartCards}
                  />
                )}
              {(gameState === "SKART_LAY_DOWN" ||
                gameState === "BONUS_ANNOUNCEMENT") &&
                discardInformation !== null && (
                  <>
                    <div className="w-full h-2/3 flex items-center justify-center">
                      <p className="text-center text-3xl font-bold">
                        {discardInformation}
                      </p>
                    </div>
                    <div className="w-full h-1/3 flex items-center justify-around">
                      <button
                        className="w-40 h-15 text-2xl bg-green-500 hover:bg-green-700 cursor-pointer text-white font-bold py-2 px-4 rounded"
                        onClick={handleDiscardHand}
                      >
                        Yes
                      </button>
                      <button
                        className="w-40 h-15 text-2xl bg-red-500 hover:bg-red-700 cursor-pointer text-white font-bold py-2 px-4 rounded"
                        onClick={() => setDiscardInformation(null)}
                      >
                        No
                      </button>
                    </div>
                  </>
                )}
              {gameState === "NEW" && publicHand.length > 0 && (
                <PublicHand publicHand={publicHand} />
              )}
              {gameState === "BONUS_ANNOUNCEMENT" &&
                discardInformation === null &&
                turnPlayer === user?.username && (
                  <Bonuses
                    hasEightTarokks={hasEightTarokks}
                    hasNineTarokks={hasNineTarokks}
                    declarer={declarer}
                    callableTarokks={callableTarokks}
                    onRenderTarokkNumberButton={() =>
                      renderTarokkNumberButton()
                    }
                    onRenderCallableTarokkButtons={() =>
                      renderCallableTarokkButtons()
                    }
                    onRenderBonusButtons={() => renderBonusButtons()}
                  />
                )}
            </div>
          ) : (
            <div className="w-[30%]">
              <div className="h-full flex flex-col justify-center items-center">
                <div className="relative w-full h-full">
                  {renderTrickCards()}
                </div>
              </div>
            </div>
          )}

          {/* Player 2's area */}
          {gameState !== "TRICK_PHASE" && gameState !== "FINISHED" ? (
            <div className="w-1/4">
              {renderPlayerHand(getSeatAttributes(seatModifier.current)[1])}
            </div>
          ) : (
            <div className="w-[35%]">
              {renderEasternPlayerHandInTrickPhase(
                getSeatAttributes(seatModifier.current)[1],
              )}
            </div>
          )}
        </div>

        {/* Bottom row */}
        <div className="w-full h-1/3">
          {renderOwnHand(getSeatAttributes(seatModifier.current)[0])}
        </div>
      </div>
    </div>
  );
}

export default GameTable;
