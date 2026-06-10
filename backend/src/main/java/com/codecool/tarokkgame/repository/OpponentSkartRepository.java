package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.model.entity.OpponentSkart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpponentSkartRepository extends JpaRepository<OpponentSkart, Long> {

    List<OpponentSkart> findAllByGameId(long gameId);

    int countAllByGameId(long gameId);

    void deleteAllByGameId(long gameId);
}
