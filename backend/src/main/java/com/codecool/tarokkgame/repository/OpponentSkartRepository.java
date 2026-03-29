package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.model.entity.OpponentSkart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

@Repository
public interface OpponentSkartRepository extends JpaRepository<OpponentSkart, Long> {

    /*
    @NativeQuery(value = "SELECT COUNT(id) FROM opponent_skart WHERE game_id = ?1")
    int countByGameId(Long gameId);

     */

    int countAllByGameId(long gameId);
}
