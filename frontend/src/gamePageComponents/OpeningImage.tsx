function OpeningImage({ isGameNew }: { isGameNew: boolean }) {
    if (isGameNew) {
        return (
            <div className="w-full h-full flex justify-center items-center">
                <img src="deck.png" alt="Deck" className="h-full" />
            </div>
        );
    }
    return null;
}

export default OpeningImage;