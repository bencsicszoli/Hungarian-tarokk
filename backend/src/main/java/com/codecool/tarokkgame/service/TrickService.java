package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.constants.GameState;
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
    private final TrickRepository trickRepository;
    private final OwnTrickRepository ownTrickRepository;
    private final PlayerRepository playerRepository;

    public TrickService(GameRepository gameRepository, PlayerCardRepository playerCardRepository, MapperService mapperService, TrickRepository trickRepository, OwnTrickRepository ownTrickRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerCardRepository = playerCardRepository;
        this.mapperService = mapperService;
        this.trickRepository = trickRepository;
        this.ownTrickRepository = ownTrickRepository;
        this.playerRepository = playerRepository;
    }

    public PlayerCardListDTO getFirstPlayerCards(Player player, Game game, int calledTarokk) {
        List<PlayerCard> playerCards = playerCardRepository.findAllByPlayerId(player.getId());  // player.getPlayerCards() ?

        setPlayerCardsClickableAndHandleCaseUltimo(player, game, playerCards, calledTarokk);

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
        int x = (int) ((Math.random() - 0.5) * 30);
        trick.setX(x);
        int y = (int) ((Math.random() - 0.5) * 20);
        trick.setY(y);
        int rotation = (int) (Math.random() * 90);
        trick.setRotation(rotation);
        List<Trick> tricks = game.getTricks();
        tricks.add(trick);
        trickRepository.saveAll(tricks);  // ?

        List<TrickCardDTO> trickCardDTOList = mapperService.mapToTrickCardListDTO(tricks);
        int cardsInHand = playerCardRepository.countAllByPlayerId(player.getId());
        gameRepository.save(game);
        return new TrickCardListDTO(trickCardDTOList, player.getName(), cardsInHand, "game.trickCards");
    }


    public PlayerCardListDTO getTurnPlayerCards(Game game, Player player, Card firstCardInTrick) {  //First trick
        Player nextPlayer = game.getNextPlayer(player);
        String suit = firstCardInTrick.getSuit();

        List<PlayerCard> playerCards = setNextPlayerCardsClickable(nextPlayer, suit, game);

        game.setTurnPlayer(nextPlayer.getName());
        gameRepository.save(game);
        List<PlayerCardDTO> playerCardList = mapperService.mapToPlayerCardListDTO(playerCards);
        List<PlayerCardDTO> sortedcardList = playerCardList.stream().sorted(Comparator.comparingInt(PlayerCardDTO::cardId)).toList();
        return new PlayerCardListDTO(sortedcardList, "game.playerCards");
    }

    public PlayerCardListDTO handleWholeTrick(Game game) {
        List<Trick> tricks = game.getTricks();
        if (game.getTrickRound() == 9) {
            game.setSuccessfulUltimoInLastRound();
        }
        game.setSuccessfulXXICatching();
        Trick strongestCard = getStrongestCard(game, tricks);
        Player trickWinner = strongestCard.getPlayer();
        switch (trickWinner.getPlace()) {
            case 1: game.setPlayer1TrickCards(game.getPlayer1TrickCards() + 4);
            break;
            case 2: game.setPlayer2TrickCards(game.getPlayer2TrickCards() + 4);
            break;
            case 3: game.setPlayer3TrickCards(game.getPlayer3TrickCards() + 4);
            break;
            case 4: game.setPlayer4TrickCards(game.getPlayer4TrickCards() + 4);
            break;
            default: throw new RuntimeException("Invalid trick winner place");
        }

        List<OwnTrick> wonCards = mapperService.mapToOwnTrickList(tricks, trickWinner);

        ownTrickRepository.saveAll(wonCards);
        //game.getTricks().removeAll(tricks);
        game.setTricks(new ArrayList<>(4));
        game.setTurnPlayer(trickWinner.getName());
        game.setTrickRound(game.getTrickRound() + 1);
        if (game.getTrickRound() == 10) {
            game.setState(GameState.FINISHED);
            gameRepository.save(game);
            return null;
        }
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

    private void setPlayerCardsClickableAndHandleCaseUltimo(Player player, Game game, List<PlayerCard> playerCards, int calledTarokk) {
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
    }

    private List<PlayerCard> setNextPlayerCardsClickable(Player nextPlayer, String suit, Game game) {
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
        return playerCards;
    }

    private Trick getStrongestCard(Game game, List<Trick> tricks) {
        Trick strongestCard;
        if (game.isTrickContainTarokk()) {
            strongestCard = game.getStrongestTarokkInTrick();
        } else {
            String calledCardSuit = tricks.getFirst().getCard().getSuit();
            strongestCard = game.getStrongestCardInASuit(calledCardSuit);
        }
        return strongestCard;
    }
}
