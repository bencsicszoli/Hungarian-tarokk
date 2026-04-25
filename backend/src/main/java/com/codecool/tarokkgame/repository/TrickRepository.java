package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.model.entity.Trick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrickRepository extends JpaRepository<Trick, Long> {

}
