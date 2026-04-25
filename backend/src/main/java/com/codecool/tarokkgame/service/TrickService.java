package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.constants.RoleInGame;
import com.codecool.tarokkgame.model.dto.messagedto.response.PlayerCardDTO;
import com.codecool.tarokkgame.model.dto.messagedto.response.PlayerCardListDTO;
import com.codecool.tarokkgame.model.dto.messagedto.response.TrickCardDTO;
import com.codecool.tarokkgame.model.dto.messagedto.response.TrickCardListDTO;
import com.codecool.tarokkgame.model.entity.*;
import com.codecool.tarokkgame.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TrickService {
    private final GameRepository gameRepository;
    private final PlayerCardRepository playerCardRepository;
    private final MapperService mapperService;
    private final CardRepository cardRepository;
    private final TrickRepository trickRepository;
    private final OwnTrickRepository ownTrickRepository;

    public TrickService(GameRepository gameRepository, PlayerCardRepository playerCardRepository, MapperService mapperService, CardRepository cardRepository, TrickRepository trickRepository, OwnTrickRepository ownTrickRepository) {
        this.gameRepository = gameRepository;
        this.playerCardRepository = playerCardRepository;
        this.mapperService = mapperService;
        this.cardRepository = cardRepository;
        this.trickRepository = trickRepository;
        this.ownTrickRepository = ownTrickRepository;
    }

    public PlayerCardListDTO getFirstPlayerCards(Player player, Game game, int calledTarokk) {
        List<PlayerCard> playerCards = playerCardRepository.findAllByPlayerId(player.getId());  // player.getPlayerCards() ?
        if (player.getRoleInGame().equals(RoleInGame.DECLARER) || player.hasTheGivenTarokk(calledTarokk)) {
            if (game.isDeclarerAnnouncedUltimo()) {
                player.setAnnouncedUltimo(true);
            }
        } else {
            if (game.isOpponentAnnouncedUltimo()) {
                player.setAnnouncedUltimo(true);
            }
        }
        if (!player.isHasPagat()) {
            for (PlayerCard playerCard : playerCards) {
                playerCard.setClickable(true);
            }
        } else {
            if (player.isAnnouncedUltimo()) {
                if (game.getTrickRound() < 9) {
                    for (PlayerCard playerCard : playerCards) {
                        playerCard.setClickable(playerCard.getCard().getStrength() != 1);
                    }
                } else {
                    for (PlayerCard playerCard : playerCards) {
                        playerCard.setClickable(true);
                    }
                }

            } else {
                for (PlayerCard playerCard : playerCards) {
                    playerCard.setClickable(true);
                }
            }
        }
        List<PlayerCardDTO> playerCardList = mapperService.mapToPlayerCardListDTO(playerCards);
        List<PlayerCardDTO> sortedcardList = playerCardList.stream().sorted(Comparator.comparingInt(PlayerCardDTO::cardId)).toList();
        return new PlayerCardListDTO(sortedcardList, "game.playerCards");
    }

    //Handle calledTarokk in play
    public TrickCardListDTO getTrickCards(Game game, Player player, Card card) {
        playerCardRepository.deleteByPlayerIdAndCardId(player.getId(), card.getId());
        if (card.getStrength() == game.getInvitedTarokk()) {
            player.setRoleInGame(RoleInGame.DECLARER_PARTNER);
            game.markPlayersAsOpponent();
        }
        Trick trick = new Trick();
        trick.setCard(card);
        trick.setPlayer(player);
        List<Trick> tricks = game.getTricks();
        tricks.add(trick);
        trickRepository.saveAll(tricks);  // ?
        List<TrickCardDTO> trickCardDTOList = new ArrayList<>();
        for (Trick trickCard : tricks) {
            TrickCardDTO trickCardDTO = new TrickCardDTO(
                    trickCard.getCard().getId(),
                    trickCard.getPlayer().getId(),
                    trickCard.getCard().getFrontImagePath());
            trickCardDTOList.add(trickCardDTO);
        }
        gameRepository.save(game);
        return new TrickCardListDTO(trickCardDTOList, "game.trickCards");
    }


    public PlayerCardListDTO getTurnPlayerCards(Game game, Player player, Card card) {  //First trick
        Player nextPlayer = game.getNextPlayer(player);
        String suit = card.getSuit();
        List<PlayerCard> playerCards = playerCardRepository.findAllByPlayerId(nextPlayer.getId());
        if (!suit.equals("tarokk") && nextPlayer.hasTheGivenSuit(suit)) {
            nextPlayer.markPlayerCardsClickableBySuit(suit);
        } else if (!suit.equals("tarokk") && !nextPlayer.hasTheGivenSuit(suit)) {
            if (!nextPlayer.hasAnyTarokks()) {
                nextPlayer.markAllPlayerCardsClickable();
            } else {
                setTarokksClickable(nextPlayer, game, playerCards); // pagat?
            }
        } else {
            if (nextPlayer.hasAnyTarokks()) {
                setTarokksClickable(nextPlayer, game, playerCards); //pagat?
            } else {
                nextPlayer.markAllPlayerCardsClickable();
            }
        }
        game.setTurnPlayer(nextPlayer.getName());
        gameRepository.save(game);
        List<PlayerCardDTO> playerCardList = mapperService.mapToPlayerCardListDTO(playerCards);
        List<PlayerCardDTO> sortedcardList = playerCardList.stream().sorted(Comparator.comparingInt(PlayerCardDTO::cardId)).toList();
        return new PlayerCardListDTO(sortedcardList, "game.playerCards");
    }

// XXI-fogás, Pagát ultimó kezelése?
    public PlayerCardListDTO handleWholeTrick(Game game) {
        List<Trick> tricks = game.getTricks();
        if (game.getTrickRound() == 9) {
            game.setSuccessfulUltimoInLastRound();
        }
        game.setSuccessfulXXICatching();
        Trick strongestCard;
        if (game.isTrickContainTarokk()) {
            strongestCard = game.getStrongestTarokkInTrick();
        } else {
            String calledCardSuit = tricks.getFirst().getCard().getSuit();
            strongestCard = game.getStrongestCardInASuit(calledCardSuit);
        }
        Player trickWinner = strongestCard.getPlayer();
        List<OwnTrick> wonCards = new ArrayList<>();
        for (Trick trick : tricks) {
            OwnTrick ownTrick = new OwnTrick();
            ownTrick.setCard(trick.getCard());
            ownTrick.setPlayer(trickWinner);
            wonCards.add(ownTrick);
        }
        ownTrickRepository.saveAll(wonCards);
        //game.getTricks().removeAll(tricks);
        game.setTricks(new ArrayList<>());
        game.setTurnPlayer(trickWinner.getName());
        game.setTrickRound(game.getTrickRound() + 1);
        gameRepository.save(game);
        return getFirstPlayerCards(trickWinner, game, game.getInvitedTarokk());
    }

    private void setTarokksClickable(Player nextPlayer, Game game, List<PlayerCard> playerCards) {

        if (nextPlayer.getRoleInGame().equals(RoleInGame.DECLARER) || nextPlayer.hasTheGivenTarokk(game.getInvitedTarokk())) {
            if (game.isDeclarerAnnouncedUltimo()) {
                nextPlayer.setAnnouncedUltimo(true);
            }
        } else {
            if (game.isOpponentAnnouncedUltimo()) {
                nextPlayer.setAnnouncedUltimo(true);
            }
        }
        if (!nextPlayer.isHasPagat()) {
            nextPlayer.markAllTarokksClickable();
        } else {
            if (nextPlayer.isAnnouncedUltimo()) {
                if (nextPlayer.hasAtLeastTwoTarokks()) {
                    for (PlayerCard playerCard : playerCards) {
                        playerCard.setClickable(playerCard.getCard().getStrength() > 1);
                    }
                } else {
                    nextPlayer.markAllTarokksClickable();
                }

            } else {
                nextPlayer.markAllTarokksClickable();
            }
        }
    }
}
