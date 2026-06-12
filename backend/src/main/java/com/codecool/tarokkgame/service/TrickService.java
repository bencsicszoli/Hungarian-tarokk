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
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class TrickService {
    private final MapperService mapperService;
    private final TrickRepository trickRepository;
    private final OwnTrickRepository ownTrickRepository;

    public TrickService(MapperService mapperService, TrickRepository trickRepository, OwnTrickRepository ownTrickRepository) {
        this.mapperService = mapperService;
        this.trickRepository = trickRepository;
        this.ownTrickRepository = ownTrickRepository;
    }

    public PlayerCardListDTO getFirstPlayerCards(Player player, Game game, int calledTarokk) {
        List<PlayerCard> playerCards = player.getPlayerCards();

        setPlayerCardsClickableAndHandleCaseUltimo(player, game, playerCards, calledTarokk);

        List<PlayerCardDTO> playerCardList = mapperService.mapToPlayerCardListDTO(playerCards);
        List<PlayerCardDTO> sortedcardList = playerCardList.stream().sorted(Comparator.comparingInt(PlayerCardDTO::cardId)).toList();
        return new PlayerCardListDTO(sortedcardList, "game.playerCards");
    }

    //Handle calledTarokk in play
    public TrickCardListDTO getTrickCards(Game game, Player player, Card card) {
        for (PlayerCard playerCard : player.getPlayerCards()) {
            if (playerCard.getCard().getId() == card.getId()) {
                player.getPlayerCards().remove(playerCard);
                break;
            }
        }
        if (card.getStrength() == game.getInvitedTarokk()) {
            player.setRoleInGame(RoleInGame.DECLARER_PARTNER);
            game.markPlayersAsOpponent();
        }
        Trick trick = new Trick();
        trick.setCard(card);
        trick.setPlayer(player);
        trick.setGame(game);
        int x = (int) ((Math.random() - 0.5) * 30);
        trick.setX(x);
        int y = (int) ((Math.random() - 0.5) * 20);
        trick.setY(y);
        int rotation = (int) (Math.random() * 180);
        trick.setRotation(rotation);
        trickRepository.save(trick);
        List<Trick> tricks = game.getTricks();
        tricks.add(trick);
        List<Trick> sortedTricks = tricks.stream().sorted(Comparator.comparingLong(Trick::getId)).toList();
        int cardsInHand = player.getPlayerCards().size();
        List<TrickCardDTO> trickCardDTOList = mapperService.mapToTrickCardListDTO(sortedTricks);
        return new TrickCardListDTO(trickCardDTOList, player.getName(), cardsInHand, "game.trickCards");
    }

    public PlayerCardListDTO getTurnPlayerCards(Game game, Player player) {
        Player nextPlayer = game.getNextPlayer(player);
        String firstCardSuit;
        Optional<Trick> firstCardSuitOpt = game.getTricks().stream().min(Comparator.comparingLong(Trick::getId));
        if (firstCardSuitOpt.isPresent()) {
            firstCardSuit = firstCardSuitOpt.get().getCard().getSuit();
        } else {
            throw new NoSuchElementException("First card suit not found");
        }
        List<PlayerCard> playerCards = setNextPlayerCardsClickable(nextPlayer, firstCardSuit, game);

        game.setTurnPlayer(nextPlayer.getName());
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
            case 1: game.setPlayer1TrickCards(game.getPlayer1TrickCards() + 4); break;
            case 2: game.setPlayer2TrickCards(game.getPlayer2TrickCards() + 4); break;
            case 3: game.setPlayer3TrickCards(game.getPlayer3TrickCards() + 4); break;
            case 4: game.setPlayer4TrickCards(game.getPlayer4TrickCards() + 4); break;
            default: throw new RuntimeException("Invalid trick winner place");
        }

        List<OwnTrick> wonCards = mapperService.mapToOwnTrickList(tricks, trickWinner);
        ownTrickRepository.saveAll(wonCards);
        game.getTricks().removeAll(tricks); // ?
        trickRepository.deleteAllByGameId(game.getId());
        game.setTurnPlayer(trickWinner.getName());
        game.setTrickRound(game.getTrickRound() + 1);
        if (game.getTrickRound() == 10) {
            game.setState(GameState.FINISHED);
            return null;
        }
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
        List<PlayerCard> playerCards = nextPlayer.getPlayerCards();
        if (!suit.equals("tarokk") && nextPlayer.hasTheGivenSuit(suit)) {
            nextPlayer.markPlayerCardsClickableBySuit(suit);
        } else if (!suit.equals("tarokk") && !nextPlayer.hasTheGivenSuit(suit)) {
            if (!nextPlayer.hasAnyTarokks()) {
                nextPlayer.markAllPlayerCardsClickable();
            } else {
                setTarokksClickable(nextPlayer, game, playerCards);
            }
        } else {
            if (nextPlayer.hasAnyTarokks()) {
                setTarokksClickable(nextPlayer, game, playerCards);
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
