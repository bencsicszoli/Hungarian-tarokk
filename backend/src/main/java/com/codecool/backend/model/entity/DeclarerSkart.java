package com.codecool.backend.model.entity;

import com.codecool.backend.constants.RoleInGame;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("DECLARER")
@Getter
@Setter
public class DeclarerSkart extends Skart {

    @Override
    public RoleInGame getOwner() {
        return RoleInGame.DECLARER;
    }

    @Override
    public int getCardValue() {
        return card.getPointValue();
    }
}

