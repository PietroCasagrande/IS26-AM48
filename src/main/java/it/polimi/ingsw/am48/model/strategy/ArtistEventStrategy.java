package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;

public class ArtistEventStrategy extends CardStrategy {
    int threshold;
    int ppPerArtist;
    int ppLost;   // salvato con segno POSITIVO, metto io il - in updatePoints

    protected ArtistEventStrategy(int threshold, int ppPerArtist, int ppLost, RegistrationAction registration, UnregistrationAction unregistration) {
        super(registration, unregistration);
        this.threshold = threshold;
        this.ppPerArtist = ppPerArtist;
        this.ppLost = ppLost;
    }

    @Override
    public void effect(PlayerContext playerContext) {
        // assegna tot pp per ogni artista posseduto se si è oltre la soglia, altrimenti toglie il numero di pp indicato
        playerContext.getPlayers().forEach(p -> {
                int artistsCount = p.getTribe().countByType(CharacterType.ARTIST);
                if(artistsCount >= this.threshold) p.updatePoints(ppPerArtist * artistsCount);
                else p.updatePoints(-ppLost);
            });
    }
}
