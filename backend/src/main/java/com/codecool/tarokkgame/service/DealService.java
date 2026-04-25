package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.model.dto.messagedto.response.PlayerCardDTO;
import com.codecool.tarokkgame.model.entity.*;
import com.codecool.tarokkgame.repository.DeckCardRepository;
import com.codecool.tarokkgame.repository.PlayerCardRepository;
import com.codecool.tarokkgame.repository.TalonCardRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.*;

@Service
public class DealService {
    private final DeckCardRepository deckRepository;
    private final TalonCardRepository talonRepository;
    private final PlayerCardRepository playerCardRepository;

    public DealService(DeckCardRepository deckRepository, TalonCardRepository talonRepository, PlayerCardRepository playerCardRepository) {
        this.deckRepository = deckRepository;
        this.talonRepository = talonRepository;
        this.playerCardRepository = playerCardRepository;
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
        List<PlayerCard> playerCards = new ArrayList<>();
        return createPlayerCardsFromDeck(player, from, to, playerCards);
    }

    public List<PlayerCardDTO> getAllPlayerCards(Player player, int from, int to) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        List<PlayerCard> playerCards = playerCardRepository.findAllByPlayerId(player.getId());
        List<PlayerCardDTO> playerCardDTOs = createPlayerCardsFromDeck(player, from, to, playerCards);
        return playerCardDTOs.stream().sorted(Comparator.comparingInt(PlayerCardDTO::cardId)).toList();
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
        for (PlayerCard playerCard : playerCards) {
            PlayerCardDTO dto = new PlayerCardDTO(
                    playerCard.getCard().getId(),
                    playerCard.getPlayer().getId(),
                    playerCard.getCard().getFrontImagePath(),
                    playerCard.isClickable());
            playerCardDTOs.add(dto);
        }
        playerCardRepository.saveAll(playerCards);
        return playerCardDTOs;
    }
}
