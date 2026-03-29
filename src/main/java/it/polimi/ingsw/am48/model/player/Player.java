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

    // getter direttamente da player, non passiamo per getTribe()
    public int getPoints() { return tribe.getCurrentPrestigePoints(); }
    public int getFood() { return tribe.getCurrentFood(); }

    // metodo utilizzato nelle strategy per aggiornare statistiche di tribe
    public Tribe getTribe() { return tribe; }

    public void addToTribe(CharacterCard card) { tribe.addToTribe(card); }
    public void addToTribe(BuildingCard card) { tribe.addToTribe(card); }

}
