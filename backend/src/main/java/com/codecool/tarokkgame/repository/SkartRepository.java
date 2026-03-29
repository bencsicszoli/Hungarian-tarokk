package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.model.entity.Skart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

@Repository
public interface SkartRepository extends JpaRepository<Skart, Long> {
    @NativeQuery(value = "SELECT COUNT(id) WHERE game_id = ?1")
    int countByGameId(Long gameId);
}
