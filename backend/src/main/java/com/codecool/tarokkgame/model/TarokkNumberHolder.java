package com.codecool.tarokkgame.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TarokkNumberHolder {
    private boolean isEightTarokk = false;
    private boolean isNineTarokk = false;

    public TarokkNumberHolder(boolean isEightTarokk, boolean isNineTarokk) {
        this.isEightTarokk = isEightTarokk;
        this.isNineTarokk = isNineTarokk;
    }
}
