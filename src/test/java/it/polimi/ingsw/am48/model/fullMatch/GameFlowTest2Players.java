package it.polimi.ingsw.am48.model.fullMatch;

import it.polimi.ingsw.am48.exception.InvalidActionException;
import it.polimi.ingsw.am48.model.game.Game;
import it.polimi.ingsw.am48.model.game.GameManager;
import it.polimi.ingsw.am48.model.player.Player;

import java.util.List;
import java.util.Scanner;

public class GameFlowTest2Players {

    private static void printStats(Player p) {
        System.out.println("=== STATS: " + p.getNickname() + " ===");
        System.out.println("food: "              + p.getFood());
        System.out.println("pp: "                + p.getPoints());
        System.out.println("builder points: "    + p.getBuilderPoints());
        System.out.println("stars: "             + p.getShamanStars());
        System.out.println("artifacts: "         + p.getArtifacts());
        System.out.println("characters: "        + p.getTotalCharacters());
        System.out.println("building discount: " + p.getBuildingDiscount());
        System.out.println("buildings points: "  + p.getBuildingPoints());
        System.out.println("food discount: "     + p.getFoodDiscount());
    }

    private static void printBoard(Game game) {
        System.out.println("=== BOARD ===");
        System.out.println("Fila sopra:");
        game.getBoard().getTribeShowed().getUpperList().forEach(c -> System.out.println(c.getCardId()));
        System.out.println("Fila sotto:");
        game.getBoard().getTribeShowed().getLowerList().forEach(c -> System.out.println(c.getCardId()));
        System.out.println("Edifici sopra:");
        game.getBoard().getBuildingShowed().getUpperList().forEach(c -> System.out.println(c.getCardId()));
        System.out.println("Edifici sotto:");
        game.getBoard().getBuildingShowed().getLowerList().forEach(c -> System.out.println(c.getCardId()));
    }

    public static void main(String[] args) {
        GameManager gameManager = new GameManager();
        gameManager.joinGame(2, "Alice");
        gameManager.joinGame(2, "Bob");

        Game game = gameManager.getGameByNickname("Alice");
        List<Player> placeOrder = game.getBoard().getPlaceOrder();

        System.out.println("=== ORDINE DI PIAZZAMENTO INIZIALE ===");
        //placeOrder.forEach(p -> System.out.println(p.getNickname() + " | food: " + p.getTribe().getCurrentFood()));
        System.out.println("Primo a piazzare il totem: " + placeOrder.getFirst().getNickname());

        Scanner sc = new Scanner(System.in);

        for (int j = 0; j < 10; j++) {
            System.out.println("\n========= TURN " + (j + 1) + " =========");
            printBoard(game);

            List<Player> currentPlaceOrder = game.getBoard().getPlaceOrder();
            System.out.println("Primo a piazzare il totem: " + placeOrder.getFirst().getNickname());

            boolean placed1 = false;
            while (!placed1) {
                System.out.print("Posizione 1: ");
                char c1 = sc.next().charAt(0);
                try {
                    gameManager.placeTotem(currentPlaceOrder.getFirst().getNickname(), c1);
                    placed1 = true;
                } catch (InvalidActionException | IllegalStateException | IllegalArgumentException e) {
                    System.out.println("Non valido - " + e.getMessage());
                    System.out.println("Riprova.");
                }
            }

            boolean placed2 = false;
            while (!placed2) {
                System.out.print("Posizione 2: ");
                char c2 = sc.next().charAt(0);
                sc.nextLine();
                try {
                    gameManager.placeTotem(currentPlaceOrder.getFirst().getNickname(), c2);
                    placed2 = true;
                } catch (InvalidActionException | IllegalStateException | IllegalArgumentException e) {
                    System.out.println("Non valido - " + e.getMessage());
                    System.out.println("Riprova.");
                }
            }


            // pickOrder può differire da placeOrder — va riletto dopo il piazzamento
            List<Player> pickOrder = game.getBoard().getPickOrder();
            System.out.println(">>> Ordine di pick: " + pickOrder.stream().map(Player::getNickname).toList());

            for (int pi = 0; pi < pickOrder.size(); pi++) {
                Player current = pickOrder.get(pi);
                int picks = game.getBoard().findTrackPosition(current).getNumUp()
                        + game.getBoard().findTrackPosition(current).getNumDown();

                for (int i = 0; i < picks; i++) {
                    System.out.println("\n[" + current.getNickname() + "] pick " + (i + 1) + "/" + picks);
                    printBoard(game);

                    boolean success = false;
                    while (!success) {
                        System.out.print("Carta: ");
                        String s = sc.nextLine().trim();
                        if (s.isEmpty()) {
                            System.out.println("Input vuoto, riprova.");
                            continue;
                        }
                        try {
                            gameManager.takeCard(current.getNickname(), s);
                            success = true;
                        } catch (InvalidActionException e) {
                            System.out.println("Non valido - " + e.getMessage());
                            System.out.println("Riprova.");
                        }
                    }

                    pickOrder.forEach(GameFlowTest2Players::printStats);
                    printBoard(game);
                }
            }
        }
    }
}
