package com.codecool.tarokkgame.model.entity;

import com.codecool.tarokkgame.constants.RoleInGame;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("OPPONENT")
@Getter
@Setter
public class OpponentSkart extends Skart {

    @Override
    public RoleInGame getOwner() {
        return RoleInGame.OPPONENT;
    }

    @Override
    public int getCardValue() {
        return card.getPointValue();
    }
}

