package it.polimi.ingsw.am48.dto;

import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

/**
 * An immutable data transfer object representing the server's response
 * when a player successfully joins a game or lobby.
 * <p>
 * This record encapsulates both the state synchronization payload and the
 * immediate status of the match, allowing the client to correctly render
 * the UI and determine if the gameplay phase should begin.
 *
 * @param snapshot    the serialized current state of the game, used to synchronize the client's local view
 * @param gameStarted a flag indicating whether the match has officially begun
 *                    (e.g., because this specific join reached the required player count)
 */
public record JoinResult(GameSnapshot snapshot, boolean gameStarted) { }