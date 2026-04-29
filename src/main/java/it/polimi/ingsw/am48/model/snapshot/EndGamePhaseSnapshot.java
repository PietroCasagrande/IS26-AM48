package it.polimi.ingsw.am48.model.snapshot;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;

public class EndGamePhaseSnapshot extends PhaseSnapshot implements Serializable {
    @JsonCreator
    public EndGamePhaseSnapshot() {
        super("END_GAME");
    }
}