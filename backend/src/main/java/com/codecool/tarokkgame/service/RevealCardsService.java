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
import com.codecool.tarokkgame.repository.SkartRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        List<OpponentSkart> cards = opponentSkartRepository.findAllByGameId(gameId);
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
        List<OwnTrick> declarerTricks = ownTrickRepository.findAllByPlayerGameAndPlayerRoleInGame(game, RoleInGame.DECLARER);
        List<OwnTrick> partnerTricks = ownTrickRepository.findAllByPlayerGameAndPlayerRoleInGame(game, RoleInGame.DECLARER_PARTNER);
        List<CardImageDTO> DTOs = new ArrayList<>();
        if (!declarerTricks.isEmpty()) {
            for (OwnTrick declarerTrick : declarerTricks) {
                DTOs.add(new CardImageDTO(declarerTrick.getCard().getFrontImagePath()));
            }
        }
        if (!partnerTricks.isEmpty()) {
            for (OwnTrick partnerTrick : partnerTricks) {
                DTOs.add(new CardImageDTO(partnerTrick.getCard().getFrontImagePath()));
            }
        }
        return new CardImageListDTO(DTOs, "game.declarerTrickImages");
    }

    public CardImageListDTO getOpponentTricks(Game game) {
        List<OwnTrick> opponentTricks = ownTrickRepository.findAllByPlayerGameAndPlayerRoleInGame(game, RoleInGame.OPPONENT);
        List<CardImageDTO> DTOs = new ArrayList<>();
        if (!opponentTricks.isEmpty()) {
            for (OwnTrick opponentTrick : opponentTricks) {
                DTOs.add(new CardImageDTO(opponentTrick.getCard().getFrontImagePath()));
            }
        }
        return new CardImageListDTO(DTOs, "game.opponentTrickImages");
    }
}
