package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.constants.GameState;
import com.codecool.tarokkgame.constants.RoleInGame;
import com.codecool.tarokkgame.model.dto.messagedto.DeclarerSkartWithTarokkDTO;
import com.codecool.tarokkgame.model.dto.messagedto.PlayerCardDTO;
import com.codecool.tarokkgame.model.dto.messagedto.PlayerCardListDTO;
import com.codecool.tarokkgame.model.dto.messagedto.PublicSkartDTO;
import com.codecool.tarokkgame.model.entity.*;
import com.codecool.tarokkgame.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TalonService {
    private final TalonCardRepository talonCardRepository;
    private final SkartRepository skartRepository;
    private final PlayerCardRepository playerCardRepository;
    private final CardRepository cardRepository;
    private final MapperService mapperService;
    private final DeclarerSkartRepository declarerSkartRepository;
    private final OpponentSkartRepository opponentSkartRepository;
    private final PlayerRepository playerRepository;

    public TalonService(TalonCardRepository talonCardRepository, SkartRepository skartRepository, PlayerCardRepository playerCardRepository, CardRepository cardRepository, MapperService mapperService, DeclarerSkartRepository declarerSkartRepository, OpponentSkartRepository opponentSkartRepository, PlayerRepository playerRepository) {
        this.talonCardRepository = talonCardRepository;
        this.skartRepository = skartRepository;
        this.playerCardRepository = playerCardRepository;
        this.cardRepository = cardRepository;
        this.mapperService = mapperService;
        this.declarerSkartRepository = declarerSkartRepository;
        this.opponentSkartRepository = opponentSkartRepository;
        this.playerRepository = playerRepository;
    }

    public PlayerCardListDTO allocateTalonCards(Game game, Player player, long idFrom, long idTo) {
        List<PlayerCardDTO> DTOs = new ArrayList<>();
        if (idFrom <= idTo) {
            addTalonCardsToPlayerHand(idFrom, idTo, player, game);
        }
        List<PlayerCard> playerCards = playerCardRepository.findAllByPlayerId(player.getId());
        setPlayerCardsClickable(playerCards, player, game, DTOs);

        List<PlayerCardDTO> sortedDTO = DTOs.stream().sorted(Comparator.comparingInt(PlayerCardDTO::cardId)).toList();
        return new PlayerCardListDTO(sortedDTO, "game.playerCardsWithTalon");
    }

    public PlayerCardListDTO discardTalonCards(Game game, Player player, List<PlayerCardDTO> cardDTOS) {
        List<Skart> cardsToSkart = new ArrayList<>();
        if (player.getRoleInGame().equals(RoleInGame.DECLARER)) {
            placeCardsToDeclarerSkart(cardDTOS, game, cardsToSkart, player);
        } else {
            placeCardsToOpponentSkart(cardDTOS, game, cardsToSkart, player);
        }
        skartRepository.saveAll(cardsToSkart);
        playerRepository.save(player);
        return getSortedPlayerHandFromPlayerCardList(player);
    }

    public PublicSkartDTO createSkartDTO(Game game, int thrownCards, Player player) {
        int declarerSkartLength = declarerSkartRepository.countAllByGameId(game.getId());
        int opponentSkartLength = opponentSkartRepository.countAllByGameId(game.getId());
        String singularOrPluralCard;
        if (thrownCards == 1) {
            singularOrPluralCard = "card";
        } else {
            singularOrPluralCard = "cards";
        }
        String discardedCardsInfo = player.getName() + " added " + thrownCards + " " + singularOrPluralCard + " to skart";
        Player nextPlayer = game.getNextPlayer(player);
        String turnPlayer = nextPlayer.getName();
        game.setTurnPlayer(turnPlayer);
        if (declarerSkartLength + opponentSkartLength == 6) {
            game.setState(GameState.BONUS_ANNOUNCEMENT);
            game.setTurnPlayer(game.getDeclarer());
            turnPlayer = game.getDeclarer();
        }
        int playerHandLength = player.getPlayerCards().size();
        return new PublicSkartDTO(player.getName(), playerHandLength, declarerSkartLength, opponentSkartLength, discardedCardsInfo, turnPlayer, "game.publicSkartInfo");
    }

    public DeclarerSkartWithTarokkDTO createPublicSkartDTO(Game game) {
        if (!game.isTarokkInDeclarerSkart() && !game.isTarokkInOpponentSkart()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        List<PlayerCardDTO> playerCardDTOs = new ArrayList<>();
        if (game.isTarokkInDeclarerSkart()) {
            handleTarokksInDeclarerSkart(game, builder, playerCardDTOs);
        }
        if (game.isTarokkInOpponentSkart()) {
            handleTarokksInOpponentSkart(game, builder);
        }
        return new DeclarerSkartWithTarokkDTO(playerCardDTOs, builder.toString(), "game.tarokkInSkart");
    }

    private void addTalonCardsToPlayerHand(long idFrom, long idTo, Player player, Game game) {
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

    private void setPlayerCardsClickable(List<PlayerCard> playerCards, Player player, Game game, List<PlayerCardDTO> DTOs) {
        for (PlayerCard playerCard : playerCards) {
            boolean clickable = setIfCardsNotClickable(playerCard, player, game);
            PlayerCardDTO dto = new PlayerCardDTO(playerCard.getCard().getId(), player.getId(), playerCard.getCard().getFrontImagePath(), clickable);
            DTOs.add(dto);
        }
    }

    private void placeCardsToDeclarerSkart(List<PlayerCardDTO> cardDTOS, Game game, List<Skart> cardsToSkart, Player player) {
        for (PlayerCardDTO playerCardDTO : cardDTOS) {
            if (playerCardDTO.imagePath().contains("tarokk")) {
                game.setTarokkInDeclarerSkart(true);
            }
            Skart skartCard = new DeclarerSkart();
            addPlayerCardToSkart(playerCardDTO, skartCard, game, cardsToSkart, player);
        }
    }

    private void placeCardsToOpponentSkart(List<PlayerCardDTO> cardDTOS, Game game, List<Skart> cardsToSkart, Player player) {
        for (PlayerCardDTO playerCardDTO : cardDTOS) {
            if (playerCardDTO.imagePath().contains("tarokk")) {
                game.setTarokkInOpponentSkart(true);
                player.setTarokksInSkart(player.getTarokksInSkart() + 1);
            }
            Skart skartCard = new OpponentSkart();
            addPlayerCardToSkart(playerCardDTO, skartCard, game,cardsToSkart, player);
        }
    }

    private void addPlayerCardToSkart(PlayerCardDTO playerCardDTO, Skart skartCard, Game game, List<Skart> cardsToSkart, Player player) {
        Card card = cardRepository.findById(playerCardDTO.cardId()).orElseThrow(() -> new NoSuchElementException("Card not found"));
        skartCard.setCard(card);
        skartCard.setGame(game);
        cardsToSkart.add(skartCard);
        playerCardRepository.deleteByPlayerIdAndCardId(player.getId(), card.getId());
    }

    private PlayerCardListDTO getSortedPlayerHandFromPlayerCardList(Player player) {
        List<PlayerCard> playerCards = playerCardRepository.findAllByPlayerId(player.getId());
        List<PlayerCardDTO> playerCardDTOS = mapperService.mapToPlayerCardListDTO(playerCards);
        List<PlayerCardDTO> sortedCards = playerCardDTOS.stream().sorted(Comparator.comparingInt(PlayerCardDTO::cardId)).toList();
        return new PlayerCardListDTO(sortedCards, "game.playerCards");
    }

    private void handleTarokksInDeclarerSkart(Game game, StringBuilder builder, List<PlayerCardDTO> playerCardDTOs) {
        Player declarer = playerRepository.findByUserUsernameAndGameId(game.getDeclarer(), game.getId()).orElseThrow(() -> new NoSuchElementException("Declarer not found"));
        List<DeclarerSkart> skartCards = declarerSkartRepository.findAllByGameId(game.getId());
        int tarokks = changeFrontImageIfTarokk(skartCards, playerCardDTOs, declarer);
        if (tarokks != 1) {
            builder.append(declarer.getName()).append(" placed ").append(tarokks).append(" tarokks").append(" in skart!");
        } else {
            builder.append(declarer.getName()).append(" placed 1 tarokk in skart!");
        }
    }

    private void handleTarokksInOpponentSkart(Game game, StringBuilder builder) {
        String opponentSkartInfo = game.getMessageWithTarokksInOpponentSkart();
        builder.append(opponentSkartInfo);
    }

    private boolean setIfCardsNotClickable(PlayerCard playerCard, Player player, Game game) {
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
        return clickable;
    }

    private int changeFrontImageIfTarokk(List<DeclarerSkart> skartCards, List<PlayerCardDTO> playerCardDTOs, Player declarer) {
        int tarokks = 0;
        for (DeclarerSkart skartCard : skartCards) {
            Card card = skartCard.getCard();
            if (skartCard.getCard().getSuit().equals("tarokk")) {
                tarokks++;
                PlayerCardDTO cardDTO = new PlayerCardDTO(card.getId(), declarer.getId(), card.getFrontImagePath(), false);
                playerCardDTOs.add(cardDTO);
            } else {
                PlayerCardDTO cardDTO = new PlayerCardDTO(card.getId(), declarer.getId(), "Back.png", false);
                playerCardDTOs.add(cardDTO);
            }
        }
        return tarokks;
    }
}
