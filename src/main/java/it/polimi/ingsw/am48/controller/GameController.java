package it.polimi.ingsw.am48.controller;

import it.polimi.ingsw.am48.dto.JoinResult;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.game.GameManager;
import it.polimi.ingsw.am48.model.game.ModelInterface;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

import java.util.List;

/**
 * Thin controller layer that mediates between the network layer
 * (via Socket or RMI) and the game model.
 * It receives client actions (join, place totem, take card),
 * delegates them to the {@link ModelInterface}, and returns the
 * resulting {@link GameDelta deltas} or other response objects.
 */
public class GameController {
    private final ModelInterface model;

    /**
     * Creates a new {@code GameController} with the given model implementation.
     *
     * @param model the {@link ModelInterface} instance that handles game logic
     */
    public GameController(ModelInterface model) {
        this.model = model;
    }

    /**
     * Handles a player's request to join a game lobby.
     *
     * @param numPlayers the number of players configured for the game
     * @param nickname   the player's chosen nickname
     * @return a {@link JoinResult} containing the initial {@link GameSnapshot}
     * and a flag indicating whether the game has started
     */
    public JoinResult handleJoinGame(int numPlayers, String nickname){
        return model.joinGame(numPlayers, nickname);
    }

    /**
     * Handles a player's request to place their totem on the offer track
     * at the specified position.
     *
     * @param nickname the player placing the totem
     * @param position the position on the offer track
     * @return a list of {@link GameDelta} describing the resulting state changes
     */
    public List<GameDelta> handlePlaceTotem(String nickname, char position){
        List<GameDelta> deltas = model.placeTotem(nickname, position);
        return deltas;
    }

    /**
     * Handles a player's request to take a card (character or building)
     * identified by its card ID.
     *
     * @param nickname the player taking the card
     * @param cardId   the unique identifier of the card to take
     * @return a list of {@link GameDelta} describing the resulting state changes
     */
    public List<GameDelta> handleTakeCard(String nickname, String cardId){
        List<GameDelta> deltas = model.takeCard(nickname, cardId);
        return deltas;
    }

    /**
     * Handles a client disconnection by notifying the model and
     * returning the list of remaining players to be notified.
     *
     * @param nickname the nickname of the disconnecting player
     * @return a list of nicknames of other players still in the game
     */
    public List<String> handleClientDisconnect(String nickname) {
        return model.handleClientDisconnect(nickname);
    }

    /**
     * Returns the list of players currently in the same game as the
     * given nickname.
     *
     * @param nickname a player whose game to look up
     * @return a list of player nicknames in the same game
     */
    public List<String> getPlayersInGame(String nickname) {
        return model.getPlayersInGame(nickname);
    }
}
