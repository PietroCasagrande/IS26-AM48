package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.model.board.*;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.factory.*;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.WaitingPhaseSnapshot;

public class WaitingForPlayersPhase implements GamePhase {
    private final int requiredPlayer;

    public WaitingForPlayersPhase(int requiredPlayer) {
        this.requiredPlayer = requiredPlayer;
    }

    // TODO: c'è da sistemare l'ordine di alcune chiamate, non ha senso il confronto con l'index non aggiornato
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

    @Override
    public PhaseSnapshot toSnapshot() {
        return new WaitingPhaseSnapshot();
    }

}
