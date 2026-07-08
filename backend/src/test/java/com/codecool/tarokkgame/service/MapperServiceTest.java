package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.constants.MessageKey;
import com.codecool.tarokkgame.model.dto.LocalizedMessage;
import com.codecool.tarokkgame.model.dto.messagedto.response.PrivateInfoListDTO;
import com.codecool.tarokkgame.model.entity.Player;
import com.codecool.tarokkgame.model.entity.RoundResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class MapperServiceTest {

    @InjectMocks
    private MapperService mapperService;

    @Test
    public void mapToPrivateResultShouldReturnPrivateInfoListDTOIfPlayerHasResultWithInfo() {
        RoundResult result = new RoundResult();
        List<LocalizedMessage> info = List.of(new LocalizedMessage(MessageKey.RESULT_TOTAL, Map.of("sum", 5, "count", 5)));
        result.setInfo(info);
        Player player = new Player();
        player.setResult(result);

        PrivateInfoListDTO infoDTO = mapperService.mapToPrivateResult(player);

        assertEquals(new PrivateInfoListDTO(info, "game.privateResult"), infoDTO);
    }

    @Test
    public void mapToPrivateResultShouldReturnPrivateInfoListDTOIfPlayerHasResultWithoutInfo() {
        RoundResult result = new RoundResult();
        Player player = new Player();
        player.setResult(result);

        PrivateInfoListDTO infoDTO = mapperService.mapToPrivateResult(player);

        assertEquals(new PrivateInfoListDTO(null, "game.privateResult"), infoDTO);
    }
}
