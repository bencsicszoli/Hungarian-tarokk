package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.model.entity.Card;
import com.codecool.tarokkgame.model.entity.DeckCard;
import com.codecool.tarokkgame.model.entity.Game;
import com.codecool.tarokkgame.repository.CardRepository;
import com.codecool.tarokkgame.repository.DeckCardRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ShuffleService {

    public final Random random;
    public final CardRepository cardRepository;
    public final DeckCardRepository deckCardRepository;

    public ShuffleService(Random random, CardRepository cardRepository, DeckCardRepository deckCardRepository) {
        this.random = random;
        this.cardRepository = cardRepository;
        this.deckCardRepository = deckCardRepository;
    }

    public void addShuffledDeck(Game game) {
        deckCardRepository.deleteAllByGameId(game.getId());
        List<Card> cards = cardRepository.findAll();
        List<DeckCard> deckCards = new ArrayList<>();
        int cardOrder = 1;
        List<Integer> indexes = getShuffledCardIndexes();
        for (Integer index : indexes) {
            Card card = cards.get(index - 1);
            DeckCard deckCard = new DeckCard();
            deckCard.setCard(card);
            deckCard.setCardOrder(cardOrder);
            deckCard.setGame(game);
            deckCards.add(deckCard);
            cardOrder++;
        }
        deckCardRepository.saveAll(deckCards);
    }

    private List<Integer> getShuffledCardIndexes() {
        List<Integer> cardIndexes = IntStream.rangeClosed(1, 42)
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(cardIndexes, random);
        return cardIndexes;
    }
}
