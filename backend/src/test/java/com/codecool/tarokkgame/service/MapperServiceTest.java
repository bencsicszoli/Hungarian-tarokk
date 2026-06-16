package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.model.dto.messagedto.response.PrivateInfoDTO;
import com.codecool.tarokkgame.model.entity.Player;
import com.codecool.tarokkgame.model.entity.RoundResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class MapperServiceTest {

    @InjectMocks
    private MapperService mapperService;

    @Test
    public void mapToPrivateResultShouldReturnPrivateInfoDTOIfPlayerHasResultWithInfo() {
        RoundResult result = new RoundResult();
        result.setInfo("This player won the game");
        Player player = new Player();
        player.setResult(result);

        PrivateInfoDTO infoDTO = mapperService.mapToPrivateResult(player);

        assertEquals(new PrivateInfoDTO("This player won the game", "game.privateResult"), infoDTO);
    }

    @Test
    public void mapToPrivateResultShouldReturnPrivateInfoDTOIfPlayerHasResultWithoutInfo() {
        RoundResult result = new RoundResult();
        Player player = new Player();
        player.setResult(result);

        PrivateInfoDTO infoDTO = mapperService.mapToPrivateResult(player);

        assertEquals(new PrivateInfoDTO(null, "game.privateResult"), infoDTO);
    }
}
