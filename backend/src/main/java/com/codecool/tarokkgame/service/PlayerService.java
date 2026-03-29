package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.model.dto.messagedto.JoinMessageDTO;
import com.codecool.tarokkgame.model.entity.AppUser;
import com.codecool.tarokkgame.model.entity.Game;
import com.codecool.tarokkgame.model.entity.Player;
import com.codecool.tarokkgame.repository.AppUserRepository;
import com.codecool.tarokkgame.repository.GameRepository;
import com.codecool.tarokkgame.repository.PlayerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class PlayerService {

    private final AppUserRepository userRepository;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final MapperService mapper;

    public PlayerService(AppUserRepository userRepository, GameRepository gameRepository, PlayerRepository playerRepository, MapperService mapper) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.mapper = mapper;
    }

    @Transactional
    public JoinMessageDTO joinGame(String username) {

        // Player is already in game:
        Optional<Game> gameOpt = gameRepository.findLastGameByPlayer(username);
        if (gameOpt.isPresent()) {
            return mapper.mapToJoinMessageDTO(gameOpt.get(), gameOpt.get().getPlayers());
        }

        // A game with missing players exists
        AppUser user = userRepository.findByUsername(username).orElseThrow(() -> new NoSuchElementException(String.format("User %s not found", username.toUpperCase())));
        Game gameWithEmptySeat = gameRepository.findFirstGameByMissingPlayer().orElse(null);
        if (gameWithEmptySeat != null) {
            return createMessageWhenGameWithMissingPlayerExists(gameWithEmptySeat, user, username);

        // There is no game with missing players:
        } else {
            return createMessageWhenNoGameWithMissingPlayer(user, username);
        }
    }

    private JoinMessageDTO createMessageWhenGameWithMissingPlayerExists(Game gameWithEmptySeat, AppUser user, String username) {
        Player newPlayer = new Player();
        newPlayer.setUser(user);
        gameWithEmptySeat.setInformation(String.format("%s has joined the game", username.toUpperCase()));
        findEmptySeatAtCardTable(gameWithEmptySeat, newPlayer, username);
        List<Player> players = gameWithEmptySeat.getPlayers();
        players.add(newPlayer);
        if (players.size() == 4) {
            setRolesAtCardTable(players, gameWithEmptySeat);
        }
        newPlayer.setGame(gameWithEmptySeat);
        playerRepository.save(newPlayer);
        gameRepository.save(gameWithEmptySeat);
        return mapper.mapToJoinMessageDTO(gameWithEmptySeat, players);
    }

    private void findEmptySeatAtCardTable(Game gameWithEmptySeat, Player newPlayer, String username) {
        if (gameWithEmptySeat.getPlayer1() == null) {
            gameWithEmptySeat.setPlayer1(username);
            newPlayer.setPlace(1);
        } else if (gameWithEmptySeat.getPlayer2() == null) {
            gameWithEmptySeat.setPlayer2(username);
            newPlayer.setPlace(2);
        } else if (gameWithEmptySeat.getPlayer3() == null) {
            gameWithEmptySeat.setPlayer3(username);
            newPlayer.setPlace(3);
        } else if (gameWithEmptySeat.getPlayer4() == null) {
            gameWithEmptySeat.setPlayer4(username);
            newPlayer.setPlace(4);
        }
    }

    private void setRolesAtCardTable(List<Player> players, Game gameWithEmptySeat) {
        Player player1 = players.getFirst();
        gameWithEmptySeat.setDealer(player1.getName());
        Player player2 = players.get(1);
        gameWithEmptySeat.setStartPlayer(player2.getName());
        gameWithEmptySeat.setTurnPlayer(player2.getName());
    }

    private JoinMessageDTO createMessageWhenNoGameWithMissingPlayer(AppUser user, String username) {
        Game newGame = new Game();
        newGame.setPlayer1(username);
        Player newPlayer = new Player();
        newPlayer.setUser(user);
        newGame.setInformation(String.format("%s has joined the game", username.toUpperCase()));
        newPlayer.setPlace(1);
        Game savedGame = gameRepository.save(newGame);
        newPlayer.setGame(savedGame);
        playerRepository.save(newPlayer);
        return mapper.mapToJoinMessageDTO(savedGame, List.of(newPlayer));
    }
}
