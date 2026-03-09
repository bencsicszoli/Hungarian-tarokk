package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.model.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Integer> {
}
