package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;

import java.util.List;

public class ShamanEventStrategy extends CardStrategy {
    private int ppToMin;    // salvati POSITIVI
    private int ppToMax;

    protected ShamanEventStrategy(int ppToMin, int ppToMax, RegistrationAction registration) {
        super(registration);
        this.ppToMin = ppToMin;
        this.ppToMax = ppToMax;
    }

    @Override
    public void effect(PlayerContext playerContext) {
        List<Player> players = playerContext.getPlayers();

        // numero massimo di stelle riscontrato
        int maxStars = players.stream()
                .mapToInt(Player::getShamanStars)
                .max()
                .orElse(0);

        // numero minimo di stelle riscontrato
        int minStars = players.stream()
                .mapToInt(Player::getShamanStars)
                .min()
                .orElse(0);

        players.forEach(p -> {
            // tutti i player con le stelle massime ricevono punti (o doppi punti se si ha quell'edificio specifico)
            if (p.getShamanStars() == maxStars){
                p.updatePoints(ppToMax);
                if(p.deservesDoubleShamanPp()) p.updatePoints(ppToMax);
            }
            // tutti i player con le stelle minime perdono punti (a meno che non si abbia l'edificio che rende immuni)
            if(p.getShamanStars() == minStars){
                if(!p.isShamanSafe()) p.updatePoints(-ppToMin);
            }
        });
    }
}
