import { useTranslation } from "react-i18next";

function OpeningImage({ isGameNew }: { isGameNew: boolean }) {
    const { t } = useTranslation();
    if (isGameNew) {
        return (
            <div className="w-full h-full flex justify-center items-center">
                <img src="deck.png" alt={t("game.alt.deck")} className="h-full" />
            </div>
        );
    }
    return null;
}

export default OpeningImage;