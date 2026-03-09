package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.model.entity.TalonCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TalonCardRepository extends JpaRepository<TalonCard, Long> {
}
