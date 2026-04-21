package it.polimi.ingsw.am48.network.client;

import it.polimi.ingsw.am48.model.snapshot.PlayerSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ClientPlayerState {
    private String nickname;
    private String totemColor;
    private int food;
    private int points;
    private List<String> cardIds; // carte in tribù

    public static ClientPlayerState fromSnapshot(PlayerSnapshot snapshot) {
        ClientPlayerState state = new ClientPlayerState();
        state.nickname = snapshot.getNickname();
        state.totemColor = snapshot.getTotemColor();
        state.food = snapshot.getTribe().getCurrentFood();
        state.points = snapshot.getTribe().getCurrentPrestigePoints();
        state.cardIds = new ArrayList<>(snapshot.getTribe().getCharacterCardIds());
        return state;
    }

    // setters e getters
    public void setFood(int food) { this.food = food; }
    public void setPoints(int points) { this.points = points; }
    public void addCard(String cardId) { cardIds.add(cardId); }
}