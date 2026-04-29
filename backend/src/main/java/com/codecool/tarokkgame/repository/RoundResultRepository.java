package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.model.entity.RoundResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoundResultRepository extends JpaRepository<RoundResult, Long> {
}
