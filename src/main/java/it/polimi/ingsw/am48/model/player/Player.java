package it.polimi.ingsw.am48.model.player;

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

    public int getPoints() { return tribe.getCurrentPrestigePoints(); }
    public int getFood() { return tribe.getFood(); }
    public void addToTribe(CharacterCard card) { tribe.addToTribe(card); }
    public void addToTribe(BuildingCard card) { tribe.addToTribe(card); }
    public void addResource(Resource resource, int amount) { tribe.updateResource(resource, amount); }
    public void addArtifact(Artifact artifact) { tribe.updateArtifact(artifact); }
    // da capire se servono i metodi getTribe(), removePoints(), e altri getter
}
