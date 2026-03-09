package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.model.entity.DeckCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeckCardRepository extends JpaRepository<DeckCard, Long> {

    @NativeQuery(value = "SELECT * FROM deck WHERE game_id = ?1 AND card_order BETWEEN 1 AND 6 ORDER BY card_order")
    List<DeckCard> findTalonCards(Long gameId);

    @NativeQuery(value = "SELECT * FROM deck WHERE game_id = ?1 AND card_order BETWEEN ?2 AND ?3 ORDER BY card_order")
    List<DeckCard> findCardsByGameIdAndCardOrder(Long gameId, int from, int to);

    void deleteAllByGameId(Long gameId);
}
