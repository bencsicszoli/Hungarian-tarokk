package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.model.entity.Skart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

@Repository
public interface SkartRepository extends JpaRepository<Skart, Long> {

    void deleteAllByGameId(Long gameId);
}
