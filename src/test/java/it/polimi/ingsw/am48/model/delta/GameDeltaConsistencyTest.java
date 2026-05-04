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

//    @Test
//    public void deltasAppliedMatchServerStateAfterOneRound() {
//        GameManager gameManager = new GameManager();
//
//        //joining game
//        JoinResult joinResult1 = gameManager.joinGame(2, "Alice");
//        JoinResult joinResult2 = gameManager.joinGame(2, "Bob");
//
//        Game game = gameManager.getGameByNickname("Alice");
//        List<Player> order = game.getBoard().getPlaceOrder();
//
//        assertNotNull(joinResult2);
//        GameSnapshot initialSnapshot = game.toSnapshot();
//        assertNotNull(initialSnapshot.getBoard(), "Board should be initialized after all players join");
//
//        // client initialized with the server snapshot
//        ClientModel clientModel = new ClientModel();
//        clientModel.setInitialState(initialSnapshot);
//
//        List<GameDelta> allDeltas = new ArrayList<>();
//
//        //place totem phase
//        allDeltas.addAll(gameManager.placeTotem(order.getFirst().getNickname(), 'E'));
//        allDeltas.addAll(gameManager.placeTotem(order.getFirst().getNickname(), 'C'));
//
//        //take card
//        List<Player> pickOrder = game.getBoard().getPickOrder();
//        List<Card> upperList = new ArrayList<>(game.getBoard().getTribeShowed().getUpperList());
//        List<Card> lowerList = new ArrayList<>(game.getBoard().getTribeShowed().getLowerList());
//
//        allDeltas.addAll(gameManager.takeCard(pickOrder.getFirst().getNickname(), upperList.getFirst().getCardId()));
//        allDeltas.addAll(gameManager.takeCard(pickOrder.get(1).getNickname(), lowerList.getFirst().getCardId()));
//        allDeltas.addAll(gameManager.takeCard(pickOrder.get(1).getNickname(), upperList.get(1).getCardId()));
//
//        // ora controlliamo che lo stato del ClientGameState di ClientModel sia uguale allo stato del model sul server
//
//        for (GameDelta gameDelta : allDeltas) {
//            clientModel.applyDelta(gameDelta);
//        }
//
//        GameSnapshot finalSnapshot = game.toSnapshot();
//        ClientGameState finalStateFromSnapshot = ClientGameState.fromSnapshot(finalSnapshot);
//
//        // non controlliamo winnerNickname, turn e phase perchè attualmente non vengono mandati al ClientModel, forse li toglieremo
//        assertThat(clientModel.getState().getGameId()).as("gameId").isEqualTo(finalStateFromSnapshot.getGameId());
//
//        // 2. Liste di stringhe (l'ordine è importante per le righe delle carte)
//        assertThat(clientModel.getState().getUpperRowCardIds()).as("upperRowCardIds").containsExactlyElementsOf(finalStateFromSnapshot.getUpperRowCardIds());
//        assertThat(clientModel.getState().getLowerRowCardIds()).as("lowerRowCardIds").containsExactlyElementsOf(finalStateFromSnapshot.getLowerRowCardIds());
//        assertThat(clientModel.getState().getBuildingUpperIds()).as("buildingUpperIds").containsExactlyElementsOf(finalStateFromSnapshot.getBuildingUpperIds());
//        assertThat(clientModel.getState().getBuildingLowerIds()).as("buildingLowerIds").containsExactlyElementsOf(finalStateFromSnapshot.getBuildingLowerIds());
//        assertThat(clientModel.getState().getOfferTurnCardOrder()).as("offerTurnCardOrder").containsExactlyElementsOf(finalStateFromSnapshot.getOfferTurnCardOrder());
//
//        // 4. Confronto dei Players
//        assertThat(clientModel.getState().getPlayers()).as("Mappa players").hasSize(finalStateFromSnapshot.getPlayers().size());
//
//        clientModel.getState().getPlayers().forEach((nickname, actualPlayer) -> {
//            ClientPlayerState expectedPlayer = finalStateFromSnapshot.getPlayers().get(nickname);
//            assertThat(expectedPlayer).as("Player con nickname " + nickname).isNotNull();
//
//            // Chiamata al metodo helper per il confronto dettagliato del player
//            comparePlayerStates(actualPlayer, expectedPlayer);
//        });
//    }
//
//    private void comparePlayerStates(ClientPlayerState actualP, ClientPlayerState expectedP) {
//        String name = actualP.getNickname();
//
//        assertThat(actualP.getNickname()).as(name + ": nickname").isEqualTo(expectedP.getNickname());
//        assertThat(actualP.getTotemColor()).as(name + ": totemColor").isEqualTo(expectedP.getTotemColor());
//        assertThat(actualP.getFood()).as(name + ": food").isEqualTo(expectedP.getFood());
//        assertThat(actualP.getPoints()).as(name + ": points").isEqualTo(expectedP.getPoints());
//
//        // Liste delle carte del player
//        assertThat(actualP.getCharacterCardIds())
//                .as(name + ": characterCards")
//                .containsExactlyInAnyOrderElementsOf(expectedP.getCharacterCardIds());
//
//        assertThat(actualP.getBuildingCardIds())
//                .as(name + ": buildingCards")
//                .containsExactlyInAnyOrderElementsOf(expectedP.getBuildingCardIds());
//    }

    
}