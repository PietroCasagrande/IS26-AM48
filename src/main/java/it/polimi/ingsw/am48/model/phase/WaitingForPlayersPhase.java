package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.WaitingPhaseSnapshot;

import java.util.List;

public class WaitingForPlayersPhase implements GamePhase {
    private int requiredPlayer;

    public WaitingForPlayersPhase(int requiredPlayer) {
        this.requiredPlayer = requiredPlayer;
    }

    @Override
    public void addPlayer(PlayerContext playerContext, Game game,String playerNickname){
        int index = playerContext.getPlayers().size();
        Totem[] availableTotems = Totem.values();
        Totem assignedTotem = availableTotems[index];
        Player player = new Player(playerNickname, assignedTotem);
        playerContext.addPlayer(player);
        if (index == requiredPlayer) {
            game.getBoard().setupBoard(playerContext.getPlayers());
            List <Player> casuallyOrderedPlayers = game.getBoard().getPlaceOrder();
            game.setPhase(new PlaceTotemPhase(casuallyOrderedPlayers));
        }
    }

    @Override
    public PhaseSnapshot toSnapshot() {
        return new WaitingPhaseSnapshot();
    }

}
