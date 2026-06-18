package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.board.*;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.factory.*;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.WaitingPhaseSnapshot;

/**
 * The initial state of the game, representing the lobby gathering phase.
 * <p>
 * During this phase, the game accepts new players until the required capacity
 * is reached. Upon reaching capacity, this phase takes the responsibility of
 * initializing the game board (via the {@link BoardBuilder}) and transitioning
 * the game state to the first active playing phase ({@link PlaceTotemPhase}).
 */
public class WaitingForPlayersPhase implements GamePhase {
    private final int requiredPlayer;

    /**
     * Constructs a new waiting phase with the number of players.
     *
     * @param requiredPlayer the total number of players needed to start the game
     */
    public WaitingForPlayersPhase(int requiredPlayer) {
        this.requiredPlayer = requiredPlayer;
    }

    /**
     * Registers a new player to the game and assigns them a unique totem.
     * <p>
     * If the addition of this player satisfies the required player count,
     * the game board is automatically generated, the initial setup is performed,
     * and the game transitions to the {@link PlaceTotemPhase}.
     *
     * @param playerContext  the context managing the list of active players
     * @param game           the main game instance to initialize and transition
     * @param playerNickname the nickname of the joining player
     */
    @Override
    public void addPlayer(PlayerContext playerContext, Game game,String playerNickname){
        int index = playerContext.getPlayers().size();
        Totem[] availableTotems = Totem.values();
        Totem assignedTotem = availableTotems[index];
        Player player = new Player(playerNickname, assignedTotem);
        playerContext.addPlayer(player);

        // Game start
        if(playerContext.getPlayers().size() == requiredPlayer){
            game.setBoard(new BoardBuilder().createBoard(this.requiredPlayer));
            game.getBoard().setupBoard(playerContext.getPlayers());
            game.setPhase(new PlaceTotemPhase());
        }
    }

    /**
     * Serializes this phase into a snapshot.
     *
     * @return a {@link WaitingPhaseSnapshot} representing the current lobby state
     */
    @Override
    public PhaseSnapshot toSnapshot() {
        return new WaitingPhaseSnapshot();
    }

}
