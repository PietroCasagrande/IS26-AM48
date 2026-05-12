package it.polimi.ingsw.am48.model.phase;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import it.polimi.ingsw.am48.model.snapshot.PhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.PlaceTotemPhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.PlayerOfferPhaseSnapshot;
import it.polimi.ingsw.am48.model.snapshot.WaitingPhaseSnapshot;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface GamePhase {
    default void addPlayer(PlayerContext playerContext,Game game, String playerNickname){
        throw new InvalidActionException("Azione non consentita in questa fase");
    }
    default List<GameDelta> placeTotem(Game game, Player player, char position){
        throw new InvalidActionException("Azione non consentita in questa fase");
    }
    default List<GameDelta> takeCard(Game game, Player player, String cardId){
        throw new InvalidActionException("Azione non consentita in questa fase");
    }

    PhaseSnapshot toSnapshot();

    public static GamePhase fromSnapshot(PhaseSnapshot snapshot, List<Player> players, int numPlayers) {
        switch (snapshot.getPhaseName()) {
            case "WAITING_FOR_PLAYERS":
                return new WaitingForPlayersPhase(numPlayers);

            case "PLACE_TOTEM": {
                PlaceTotemPhaseSnapshot s = (PlaceTotemPhaseSnapshot) snapshot;
                Set<Player> placed = players.stream()
                        .filter(p -> s.getPlayersPlacedNicknames().contains(p.getNickname()))
                        .collect(Collectors.toSet());
                return new PlaceTotemPhase(placed);
            }

            case "PLAYER_OFFER": {
                PlayerOfferPhaseSnapshot s = (PlayerOfferPhaseSnapshot) snapshot;
                List<Player> actionOrder = s.getActionOrderNicknames().stream()
                        .map(nick -> players.stream()
                                .filter(p -> p.getNickname().equals(nick))
                                .findFirst().orElseThrow())
                        .collect(Collectors.toList());

                Player extraPickPlayer = s.getExtraPickPlayerNickname() != null
                        ? players.stream()
                        .filter(p -> p.getNickname().equals(s.getExtraPickPlayerNickname()))
                        .findFirst().orElseThrow()
                        : null;

                return new PlayerOfferPhase(actionOrder, s.getCurrIdx(), s.getPicksFromUp(),
                        s.getPicksFromDown(), s.getExtraPickActive(),
                        extraPickPlayer, s.getTotemReturned());
            }

            case "END_TURN":
            case "END_GAME":
                throw new IllegalStateException("Fase non ripristinabile: " + snapshot.getPhaseName());

            default:
                throw new IllegalStateException("Fase sconosciuta: " + snapshot.getPhaseName());
        }
    }
}
