package it.polimi.ingsw.am48.model.player;

import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.Artifact;
import it.polimi.ingsw.am48.model.enums.Totem;

import java.util.Map;
import java.util.Set;

public class Player {
    private final String nickname;
    private final Totem totem;
    private final Tribe tribe;

    public Player(String nickname, Totem totem){
        this.nickname = nickname;
        this.totem = totem;
        this.tribe = new Tribe();
    }

    // getter nickname e totem
    public String getNickname(){ return nickname; }
    public Totem getTotem(){ return totem; }

    // getter e setter direttamente da player, non passiamo per getTribe()
    public int getPoints() { return this.tribe.getCurrentPrestigePoints(); }
    public int getFood() { return this.tribe.getCurrentFood(); }
    public void updatePoints(int points) { this.tribe.updateCurrentPrestigePoints(points); }
    public void updateFood(int food) { this.tribe.updateCurrentFood(food); }

    // metodo utilizzato nelle strategy per aggiornare statistiche di tribe
    public Tribe getTribe() { return this.tribe; }    // forse non serve più avendo aggiunto i getter per ogni attributo

    // metodo per pagare cibo e perdere punti in caso di cibo insufficiente
    public void payFood(int food, int ppPerFood){this.tribe.payFood(food, ppPerFood); }

    // per non concatenare getTribe().set(), altrimenti demetra si arrabbia
    public void addToTribe(CharacterCard card) { this.tribe.addToTribe(card); }
    public void addToTribe(BuildingCard card) { this.tribe.addToTribe(card); }
    public int getFoodDiscount() { return tribe.getFoodDiscount(); }
    public int getBuildingDiscount() { return tribe.getBuildingDiscount(); }
    public int getShamanStars() { return tribe.getShamanStars(); }
    public boolean isShamanSafe() { return tribe.isShamanSafe(); }
    public int getBuilderPoints() { return tribe.getBuilderPoints(); }
    public int getBuildingPoints() { return tribe.getBuildingPoints(); }
    public Map<Artifact, Integer> getArtifacts() { return tribe.getArtifacts(); }
    public void updateFoodDiscount(int amount) { this.tribe.updateFoodDiscount(amount); }
    public void updateBuildingDiscount(int amount) { this.tribe.updateBuildingDiscount(amount); }
    public void updateShamanStars(int amount) { this.tribe.updateShamanStars(amount); }
    public void setShamanSafety(){ this.tribe.setShamanSafety(); }
    public void updateBuilderPoints(int amount) { this.tribe.updateBuilderPoints(amount); }
    public void updateBuildingPoints(int amount) { this.tribe.updateBuildingPoints(amount); }
    public void addArtifact(Artifact artifact) { this.tribe.addArtifact(artifact); }

}
