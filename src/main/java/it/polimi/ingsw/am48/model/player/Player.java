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
    public int getPoints() { return tribe.getCurrentPrestigePoints(); }
    public int getFood() { return tribe.getCurrentFood(); }
    public void setPoints(int points) { tribe.setCurrentPrestigePoints(points); }
    public void setFood(int food) { tribe.setCurrentFood(food); }

    // metodo utilizzato nelle strategy per aggiornare statistiche di tribe
    public Tribe getTribe() { return tribe; }

    // per non concatenare getTribe().set(), altrimenti demetra si arrabbia
    public void addToTribe(CharacterCard card) { tribe.addToTribe(card); }
    public void addToTribe(BuildingCard card) { tribe.addToTribe(card); }
    public void updateFoodDiscount(int amount) { tribe.updateFoodDiscount(amount); }
    public void updateBuildingDiscount(int amount) { tribe.updateBuildingDiscount(amount); }
    public void updateShamanStars(int amount) { tribe.updateShamanStars(amount); }
    public void updateBuilderPoints(int amount) { tribe.updateBuilderPoints(amount); }
    public void updateBuildingPoints(int amount) { tribe.updateBuildingPoints(amount); }
    public void updateArtifacts(Artifact artifact) { tribe.updateArtifacts(artifact); }

}
