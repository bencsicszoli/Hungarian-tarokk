package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.constants.MessageKey;
import com.codecool.tarokkgame.model.dto.LocalizedMessage;
import com.codecool.tarokkgame.model.dto.messagedto.response.JoinMessageDTO;
import com.codecool.tarokkgame.model.entity.AppUser;
import com.codecool.tarokkgame.model.entity.Game;
import com.codecool.tarokkgame.model.entity.Player;
import com.codecool.tarokkgame.repository.AppUserRepository;
import com.codecool.tarokkgame.repository.GameRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JoiningService {

    public static final int NUMBER_OF_NECESSARY_PLAYERS = 4;
    private final AppUserRepository userRepository;
    private final GameRepository gameRepository;
    private final MapperService mapper;

    public JoiningService(AppUserRepository userRepository, GameRepository gameRepository, MapperService mapper) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
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

    @Transactional
    public JoinMessageDTO joinGameWithId(String username, long gameId) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new NoSuchElementException(String.format("Game with id %s not found", gameId)));
        AppUser user = userRepository.findByUsername(username).orElseThrow(() -> new NoSuchElementException(String.format("User %s not found", username.toUpperCase())));
        if (game.getPlayers() == null) {
            return createMessageWhenNoGameWithMissingPlayer(user, username);
        } else {
            return createMessageWhenGameWithMissingPlayerExists(game, user, username);
        }
    }

    private JoinMessageDTO createMessageWhenGameWithMissingPlayerExists(Game gameWithEmptySeat, AppUser user, String username) {
        Player newPlayer = new Player();
        newPlayer.setUser(user);
        gameWithEmptySeat.setInformation(List.of(new LocalizedMessage(MessageKey.JOIN_PLAYER_JOINED, Map.of("username", username.toUpperCase()))));
        findEmptySeatAtCardTable(gameWithEmptySeat, newPlayer, username);
        List<Player> players = gameWithEmptySeat.getPlayers();
        players.add(newPlayer);
        List<Player> sortedPlayers = players.stream().sorted(Comparator.comparing(Player::getPlace)).toList();
        if (players.size() == NUMBER_OF_NECESSARY_PLAYERS) {
            setRolesAtCardTable(sortedPlayers, gameWithEmptySeat);
        }
        newPlayer.setGame(gameWithEmptySeat);
        return mapper.mapToJoinMessageDTO(gameWithEmptySeat, sortedPlayers);
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

    private void setRolesAtCardTable(List<Player> players, Game gameWithEmptySeat) {;
        Player player1 = players.getFirst();
        gameWithEmptySeat.setDealer(player1.getName());
        Player player2 = players.get(1);
        gameWithEmptySeat.setStartPlayer(player2.getName());
        gameWithEmptySeat.setTurnPlayer(player2.getName());
    }

    private JoinMessageDTO createMessageWhenNoGameWithMissingPlayer(AppUser user, String username) {
        Game newGame = new Game();
        gameRepository.save(newGame);
        newGame.setPlayer1(username);
        Player newPlayer = new Player();
        newPlayer.setUser(user);
        newGame.setInformation(List.of(new LocalizedMessage(MessageKey.JOIN_PLAYER_JOINED, Map.of("username", username.toUpperCase()))));
        newPlayer.setPlace(1);
        List<Player> players = new ArrayList<>(4);
        players.add(newPlayer);
        newGame.setPlayers(players);
        newPlayer.setGame(newGame);
        return mapper.mapToJoinMessageDTO(newGame, List.of(newPlayer));
    }
}
