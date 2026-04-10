package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.model.entity.FakeDeckCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FakeDeckCardRepository extends JpaRepository<FakeDeckCard, Long> {
}
