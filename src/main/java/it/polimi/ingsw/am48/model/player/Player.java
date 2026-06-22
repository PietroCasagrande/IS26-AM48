package it.polimi.ingsw.am48.model.player;

import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.Artifact;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.snapshot.PlayerSnapshot;

import java.util.Map;

/**
 * Represents a single player in the game, identified by a unique nickname and a totem color.
 * <p>
 * A player's entire mutable state &ndash; resources, acquired characters and buildings,
 * prestige points and the various bonuses granted by card effects &ndash; is held in their
 * {@link Tribe}. The {@code Player} class acts mainly as a façade over the tribe, exposing
 * its data and operations to the rest of the model (most notably to the card strategies),
 * so that callers never have to chain through {@link #getTribe()} directly.
 *
 * @see Tribe
 */
public class Player {
    private final String nickname;
    private final Totem totem;
    private final Tribe tribe;

    /**
     * Creates a new player with a fresh, empty tribe.
     *
     * @param nickname the unique nickname identifying the player
     * @param totem    the totem color assigned to the player
     */
    public Player(String nickname, Totem totem){
        this.nickname = nickname;
        this.totem = totem;
        this.tribe = new Tribe();
    }

    /**
     * Creates a player around an already reconstructed tribe.
     * <p>
     * Reserved for {@link #fromSnapshot(PlayerSnapshot, Map)}, which rebuilds the tribe from
     * a snapshot before wrapping it into a player.
     *
     * @param nickname the player's nickname
     * @param totem    the player's totem color
     * @param tribe    the tribe to associate with the player
     */
    private Player(String nickname, Totem totem, Tribe tribe) {
        this.nickname = nickname;
        this.totem = totem;
        this.tribe = tribe;
    }

    // getter nickname e totem

    public String getNickname(){ return nickname; }
    public Totem getTotem(){ return totem; }
    // getters and setters directly on player, without going through getTribe()

    public int getPoints() { return this.tribe.getCurrentPrestigePoints(); }
    public int getFood() { return this.tribe.getCurrentFood(); }
    public void updatePoints(int points) { this.tribe.updateCurrentPrestigePoints(points); }
    public void updateFood(int food) { this.tribe.updateCurrentFood(food); }
    // method used in the strategies to update the tribe's statistics

    public Tribe getTribe() { return this.tribe; }    // maybe no longer needed now that we added getters for every attribute
    public int getTotalCharacters() { return this.tribe.getTotalCharacters(); }
    /**
     * Makes the player pay an amount of food, converting any shortfall into a prestige
     * point loss.
     * <p>
     * Delegates to {@link Tribe#payFood(int, int)}: if the tribe does not hold enough food,
     * the missing units are paid in prestige points instead, at a rate of {@code ppPerFood}
     * points per unit.
     *
     * @param food      the amount of food to pay (must be positive)
     * @param ppPerFood the prestige points lost for each unit of food that cannot be paid (must be positive)
     */
    public void payFood(int food, int ppPerFood){this.tribe.payFood(food, ppPerFood); }
    // to avoid chaining getTribe().set(), otherwise the Law of Demeter is violated

    public void addToTribe(CharacterCard card) { this.tribe.addToTribe(card); }
    public void addToTribe(BuildingCard card) { this.tribe.addToTribe(card); }
    public int getFoodDiscount() { return tribe.getFoodDiscount(); }
    public int getBuildingDiscount() { return tribe.getBuildingDiscount(); }
    public int getShamanStars() { return tribe.getShamanStars(); }
    public boolean isShamanSafe() { return tribe.isShamanSafe(); }
    public boolean deservesDoubleShamanPp() { return tribe.deservesDoubleShamanPp();}
    public boolean deservesExtraFood() { return tribe.deservesExtraFood(); }
    public boolean deservesExtraPick() { return tribe.deservesExtraPick(); }
    public int getBuilderPoints() { return tribe.getBuilderPoints(); }
    public int getBuildingPoints() { return tribe.getBuildingPoints(); }
    public Map<Artifact, Integer> getArtifacts() { return tribe.getArtifacts(); }
    public void updateFoodDiscount(int amount) { this.tribe.updateFoodDiscount(amount); }
    public void updateBuildingDiscount(int amount) { this.tribe.updateBuildingDiscount(amount); }
    public void updateShamanStars(int amount) { this.tribe.updateShamanStars(amount); }
    public void setShamanSafety(){ this.tribe.setShamanSafety(); }
    public void setShamanDoubling() {this.tribe.setShamanDoubling(); }
    public void setExtraFoodRight(boolean extraFoodRight) {this.tribe.setExtraFoodRight(extraFoodRight);}
    public void setExtraPickRight() {this.tribe.setExtraPickRight();}
    public void updateBuilderPoints(int amount) { this.tribe.updateBuilderPoints(amount); }
    public void updateBuildingPoints(int amount) { this.tribe.updateBuildingPoints(amount); }
    public void addArtifact(Artifact artifact) { this.tribe.addArtifact(artifact); }

    /**
     * Captures the player's current state into an immutable snapshot.
     *
     * @return a {@link PlayerSnapshot} holding the nickname, totem and a snapshot of the tribe
     */
    public PlayerSnapshot toSnapshot(){
        return new PlayerSnapshot(
                nickname,
                totem.name(),
                tribe.toSnapshot()
        );
    }

    /**
     * Rebuilds a player from a snapshot, resolving its cards through the given card map.
     *
     * @param snapshot the snapshot describing the player's persisted state
     * @param cardMap  a lookup from card id to the corresponding {@link Card} instance,
     *                 used to restore the tribe's characters and buildings
     * @return the reconstructed {@link Player}
     */
    public static Player fromSnapshot(PlayerSnapshot snapshot, Map<String, Card> cardMap) {
        Totem totem = Totem.valueOf(snapshot.getTotemColor());
        Tribe tribe = Tribe.fromSnapshot(snapshot.getTribe(), cardMap);
        return new Player(snapshot.getNickname(), totem, tribe);
    }

}
