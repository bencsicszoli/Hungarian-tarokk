package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.constants.RoleInGame;
import com.codecool.tarokkgame.model.entity.OwnTrick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OwnTrickRepository extends JpaRepository<OwnTrick, Long> {
    void deleteAllByPlayerGameId(Long playerGameId);

    @Query("""
    SELECT ot
    FROM OwnTrick ot
    WHERE ot.player.roleInGame IN :roles
    """)
    List<OwnTrick> findAllByRolesAndPlayerGameId(@Param("roles") List<RoleInGame> roles, long playerGameId);
}
