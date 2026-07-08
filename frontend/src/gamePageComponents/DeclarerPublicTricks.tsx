import type { JSX } from "react";
import { useTranslation } from "react-i18next";
import type { CardImage } from "../types.ts";

function DeclarerPublicTricks({
  publicDeclarerTricks,
  onDisplayPublicCards,
}: {
  publicDeclarerTricks: CardImage[];
  onDisplayPublicCards: (cards: CardImage[]) => JSX.Element;
}) {
  const { t } = useTranslation();
  return (
    <div className="w-full flex flex-col flex-auto justify-center items-center">
      <p className="text-center text-xl font-bold text-green-100 mb-4">
        {t("game.declarerTricksTitle")}
      </p>
      {onDisplayPublicCards(publicDeclarerTricks)}
    </div>
  );
}

export default DeclarerPublicTricks;
