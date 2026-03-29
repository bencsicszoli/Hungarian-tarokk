package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.model.entity.DeclarerSkart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeclarerSkartRepository extends JpaRepository<DeclarerSkart, Long> {

    /*
    @NativeQuery(value = "SELECT COUNT(id) FROM declarer_skart WHERE game_id = ?1")
    int countByGameId(Long gameId);
*/
    List<DeclarerSkart> findAllByGameId(Long gameId);
    int countAllByGameId(Long gameId);
}
