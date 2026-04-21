package it.polimi.ingsw.am48.dto;

import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

public record JoinResult (GameSnapshot snapshot, boolean gameStarted){ }
