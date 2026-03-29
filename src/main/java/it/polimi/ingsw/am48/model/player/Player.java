package it.polimi.ingsw.am48.model.player;

import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.Artifact;
import it.polimi.ingsw.am48.model.enums.Resource;
import it.polimi.ingsw.am48.model.enums.Totem;

public class Player {
    private final String nickname;
    private final Totem totem;
    private final Tribe tribe;

    public Player(String nickname, Totem totem){
        this.nickname = nickname;
        this.totem = totem;
        this.tribe = new Tribe();
    }

    // getter e setter direttamente da player, non passiamo per getTribe()
    public int getPoints() { return this.tribe.getCurrentPrestigePoints(); }
    public int getFood() { return this.tribe.getCurrentFood(); }
    public void updatePoints(int points) { this.tribe.updateCurrentPrestigePoints(points); }
    public void updateFood(int food) { this.tribe.updateCurrentFood(food); }

    // metodo utilizzato nelle strategy per aggiornare statistiche di tribe
    public Tribe getTribe() { return this.tribe; }

    // metodo per pagare cibo e perdere punti in caso di cibo insufficiente
    public void payFood(int food, int ppPerFood){this.tribe.payFood(food, ppPerFood);}

    // per non concatenare getTribe().set(), altrimenti demetra si arrabbia
    public void addToTribe(CharacterCard card) { this.tribe.addToTribe(card); }
    public void addToTribe(BuildingCard card) { this.tribe.addToTribe(card); }
    public void updateFoodDiscount(int amount) { this.tribe.updateFoodDiscount(amount); }
    public void updateBuildingDiscount(int amount) { this.tribe.updateBuildingDiscount(amount); }
    public void updateShamanStars(int amount) { this.tribe.updateShamanStars(amount); }
    public void updateBuilderPoints(int amount) { this.tribe.updateBuilderPoints(amount); }
    public void updateBuildingPoints(int amount) { this.tribe.updateBuildingPoints(amount); }
    public void updateArtifacts(Artifact artifact) { this.tribe.updateArtifacts(artifact); }

}
