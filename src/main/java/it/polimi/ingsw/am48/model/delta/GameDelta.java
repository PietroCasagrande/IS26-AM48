package it.polimi.ingsw.am48.model.delta;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.am48.network.client.ClientModel;

import java.io.Serializable;

/**
 * Base type for all incremental game state updates ("deltas") sent from the
 * server to the clients. Each concrete subclass represents a single, atomic
 * change to the game state (a totem placed, a card picked, the end of a turn,
 * etc.) and knows how to apply itself to a client-side
 * {@link ClientModel}.
 *
 * <p>Deltas are serialized to and from JSON using Jackson polymorphic typing:
 * a {@code "type"} property in the payload selects the concrete subclass, as
 * declared by the {@link JsonSubTypes} mapping above. This lets the network
 * layer transmit a heterogeneous stream of deltas while preserving their
 * concrete type on deserialization.
 * </p>
 *
 * @see ClientModel
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,      // uso un campo "type" nel JSON
        include = JsonTypeInfo.As.PROPERTY,           // il campo è incluso nel payload
        property = "type"                // nome del campo
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TotemPlacedDelta.class,          name = "totemPlaced"),
        @JsonSubTypes.Type(value = OfferCardADelta.class,           name = "offerCardA"),
        @JsonSubTypes.Type(value = CharacterCardPickedDelta.class,  name = "characterCardPicked"),
        @JsonSubTypes.Type(value = BuildingCardPickedDelta.class,   name = "buildingCardPicked"),
        @JsonSubTypes.Type(value = SkipDelta.class,                 name = "skip"),
        @JsonSubTypes.Type(value = EndTurnDelta.class,              name = "endTurn"),
        @JsonSubTypes.Type(value = EndGameDelta.class,              name = "endGame"),
        @JsonSubTypes.Type(value = LeaderboardDelta.class,          name = "leaderboard")
})
public abstract class GameDelta implements Serializable {
    /**
     * Applies this delta to the given client model, mutating its local replica
     * of the game state to reflect the change carried by the delta.
     *
     * @param model the client model whose state must be updated
     */
    public abstract void applyTo(ClientModel model);
}
