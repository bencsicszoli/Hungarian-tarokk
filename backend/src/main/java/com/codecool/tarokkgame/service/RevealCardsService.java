package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.constants.RoleInGame;
import com.codecool.tarokkgame.model.dto.messagedto.response.CardImageDTO;
import com.codecool.tarokkgame.model.dto.messagedto.response.CardImageListDTO;
import com.codecool.tarokkgame.model.entity.DeclarerSkart;
import com.codecool.tarokkgame.model.entity.Game;
import com.codecool.tarokkgame.model.entity.OpponentSkart;
import com.codecool.tarokkgame.model.entity.OwnTrick;
import com.codecool.tarokkgame.repository.DeclarerSkartRepository;
import com.codecool.tarokkgame.repository.OpponentSkartRepository;
import com.codecool.tarokkgame.repository.OwnTrickRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RevealCardsService {
    private final OwnTrickRepository ownTrickRepository;
    private final DeclarerSkartRepository declarerSkartRepository;
    private final OpponentSkartRepository opponentSkartRepository;


    public RevealCardsService(OwnTrickRepository ownTrickRepository, DeclarerSkartRepository declarerSkartRepository, OpponentSkartRepository opponentSkartRepository) {
        this.ownTrickRepository = ownTrickRepository;
        this.declarerSkartRepository = declarerSkartRepository;
        this.opponentSkartRepository = opponentSkartRepository;
    }

    public CardImageListDTO getDeclarerSkart(long gameId) {
        List<DeclarerSkart> cards = declarerSkartRepository.findAllByGameId(gameId);
        if (cards.isEmpty()) {
            return null;
        }
        cards.sort(Comparator.comparingInt(c -> c.getCard().getId()));
        List<CardImageDTO> DTOs = new ArrayList<>();
        if (!cards.isEmpty()) {
            for (DeclarerSkart card : cards) {
                CardImageDTO dto = new CardImageDTO(card.getCard().getFrontImagePath());
                DTOs.add(dto);
            }
        }
        return new CardImageListDTO(DTOs, "game.declarerSkartImages");
    }

    public CardImageListDTO getOpponentSkart(long gameId) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        List<OpponentSkart> cards = opponentSkartRepository.findAllByGameId(gameId);
        cards.sort(Comparator.comparingInt(o -> o.getCard().getId()));
        List<CardImageDTO> DTOs = new ArrayList<>();
        if (!cards.isEmpty()) {
            for (OpponentSkart card : cards) {
                CardImageDTO dto = new CardImageDTO(card.getCard().getFrontImagePath());
                DTOs.add(dto);
            }
        }
        return new CardImageListDTO(DTOs, "game.opponentSkartImages");
    }

    public CardImageListDTO getDeclarerTricks(Game game) {
        List<OwnTrick> declarerTricks = ownTrickRepository.findAllByRolesAndPlayerGameId(List.of(RoleInGame.DECLARER, RoleInGame.DECLARER_PARTNER), game.getId());
        return createCardImageDTOList(declarerTricks, "game.declarerTrickImages");
    }

    public CardImageListDTO getOpponentTricks(Game game) {
        List<OwnTrick> opponentTricks = ownTrickRepository.findAllByRolesAndPlayerGameId(List.of(RoleInGame.OPPONENT), game.getId());
        return createCardImageDTOList(opponentTricks, "game.opponentTrickImages");
    }

    private CardImageListDTO createCardImageDTOList(List<OwnTrick> tricks, String messageType) {
        if (tricks.isEmpty()) {
            return null;
        }
        tricks.sort(Comparator.comparingLong(OwnTrick::getId));
        List<CardImageDTO> DTOs = new ArrayList<>();
        if (!tricks.isEmpty()) {
            for (OwnTrick trick : tricks) {
                DTOs.add(new CardImageDTO(trick.getCard().getFrontImagePath()));
            }
        }
        return new CardImageListDTO(DTOs, messageType);
    }
}
