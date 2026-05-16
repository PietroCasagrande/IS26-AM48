package it.polimi.ingsw.am48.model.delta;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.am48.network.client.ClientModel;

import java.io.Serializable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,      // uso un campo "type" nel JSON
        include = JsonTypeInfo.As.PROPERTY,           // il campo è incluso nel payload
        property = "type"                // nome del campo
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TotemPlacedDelta.class,          name = "totemPlaced"),
        @JsonSubTypes.Type(value = CharacterCardPickedDelta.class,  name = "characterCardPicked"),
        @JsonSubTypes.Type(value = BuildingCardPickedDelta.class,   name = "buildingCardPicked"),
        @JsonSubTypes.Type(value = EndTurnDelta.class,              name = "endTurn"),
        @JsonSubTypes.Type(value = EndGameDelta.class,              name = "endGame"),
        @JsonSubTypes.Type(value = LeaderboardDelta.class,          name = "leaderboard")
})

public abstract class GameDelta implements Serializable {
    public abstract void applyTo(ClientModel model);
}
