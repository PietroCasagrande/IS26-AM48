package it.polimi.ingsw.am48.model.fullMatch;

import it.polimi.ingsw.am48.dto.JoinResult;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.game.GameManager;
import it.polimi.ingsw.am48.model.player.Player;

import java.util.List;
import java.util.Scanner;

public class TwoPlayersMatchTest {
    private final GameManager gameManager = new GameManager();

    public void main(){

        //joining game
        JoinResult joinResult1 = gameManager.joinGame(2, "Alice");
        JoinResult joinResult2 = gameManager.joinGame(2, "Bob");

        //getting game references
        Game game = gameManager.getGameByNickname("Alice");
        List<Player> order = game.getBoard().getPlaceOrder();
        game.getBoard().getPlaceOrder().forEach(p-> System.out.println(p.getNickname()));

        //check initial food
        order.forEach(p-> System.out.println(p.getTribe().getCurrentFood()));

        //starting game
        System.out.println("===================================== MESOS =====================================");
        System.out.println("\nStarting Game");

        // ========================================== TURN ================================================

        for(int j=0; j<10; j++){
            System.out.println("===================================== TURN " + (j+1) + " =====================================");
            System.out.println("===================================== BOARD =====================================");
            System.out.println("Fila sopra:");
            game.getBoard().getTribeShowed().getUpperList().forEach(c-> System.out.println(c.getCardId()));
            System.out.println("\nFila sotto:");
            game.getBoard().getTribeShowed().getLowerList().forEach(c-> System.out.println(c.getCardId()));
            System.out.println("\nEdifici sopra:");
            game.getBoard().getBuildingShowed().getUpperList().forEach(c-> System.out.println(c.getCardId()));
            System.out.println("\nEdifici sotto:");
            game.getBoard().getBuildingShowed().getLowerList().forEach(c-> System.out.println(c.getCardId()));
            System.out.println("===========================================================================");

            //place totem input
            System.out.println("Inserisci la posizione in cui piazzare il totem: "); //IMPORTANTE: scrivete entrambe le tessere (es C F)
            Scanner sc = new Scanner(System.in);
            char c1 = sc.next().charAt(0);
            char c2 = sc.next().charAt(0);
            sc.nextLine();

            //place totem phase
            gameManager.placeTotem(order.getFirst().getNickname(), c1);
            gameManager.placeTotem(order.getFirst().getNickname(), c2);

            //take card: saves number of inputs to read
            List<Player> pickOrder = game.getBoard().getPickOrder();
            int picks1 = game.getBoard().findTrackPosition(pickOrder.getFirst()).getNumUp() + game.getBoard().findTrackPosition(pickOrder.getFirst()).getNumDown();
            int picks2 = game.getBoard().findTrackPosition(pickOrder.get(1)).getNumUp() + game.getBoard().findTrackPosition(pickOrder.get(1)).getNumDown();

            //take card: getting card ids
            for(int i = 0; i < picks1; i++){
                System.out.println("===========================================================================");
                System.out.println("Turno " + (i+1) + " di " + pickOrder.getFirst().getNickname());
                System.out.println("Inserisci la carta che vuoi prendere: ");
                String s1 = sc.nextLine();
                gameManager.takeCard(pickOrder.getFirst().getNickname(), s1);

                //player 1 tribe update
                System.out.println("===================================== PLAYER 1 STATS =====================================");
                System.out.println("food: " + pickOrder.getFirst().getFood());
                System.out.println("pp: " + pickOrder.getFirst().getPoints());
                System.out.println("builder points: " + pickOrder.getFirst().getBuilderPoints());
                System.out.println("stars: " + pickOrder.getFirst().getShamanStars());
                System.out.println("artifacts: " + pickOrder.getFirst().getArtifacts());
                System.out.println("characters: " + pickOrder.getFirst().getTotalCharacters());
                System.out.println("building discount: " + pickOrder.getFirst().getBuildingDiscount());
                System.out.println("buildings points: " + pickOrder.getFirst().getBuildingPoints());
                System.out.println("food discount: " + pickOrder.getFirst().getFoodDiscount());

                //player 2 tribe update
                System.out.println("===================================== PLAYER 2 STATS =====================================");
                System.out.println("food: " + pickOrder.get(1).getFood());
                System.out.println("pp: " + pickOrder.get(1).getPoints());
                System.out.println("builder points: " + pickOrder.get(1).getBuilderPoints());
                System.out.println("stars: " + pickOrder.get(1).getShamanStars());
                System.out.println("artifacts: " + pickOrder.get(1).getArtifacts());
                System.out.println("characters: " + pickOrder.get(1).getTotalCharacters());
                System.out.println("building discount: " + pickOrder.get(1).getBuildingDiscount());
                System.out.println("buildings points: " + pickOrder.get(1).getBuildingPoints());
                System.out.println("food discount: " + pickOrder.get(1).getFoodDiscount());

                //board update
                System.out.println("===================================== BOARD =====================================");
                System.out.println("Fila sopra:");
                game.getBoard().getTribeShowed().getUpperList().forEach(c-> System.out.println(c.getCardId()));
                System.out.println("\nFila sotto:");
                game.getBoard().getTribeShowed().getLowerList().forEach(c-> System.out.println(c.getCardId()));
                System.out.println("\nEdifici sopra:");
                game.getBoard().getBuildingShowed().getUpperList().forEach(c-> System.out.println(c.getCardId()));
                System.out.println("\nEdifici sotto:");
                game.getBoard().getBuildingShowed().getLowerList().forEach(c-> System.out.println(c.getCardId()));
            }

            for(int i = 0; i < picks2; i++){
                System.out.println("===========================================================================");
                System.out.println("Turno " + (i+1) + " di " + pickOrder.get(1).getNickname());
                System.out.println("Inserisci la carta che vuoi prendere: ");
                String s2 = sc.nextLine();
                gameManager.takeCard(pickOrder.get(1).getNickname(), s2);

                //player 1 tribe update
                System.out.println("===================================== PLAYER 1 STATS =====================================");
                System.out.println("food: " + pickOrder.getFirst().getFood());
                System.out.println("pp: " + pickOrder.getFirst().getPoints());
                System.out.println("builder points: " + pickOrder.getFirst().getBuilderPoints());
                System.out.println("stars: " + pickOrder.getFirst().getShamanStars());
                System.out.println("artifacts: " + pickOrder.getFirst().getArtifacts());
                System.out.println("characters: " + pickOrder.getFirst().getTotalCharacters());
                System.out.println("building discount: " + pickOrder.getFirst().getBuildingDiscount());
                System.out.println("buildings points: " + pickOrder.getFirst().getBuildingPoints());
                System.out.println("food discount: " + pickOrder.getFirst().getFoodDiscount());

                //player 2 tribe update
                System.out.println("===================================== PLAYER 2 STATS =====================================");
                System.out.println("food: " + pickOrder.get(1).getFood());
                System.out.println("pp: " + pickOrder.get(1).getPoints());
                System.out.println("builder points: " + pickOrder.get(1).getBuilderPoints());
                System.out.println("stars: " + pickOrder.get(1).getShamanStars());
                System.out.println("artifacts: " + pickOrder.get(1).getArtifacts());
                System.out.println("characters: " + pickOrder.get(1).getTotalCharacters());
                System.out.println("building discount: " + pickOrder.get(1).getBuildingDiscount());
                System.out.println("buildings points: " + pickOrder.get(1).getBuildingPoints());
                System.out.println("food discount: " + pickOrder.get(1).getFoodDiscount());

                //board update
                System.out.println("===================================== BOARD =====================================");
                System.out.println("Fila sopra:");
                game.getBoard().getTribeShowed().getUpperList().forEach(c-> System.out.println(c.getCardId()));
                System.out.println("\nFila sotto:");
                game.getBoard().getTribeShowed().getLowerList().forEach(c-> System.out.println(c.getCardId()));
                System.out.println("\nEdifici sopra:");
                game.getBoard().getBuildingShowed().getUpperList().forEach(c-> System.out.println(c.getCardId()));
                System.out.println("\nEdifici sotto:");
                game.getBoard().getBuildingShowed().getLowerList().forEach(c-> System.out.println(c.getCardId()));

            }
        }
    }
}
