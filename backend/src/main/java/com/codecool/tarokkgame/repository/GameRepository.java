package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.model.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    @NativeQuery(value = "SELECT * FROM game WHERE (player1 = ?1 OR player2 = ?1 OR player3 = ?1 OR player4 = ?1) AND is_private_game = false ORDER BY id DESC LIMIT 1")
    Optional<Game> findLastGameByPlayer(String player);

    @NativeQuery(value = "SELECT * FROM game WHERE (player1 IS NULL OR player2 IS NULL OR player3 IS NULL OR player4 IS NULL) AND is_private_game = false LIMIT 1")
    Optional<Game> findFirstGameByMissingPlayer();
}
