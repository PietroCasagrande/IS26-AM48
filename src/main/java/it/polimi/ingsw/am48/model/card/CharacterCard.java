package it.polimi.ingsw.am48.model.card;

import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

/**
 * Represents a specific character card within the game.
 * <p>
 * Character cards are fundamental units that form a player's tribe.
 * Upon acquisition, they permanently join the player's personal board
 * and typically trigger an immediate, "one-shot" effect.
 */
public class CharacterCard extends Card {
    private final CharacterType type;
    private final int minPlayers;

    /**
     * Constructs a new {@code CharacterCard} with its specific attributes.
     *
     * @param cardId     the unique string identifier for this card
     * @param era        the chronological era to which this card belongs
     * @param strategy   the specific "one-shot" effect associated with this character
     * @param type       the type of the character (e.g., BUILDER, SHAMAN)
     * @param minPlayers the minimum number of players required for this card to be in the game
     */
    public CharacterCard(String cardId, Era era, CardStrategy strategy,
                         CharacterType type, int minPlayers) {
        super(cardId, era, strategy);
        this.type = type;
        this.minPlayers = minPlayers;
    }

    /**
     * Executes the acquisition logic specific to Character cards.
     * <p>
     * This method adds the character directly to the active player's tribe and
     * immediately triggers its strategic effect. Because character strategies
     * represent immediate actions rather than delayed listeners, the effect is
     * resolved on the spot.
     *
     * @param playerContext the context and state of the player acquiring the character
     */
    @Override
    public void acquire(PlayerContext playerContext) {
        playerContext.getCurrPlayer().addToTribe(this);
        if(this.strategy != null) this.strategy.effect(playerContext);
    }

    public CharacterType getType() { return type; }
    public int getMinPlayers() { return minPlayers; }

}
