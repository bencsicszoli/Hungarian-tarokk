package com.codecool.tarokkgame.model.entity;

import com.codecool.tarokkgame.constants.RoleInGame;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "skart_type")
@Getter
@Setter
public abstract class Skart implements Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Game game;

    @ManyToOne
    protected Card card;

    public abstract RoleInGame getOwner();
}

