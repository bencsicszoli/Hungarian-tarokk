package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.model.entity.PlayerCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerCardRepository extends JpaRepository<PlayerCard, Long> {
    List<PlayerCard> findAllByPlayerId(Long playerId);
    void deleteAllByPlayerId(Long playerId);
}
