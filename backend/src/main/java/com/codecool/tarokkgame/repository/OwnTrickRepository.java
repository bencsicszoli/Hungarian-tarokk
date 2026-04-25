package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.model.entity.OwnTrick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnTrickRepository extends JpaRepository<OwnTrick, Long> {
}
