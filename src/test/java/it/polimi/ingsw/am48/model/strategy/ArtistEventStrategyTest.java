package it.polimi.ingsw.am48.model.strategy;

import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.enums.Era;
import it.polimi.ingsw.am48.model.enums.Totem;
import it.polimi.ingsw.am48.model.player.Player;
import it.polimi.ingsw.am48.model.player.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ArtistEventStrategyTest {

    private Player p1;
    private Player p2;
    private Player p3;
    private PlayerContext context;
    private ArtistEventStrategy strategy;

    // threshold=2, ppPerArtist=3, ppLost=1
    @BeforeEach
    void setUp() {
        p1 = new Player("alice", Totem.BLACK);
        p2 = new Player("bob", Totem.BLUE);
        p3 = new Player("charlie", Totem.RED);

        context = new PlayerContext();
        context.setCurrPlayer(p1);
        context.addPlayer(p1);
        context.addPlayer(p2);
        context.addPlayer(p3);

        strategy = new ArtistEventStrategy(2, 3, -1, null);
    }

    private void addArtists(Player player, int count) {
        for (int i = 0; i < count; i++) {
            player.addToTribe(new CharacterCard("A" + i, Era.FIRST, null, CharacterType.ARTIST, 2));
        }
    }

    @Test
    void shouldGiveNoPPIfNoArtists() {
        // player with no artists is below the threshold, loses ppLost
        strategy.effect(context);
        assertEquals(-1, p1.getPoints());
    }

    @Test
    void shouldLosePPIfBelowThreshold() {
        // player with fewer artists than the threshold loses ppLost
        addArtists(p1, 1);
        strategy.effect(context);
        assertEquals(-1, p1.getPoints());
    }

    @Test
    void shouldGivePPExactlyAtThreshold() {
        // player with exactly threshold artists receives ppPerArtist * threshold
        addArtists(p1, 2); // threshold = 2
        strategy.effect(context);
        assertEquals(6, p1.getPoints()); // 3 pp × 2 artists
    }

    @Test
    void shouldGivePPAboveThreshold() {
        // player with more artists than the threshold receives ppPerArtist * count
        addArtists(p1, 4);
        strategy.effect(context);
        assertEquals(12, p1.getPoints()); // 3 pp × 4 artists
    }

    @Test
    void shouldAffectAllPlayersIndependently() {
        // each player is evaluated independently
        addArtists(p1, 3); // above threshold
        addArtists(p2, 1); // below threshold
        // p3 with no artists - below threshold
        strategy.effect(context);
        assertEquals(9, p1.getPoints());  // 3 pp × 3 artists
        assertEquals(-1, p2.getPoints()); // loses ppLost
        assertEquals(-1, p3.getPoints()); // loses ppLost
    }

    @Test
    void shouldStackWithExistingPoints() {
        // the effect adds to the points the player already holds
        addArtists(p1, 2);
        p1.updatePoints(5); // pre-existing points
        strategy.effect(context);
        assertEquals(11, p1.getPoints()); // 5 + (3 pp × 2 artists)
    }

    @Test
    void shouldReduceExistingPointsBelowThreshold() {
        // ppLost is subtracted from the points already held if below threshold
        p1.updatePoints(5); // pre-existing points
        strategy.effect(context); // no artists, below threshold
        assertEquals(4, p1.getPoints()); // 5 - 1 ppLost
    }

    @Test
    void shouldAllowNegativePointsIfPPLostExceedsCurrentPoints() {
        // points can become negative if ppLost exceeds the current points
        strategy.effect(context); // p1 starts from 0, loses 1
        assertEquals(-1, p1.getPoints());
    }

    @Test
    void shouldHandleAllPlayersAboveThreshold() {
        // all players above threshold receive pp, no one loses
        addArtists(p1, 2);
        addArtists(p2, 3);
        addArtists(p3, 5);
        strategy.effect(context);
        assertEquals(6, p1.getPoints());  // 3 pp × 2 artists
        assertEquals(9, p2.getPoints());  // 3 pp × 3 artists
        assertEquals(15, p3.getPoints()); // 3 pp × 5 artists
    }

    @Test
    void shouldHandleAllPlayersBelowThreshold() {
        // all players below threshold lose ppLost
        addArtists(p1, 1);
        addArtists(p2, 0);
        addArtists(p3, 0);
        strategy.effect(context);
        assertEquals(-1, p1.getPoints());
        assertEquals(-1, p2.getPoints());
        assertEquals(-1, p3.getPoints());
    }
}
