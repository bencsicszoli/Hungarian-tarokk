package com.codecool.tarokkgame.repository;

import com.codecool.tarokkgame.constants.RoleInGame;
import com.codecool.tarokkgame.model.entity.Game;
import com.codecool.tarokkgame.model.entity.OwnTrick;
import com.codecool.tarokkgame.model.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OwnTrickRepository extends JpaRepository<OwnTrick, Long> {
    List<OwnTrick> findAllByPlayer(Player player);
    //List<OwnTrick> findAllByPlayerGameAndPlayerRoleInGame(Game game, RoleInGame roleInGame);
    void deleteAllByPlayerGameId(Long playerGameId);

    @Query("""
    SELECT ot
    FROM OwnTrick ot
    WHERE ot.player.roleInGame IN :roles
    """)
    List<OwnTrick> findAllByRoles(@Param("roles") List<RoleInGame> roles);
}
