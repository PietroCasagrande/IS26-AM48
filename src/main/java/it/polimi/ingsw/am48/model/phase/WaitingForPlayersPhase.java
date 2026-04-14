package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.WaitingPhaseSnapshot;

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
        if (index == requiredPlayer) {
            game.getBoard().setupBoard(playerContext.getPlayers());
            game.setPhase(new PlaceTotemPhase());

        }
        playerContext.addPlayer(player);
    }

    @Override
    public PhaseSnapshot toSnapshot() {
        return new WaitingPhaseSnapshot();
    }

}
