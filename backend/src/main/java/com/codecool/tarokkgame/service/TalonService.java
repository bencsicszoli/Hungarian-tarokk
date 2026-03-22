package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.model.dto.messagedto.PlayerCardDTO;
import com.codecool.tarokkgame.model.dto.messagedto.PlayerCardListDTO;
import com.codecool.tarokkgame.model.entity.Game;
import com.codecool.tarokkgame.model.entity.Player;
import com.codecool.tarokkgame.model.entity.PlayerCard;
import com.codecool.tarokkgame.model.entity.TalonCard;
import com.codecool.tarokkgame.repository.PlayerCardRepository;
import com.codecool.tarokkgame.repository.TalonCardRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TalonService {
    private final TalonCardRepository talonCardRepository;
    private final PlayerCardRepository playerCardRepository;

    public TalonService(TalonCardRepository talonCardRepository, PlayerCardRepository playerCardRepository) {
        this.talonCardRepository = talonCardRepository;
        this.playerCardRepository = playerCardRepository;
    }

    //@Transactional
    public PlayerCardListDTO allocateTalonCards(Game game, Player player, long idFrom, long idTo) {
        List<PlayerCardDTO> dtos = new ArrayList<>();
        if (idFrom <= idTo) {
            List<TalonCard> talonCards = talonCardRepository.findAllByIdRangeAndGameId(idFrom, idTo, game.getId());
            List<PlayerCard> cardsToHand = new ArrayList<>();
            for (TalonCard talonCard : talonCards) {
                PlayerCard playerCard = new PlayerCard();
                playerCard.setCard(talonCard.getCard());
                playerCard.setPlayer(player);
                playerCard.setClickable(false);
                cardsToHand.add(playerCard);
            }
            playerCardRepository.saveAll(cardsToHand);
        }

        List<PlayerCard> playerCards = playerCardRepository.findAllByPlayerId(player.getId());

        for (PlayerCard playerCard : playerCards) {
            boolean clickable = true;
            if (playerCard.getCard().getStrength() == -1 ||
                playerCard.getCard().getStrength() == 1 ||
                playerCard.getCard().getStrength() == 20 ||
                playerCard.getCard().getStrength() == 21 ||
                playerCard.getCard().getStrength() == 22) {
                clickable = false;
            } else if (player.getName().equals(game.getDeclarer()) && player.isAcceptedXIX_Invit() && playerCard.getCard().getStrength() == 19) {
                clickable = false;
            } else if (player.getName().equals(game.getDeclarer()) && player.isAcceptedXVIII_Invit() && playerCard.getCard().getStrength() == 18) {
                clickable = false;
            }
            PlayerCardDTO dto = new PlayerCardDTO(playerCard.getCard().getId(), player.getId(), playerCard.getCard().getFrontImagePath(), clickable);
            dtos.add(dto);
        }
        List<PlayerCardDTO> sortedDTO = dtos.stream().sorted(Comparator.comparingInt(PlayerCardDTO::cardId)).toList();
        return new PlayerCardListDTO(sortedDTO, "game.playerCards");
    }
}
