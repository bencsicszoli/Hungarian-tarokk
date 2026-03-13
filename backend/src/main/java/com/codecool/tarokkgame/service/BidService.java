package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.constants.BidLevel;
import com.codecool.tarokkgame.model.dto.messagedto.PotentialBidsDTO;
import com.codecool.tarokkgame.model.entity.Player;
import com.codecool.tarokkgame.repository.GameRepository;
import com.codecool.tarokkgame.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BidService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;

    public BidService(GameRepository gameRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
    }

    public PotentialBidsDTO getPotentialBidsToStartPlayer(String username, long gameId) {
        Player player = playerRepository.findByUserUsernameAndGameId(username, gameId).orElseThrow(() -> new NoSuchElementException("Player not found"));
        boolean hasAnyHonours = player.hasAnyHonours();
        if (!hasAnyHonours) {
            return new PotentialBidsDTO("None", List.of(BidLevel.PASS.getDescription()), "game.firstPotentialBids");
        } else {
            if (player.couldAnnounce18Invit()) {
                return new PotentialBidsDTO(
                        "None",
                        List.of(
                                BidLevel.PASS.getDescription(),
                                BidLevel.THREE.getDescription(),
                                BidLevel.ONE.getDescription()),
                        "game.firstPotentialBids");
            } else if (player.couldAnnounce19Invit()) {
                return new PotentialBidsDTO(
                        "None",
                        List.of(
                                BidLevel.PASS.getDescription(),
                                BidLevel.THREE.getDescription(),
                                BidLevel.TWO.getDescription()),
                        "game.firstPotentialBids");

            } else {
                return new PotentialBidsDTO(
                        "None",
                        List.of(
                                BidLevel.THREE.getDescription(),
                                BidLevel.PASS.getDescription()),
                        "game.firstPotentialBids");
            }
        }
    }
}
