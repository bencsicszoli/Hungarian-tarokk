import type { JSX } from "react";

function TrickPlace({
  onRenderTrickCards,
}: {
  onRenderTrickCards: () => JSX.Element;
}) {
  return (
    <div className="w-[30%]">
      <div className="h-full flex flex-col justify-center items-center">
        <div className="relative w-full h-full">{onRenderTrickCards()}</div>
      </div>
    </div>
  );
}

export default TrickPlace;