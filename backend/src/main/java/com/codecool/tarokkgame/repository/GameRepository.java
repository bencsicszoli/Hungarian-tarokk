package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.model.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {


    @NativeQuery(value = "SELECT * FROM game WHERE player1 = ?1 OR player2 = ?1 OR player3 = ?1 OR player4 = ?1 ORDER BY id DESC LIMIT 1")
    Optional<Game> findLastGameByPlayer(String player);


    @NativeQuery(value = "SELECT * FROM game WHERE player1 IS NULL OR player2 IS NULL OR player3 IS NULL OR player4 IS NULL LIMIT 1")
    Optional<Game> findFirstGameByMissingPlayer();
/*
    @NativeQuery(value = "SELECT * FROM game JOIN player ON game.id = player.game_id WHERE player.user_id = ?1 ORDER BY game.id DESC LIMIT 1")
    Optional<Game> findLastGameByUserId(@Param("user_id") int userId);

    @NativeQuery(value = "SELECT id FROM game JOIN player ON game.id = player.game_id GROUP BY game.id HAVING COUNT(player.id) < 4 LIMIT 1")
    Optional<Long> findGameIdWithEmptySeat();

 */



}
