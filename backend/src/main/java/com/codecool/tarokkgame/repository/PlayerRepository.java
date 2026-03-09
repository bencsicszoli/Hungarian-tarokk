package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.model.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByUserId(int userId);
    Optional<Player> findByUserUsernameAndGameId(String username, long gameId);
}
