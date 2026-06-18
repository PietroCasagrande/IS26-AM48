package it.polimi.ingsw.am48.network.client;

import it.polimi.ingsw.am48.model.snapshot.PlayerSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Client-side replica of a single player's state, built from a
 * {@link PlayerSnapshot} and updated incrementally via
 * {@link it.polimi.ingsw.am48.model.delta.GameDelta} objects.
 * Holds the player's nickname, totem color, food, prestige points,
 * and the lists of character and building card IDs they own.
 */
public class ClientPlayerState {
    private String nickname;
    private String totemColor;
    private int food;
    private int points;
    private List<String> characterCardIds;
    private List<String> buildingCardIds;

    /**
     * Constructs a {@code ClientPlayerState} from a server-provided
     * {@link PlayerSnapshot}. Called when the client first joins
     * or reconnects to a game.
     *
     * @param snapshot the player snapshot received from the server
     * @return a fully populated {@code ClientPlayerState}
     */
    public static ClientPlayerState fromSnapshot(PlayerSnapshot snapshot) {
        ClientPlayerState state = new ClientPlayerState();
        state.nickname = snapshot.getNickname();
        state.totemColor = snapshot.getTotemColor();
        state.food = snapshot.getTribe().getCurrentFood();
        state.points = snapshot.getTribe().getCurrentPrestigePoints();
        state.characterCardIds = new ArrayList<>(snapshot.getTribe().getCharacterCardIds());
        state.buildingCardIds = new ArrayList<>(snapshot.getTribe().getBuildingCardIds());
        return state;
    }

    /**
     * Sets the player's food count.
     *
     * @param food the new food value
     */
    public void setFood(int food) { this.food = food; }

    /**
     * Sets the player's prestige points.
     *
     * @param points the new points value
     */
    public void setPoints(int points) { this.points = points; }

    /**
     * Adds a character card ID to the player's collection.
     *
     * @param cardId the character card identifier
     */
    public void addCharacterCard(String cardId) { characterCardIds.add(cardId); }

    /**
     * Adds a building card ID to the player's collection.
     *
     * @param cardId the building card identifier
     */
    public void addBuildingCard(String cardId) { buildingCardIds.add(cardId); }

    /**
     * Returns the player's nickname.
     *
     * @return the nickname
     */
    public String getNickname() { return nickname; }

    /**
     * Returns the player's totem color.
     *
     * @return the totem color string
     */
    public String getTotemColor() { return totemColor; }

    /**
     * Returns the player's current food count.
     *
     * @return the food value
     */
    public int getFood() { return food; }

    /**
     * Returns the player's current prestige points.
     *
     * @return the points value
     */
    public int getPoints() { return points; }

    /**
     * Returns an unmodifiable view of the character card IDs owned by the player.
     *
     * @return the character card ID list
     */
    public List<String> getCharacterCardIds() { return Collections.unmodifiableList(characterCardIds); }

    /**
     * Returns an unmodifiable view of the building card IDs owned by the player.
     *
     * @return the building card ID list
     */
    public List<String> getBuildingCardIds() { return Collections.unmodifiableList(buildingCardIds); }
}