package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.model.dto.messagedto.response.PlayerCardDTO;
import com.codecool.tarokkgame.model.entity.*;
import com.codecool.tarokkgame.repository.DeckCardRepository;
import com.codecool.tarokkgame.repository.PlayerCardRepository;
import com.codecool.tarokkgame.repository.PlayerRepository;
import com.codecool.tarokkgame.repository.TalonCardRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.*;

@Service
public class DealService {
    private final DeckCardRepository deckRepository;
    private final TalonCardRepository talonRepository;

    public DealService(DeckCardRepository deckRepository, TalonCardRepository talonRepository) {
        this.deckRepository = deckRepository;
        this.talonRepository = talonRepository;
    }

    public void setTalonCards(Game game) {
        List<TalonCard> talonCards = new ArrayList<>();
        List<DeckCard> deckCardsForTalon = deckRepository.findCardsByGameIdAndCardOrder(game.getId(), 1, 6);
        for (DeckCard deckCard : deckCardsForTalon) {
            TalonCard talonCard = new TalonCard();
            talonCard.setCard(deckCard.getCard());
            talonCard.setGame(game);
            talonCards.add(talonCard);
        }
        talonRepository.saveAll(talonCards);
    }

    public List<PlayerCardDTO> getPlayerCards(Player player, int from, int to) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        List<PlayerCard> playerCards = player.getPlayerCards();
        return createPlayerCardsFromDeck(player, from, to, playerCards);
    }

    public List<PlayerCardDTO> getAllPlayerCards(Player player, int from, int to) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        List<PlayerCard> playerCards = player.getPlayerCards();

        return createPlayerCardsFromDeck(player, from, to, playerCards);
    }

    private List<PlayerCardDTO> createPlayerCardsFromDeck(Player player, int from, int to, List<PlayerCard> playerCards) {
        List<DeckCard> deckCardsForPlayer = deckRepository.findCardsByGameIdAndCardOrder(player.getGame().getId(), from, to);
        for (DeckCard deckCard : deckCardsForPlayer) {
            if (deckCard.getCard().getStrength() == 1) {
                player.setHasPagat(true);
            }
            PlayerCard playerCard = new PlayerCard();
            playerCard.setCard(deckCard.getCard());
            playerCard.setPlayer(player);
            playerCards.add(playerCard);
        }
        List<PlayerCardDTO> playerCardDTOs = new ArrayList<>();
        //List<PlayerCard> orderedCards = player.getPlayerCards();
        playerCards.sort(Comparator.comparing(pc -> pc.getCard().getId()));
        for (PlayerCard playerCard : playerCards) {
            PlayerCardDTO dto = new PlayerCardDTO(
                    playerCard.getCard().getId(),
                    playerCard.getPlayer().getId(),
                    playerCard.getCard().getFrontImagePath(),
                    playerCard.isClickable());
            playerCardDTOs.add(dto);
        }
        return playerCardDTOs;
    }
}
