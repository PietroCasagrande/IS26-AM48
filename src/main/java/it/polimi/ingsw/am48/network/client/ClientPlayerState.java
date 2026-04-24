package it.polimi.ingsw.am48.network.client;

import it.polimi.ingsw.am48.model.snapshot.PlayerSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientPlayerState {
    private String nickname;
    private String totemColor;
    private int food;
    private int points;
    private List<String> characterCardIds;
    private List<String> buildingCardIds;

    public static ClientPlayerState fromSnapshot(PlayerSnapshot snapshot) {
        ClientPlayerState state = new ClientPlayerState();
        state.nickname = snapshot.getNickname();
        state.totemColor = snapshot.getTotemColor();
        state.food = snapshot.getTribe().getCurrentFood();
        state.points = snapshot.getTribe().getCurrentPrestigePoints();
        state.characterCardIds = new ArrayList<>(snapshot.getTribe().getCharacterCardIds());
        state.buildingCardIds = new ArrayList<>(snapshot.getTribe().getBuildingCardIds());
        return state;
    }

    public void setFood(int food) { this.food = food; }
    public void setPoints(int points) { this.points = points; }
    public void addCharacterCard(String cardId) { characterCardIds.add(cardId); }
    public void addBuildingCard(String cardId) { buildingCardIds.add(cardId); }

    public String getNickname() { return nickname; }
    public String getTotemColor() { return totemColor; }
    public int getFood() { return food; }
    public int getPoints() { return points; }
    public List<String> getCharacterCardIds() { return Collections.unmodifiableList(characterCardIds); }
    public List<String> getBuildingCardIds() { return Collections.unmodifiableList(buildingCardIds); }
}