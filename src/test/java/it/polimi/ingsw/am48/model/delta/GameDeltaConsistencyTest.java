package it.polimi.ingsw.am48.model.delta;

import it.polimi.ingsw.am48.dto.BoardDTO;
import it.polimi.ingsw.am48.dto.JoinResult;
import it.polimi.ingsw.am48.model.board.Board;
import it.polimi.ingsw.am48.model.board.OfferCard;
import it.polimi.ingsw.am48.model.card.Card;

import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.game.GameManager;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.model.snapshot.PlayerSnapshot;
import it.polimi.ingsw.am48.network.client.ClientGameState;
import it.polimi.ingsw.am48.network.client.ClientModel;
import it.polimi.ingsw.am48.network.client.ClientPlayerState;
import it.polimi.ingsw.am48.utils.GameDataLoader;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GameDeltaConsistencyTest {

    /*
    @Test
    public void deltasAppliedMatchServerStateAfterOneRound() {
        GameManager gameManager = new GameManager();

        //joining game
        JoinResult joinResult1 = gameManager.joinGame(2, "Alice");
        JoinResult joinResult2 = gameManager.joinGame(2, "Bob");

        Game game = gameManager.getGameByNickname("Alice");
        List<Player> order = game.getBoard().getPlaceOrder();

        assertNotNull(joinResult2);
        GameSnapshot initialSnapshot = game.toSnapshot();
        assertNotNull(initialSnapshot.getBoard(), "Board should be initialized after all players join");

        // client initialized with the server snapshot
        ClientModel clientModel = new ClientModel();
        clientModel.setInitialState(initialSnapshot);

        List<GameDelta> allDeltas = new ArrayList<>();

        for(int i = 1 ; i < 10 ; i++) game.incrementTurn();

        //place totem phase
        allDeltas.addAll(gameManager.placeTotem(order.getFirst().getNickname(), 'E'));
        allDeltas.addAll(gameManager.placeTotem(order.getFirst().getNickname(), 'C'));

        //take card
        List<Player> pickOrder = game.getBoard().getPickOrder();
        List<Card> upperList = new ArrayList<>(game.getBoard().getTribeShowed().getUpperList());
        List<Card> lowerList = new ArrayList<>(game.getBoard().getTribeShowed().getLowerList());

        allDeltas.addAll(gameManager.takeCard(pickOrder.getFirst().getNickname(), upperList.getFirst().getCardId()));
        allDeltas.addAll(gameManager.takeCard(pickOrder.get(1).getNickname(), lowerList.getFirst().getCardId()));
        allDeltas.addAll(gameManager.takeCard(pickOrder.get(1).getNickname(), upperList.get(1).getCardId()));

        // now we check that the ClientGameState in ClientModel matches the state of the model on the server

        for (GameDelta gameDelta : allDeltas) {
            clientModel.applyDelta(gameDelta);
        }

        GameSnapshot finalSnapshot = game.toSnapshot();
        ClientGameState finalStateFromSnapshot = ClientGameState.fromSnapshot(finalSnapshot);

        assertThat(clientModel.getState().getGameId()).as("gameId").isEqualTo(finalStateFromSnapshot.getGameId());
        assertThat(clientModel.getState().getCurrentTurn() == finalStateFromSnapshot.getCurrentTurn());
        assertThat(clientModel.getState().getCurrentPhase().equals(finalStateFromSnapshot.getCurrentPhase()));
        assertThat(clientModel.getState().getWinnerNickname().equals(finalStateFromSnapshot.getWinnerNickname()));

        // 2. String lists (order matters for the card rows)
        assertThat(clientModel.getState().getUpperRowCardIds()).as("upperRowCardIds").containsExactlyElementsOf(finalStateFromSnapshot.getUpperRowCardIds());
        assertThat(clientModel.getState().getLowerRowCardIds()).as("lowerRowCardIds").containsExactlyElementsOf(finalStateFromSnapshot.getLowerRowCardIds());
        assertThat(clientModel.getState().getBuildingUpperIds()).as("buildingUpperIds").containsExactlyElementsOf(finalStateFromSnapshot.getBuildingUpperIds());
        assertThat(clientModel.getState().getBuildingLowerIds()).as("buildingLowerIds").containsExactlyElementsOf(finalStateFromSnapshot.getBuildingLowerIds());
        assertThat(clientModel.getState().getOfferTurnCardOrder()).as("offerTurnCardOrder").containsExactlyElementsOf(finalStateFromSnapshot.getOfferTurnCardOrder());

        // 4. Players comparison
        assertThat(clientModel.getState().getPlayers()).as("players map").hasSize(finalStateFromSnapshot.getPlayers().size());

        clientModel.getState().getPlayers().forEach((nickname, actualPlayer) -> {
            ClientPlayerState expectedPlayer = finalStateFromSnapshot.getPlayers().get(nickname);
            assertThat(expectedPlayer).as("Player with nickname " + nickname).isNotNull();

            // Call to the helper method for the detailed player comparison
            comparePlayerStates(actualPlayer, expectedPlayer);
        });
    }

    private void comparePlayerStates(ClientPlayerState actualP, ClientPlayerState expectedP) {
        String name = actualP.getNickname();

        assertThat(actualP.getNickname()).as(name + ": nickname").isEqualTo(expectedP.getNickname());
        assertThat(actualP.getTotemColor()).as(name + ": totemColor").isEqualTo(expectedP.getTotemColor());
        assertThat(actualP.getFood()).as(name + ": food").isEqualTo(expectedP.getFood());
        assertThat(actualP.getPoints()).as(name + ": points").isEqualTo(expectedP.getPoints());

        // Player's card lists
        assertThat(actualP.getCharacterCardIds())
                .as(name + ": characterCards")
                .containsExactlyInAnyOrderElementsOf(expectedP.getCharacterCardIds());

        assertThat(actualP.getBuildingCardIds())
                .as(name + ": buildingCards")
                .containsExactlyInAnyOrderElementsOf(expectedP.getBuildingCardIds());
    }
    */
}