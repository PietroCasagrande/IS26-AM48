package it.polimi.ingsw.am48.model.board;

import it.polimi.ingsw.am48.dto.BoardDTO;
import it.polimi.ingsw.am48.model.factory.OfferTurnFactory;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.OfferTurnCardSnapshot;
import it.polimi.ingsw.am48.utils.GameDataLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the Offer Turn Card on the game board.
 * <p>
 * This card defines the turn order for placing totem on the offer track.
 * For the very-first turn, it randoms the order. Each slot, depending on the number of players,
 * assigns food rewards or food penalties. If a player cannot pay the corresponding amount of food,
 * they lose the prestige points as a penalty.
 */
public class OfferTurnCard {
    private final int numPlayers;
    private final List<Player> order;
    private final List<Integer> foodRewards;
    private final int ppPenalty;
    private final List<Integer> initialFoodRewards;

    /**
     * Creates a new {@code OfferTurnCard} to keep track of the current Place Totem order.
     * Initializes the default food rewards configuration depending on the slot position.
     *
     * @param numPlayers  number of players for the current game
     * @param foodRewards food given/subtracted by each slot (must contain exactly 3 positive numbers)
     * @param ppPenalty   prestige points penalty applied when a player cannot pay food (must be a positive number)
     */
    public OfferTurnCard(int numPlayers, List<Integer> foodRewards, int  ppPenalty) {
        this.numPlayers = numPlayers;
        this.foodRewards = List.copyOf(foodRewards);
        this.ppPenalty = ppPenalty;
        this.order = new ArrayList<>();
        this.initialFoodRewards = new ArrayList<>(List.of(2, 3, 3, 4, 4));
    }

    /**
     * Randomizes players' order for the very first turn and assigns initial food reward
     * depending on their slot position.
     *
     * @param players the list of all players, to which assign food
     */
    public void setupOrder(List<Player> players){
        this.order.addAll(players);
        Collections.shuffle(this.order);
        for(int i = 0; i < this.order.size(); i++){
            this.order.get(i).updateFood(this.initialFoodRewards.get(i));
        }
    }

    /**
     * Removes the totem when a player places their totem in the {@code OfferCardTrack}.
     */
    public void removeNextTotem(){
        this.order.removeFirst();
    }

    /**
     * @return the first player in the order queue, or null if the queue is empty
     */
    public Player getNextTotem() {
        if (this.order.isEmpty()) return null;
        return this.order.getFirst();
    }

    /**
     * Replaces the totem when a player completes their Offer Phase:
     * <ul>
     * <li>The first player or the first two players get food (or 0), depending on the number of players.</li>
     * <li>The last player loses food and gets a penalty if they haven't enough food.</li>
     * </ul>
     *
     * @param player the player corresponding to the totem to be returned
     */
    public void returnTotem(Player player){
        this.order.add(player);
        int position = this.order.size();
        player.setExtraFoodRight(false);    // set the extra food right to default value to avoid errors from the previous turn

        if(position <= 2) {
            player.updateFood(this.foodRewards.get(position-1));
            if(foodRewards.get(position-1) != 0) player.setExtraFoodRight(true);    // player deserves extra food only if he gets food from the offer turn card (not when he gets 0)
        }

        if(position == this.numPlayers) {
            player.payFood(this.foodRewards.getLast(), this.ppPenalty);
        }
    }

    /**
     * Creates a snapshot of the current state of the turn order card for network synchronization.
     *
     * @return an {@link OfferTurnCardSnapshot} containing the ordered list of players
     */
    public OfferTurnCardSnapshot toSnapshot() {
        List<String> totemOrder = order.stream()
                .map(Player::getNickname)
                .toList();

        return new OfferTurnCardSnapshot(totemOrder);
    }

    /**
     * Recreates the {@code OfferTurnCard} with its food rewards, point penalty and the previous turn order,
     * from a network snapshot and the base game data.
     *
     * @param snapshot  the serialized state containing the saved totem positions, food rewards and point penalty
     * @param players   the list of active {@link Player} instances to map the saved nicknames to actual objects
     * @return          the restored {@code OfferTurnCard}
     */
    public static OfferTurnCard fromSnapshot(OfferTurnCardSnapshot snapshot, List<Player> players) {
        BoardDTO dto = new GameDataLoader().loadData();
        OfferTurnCard card = new OfferTurnFactory(dto.offerTurnCard).createCards(players.size()).getFirst();

        for (String nickname : snapshot.getTotemOrder()) {
            Player player = players.stream()
                    .filter(p -> p.getNickname().equals(nickname))
                    .findFirst().orElseThrow();
            card.order.add(player);
        }

        return card;
    }

    public List <Player> getPlaceOrder(){return this.order; }

    // Getters for testing
    public int getNumPlayers(){
        return this.numPlayers;
    }

    public List<Integer> getFoodRewards(){
        return this.foodRewards;
    }

    public int getPpPenalty(){
        return this.ppPenalty;
    }
}
