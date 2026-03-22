package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.model.entity.TalonCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TalonCardRepository extends JpaRepository<TalonCard, Long> {

    @NativeQuery(value = "SELECT * FROM talon_card WHERE game_id = ?1 ORDER BY id LIMIT ?2")
    List<TalonCard> findAllByGameIdAndQuantity(Long gameId, Integer quantity);

    @Modifying
    @NativeQuery(value = "DELETE FROM talon_card WHERE id BETWEEN ?1 AND ?2")
    void deleteAllByIdRange(Long from, Long to);

    @NativeQuery(value = "SELECT MIN(id) FROM talon_card WHERE game_id = ?1")
    long findSmallestId(Long gameId);

    @NativeQuery(value = "SELECT * FROM talon_card WHERE id BETWEEN ?1 AND ?2 AND game_id = ?3")
    List<TalonCard> findAllByIdRangeAndGameId(Long idFrom, Long idTo, Long gameId);
}
