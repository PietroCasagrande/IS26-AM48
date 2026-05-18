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
        // player senza artisti non raggiunge la soglia, perde ppLost
        strategy.effect(context);
        assertEquals(-1, p1.getPoints());
    }

    @Test
    void shouldLosePPIfBelowThreshold() {
        // player con meno artisti del threshold perde ppLost
        addArtists(p1, 1);
        strategy.effect(context);
        assertEquals(-1, p1.getPoints());
    }

    @Test
    void shouldGivePPExactlyAtThreshold() {
        // player con esattamente threshold artisti riceve ppPerArtist * threshold
        addArtists(p1, 2); // threshold = 2
        strategy.effect(context);
        assertEquals(6, p1.getPoints()); // 3 pp × 2 artisti
    }

    @Test
    void shouldGivePPAboveThreshold() {
        // player con più artisti del threshold riceve ppPerArtist * count
        addArtists(p1, 4);
        strategy.effect(context);
        assertEquals(12, p1.getPoints()); // 3 pp × 4 artisti
    }

    @Test
    void shouldAffectAllPlayersIndependently() {
        // ogni player viene valutato indipendentemente
        addArtists(p1, 3); // sopra soglia
        addArtists(p2, 1); // sotto soglia
        // p3 senza artisti - sotto soglia
        strategy.effect(context);
        assertEquals(9, p1.getPoints());  // 3 pp × 3 artisti
        assertEquals(-1, p2.getPoints()); // perde ppLost
        assertEquals(-1, p3.getPoints()); // perde ppLost
    }

    @Test
    void shouldStackWithExistingPoints() {
        // l'effetto si somma ai punti già posseduti dal player
        addArtists(p1, 2);
        p1.updatePoints(5); // punti pre-esistenti
        strategy.effect(context);
        assertEquals(11, p1.getPoints()); // 5 + (3 pp × 2 artisti)
    }

    @Test
    void shouldReduceExistingPointsBelowThreshold() {
        // ppLost si sottrae dai punti già posseduti se sotto soglia
        p1.updatePoints(5); // punti pre-esistenti
        strategy.effect(context); // nessun artista, sotto soglia
        assertEquals(4, p1.getPoints()); // 5 - 1 ppLost
    }

    @Test
    void shouldAllowNegativePointsIfPPLostExceedsCurrentPoints() {
        // i punti possono diventare negativi se ppLost supera i punti attuali
        strategy.effect(context); // p1 parte da 0, perde 1
        assertEquals(-1, p1.getPoints());
    }

    @Test
    void shouldHandleAllPlayersAboveThreshold() {
        // tutti i player sopra soglia ricevono pp, nessuno perde
        addArtists(p1, 2);
        addArtists(p2, 3);
        addArtists(p3, 5);
        strategy.effect(context);
        assertEquals(6, p1.getPoints());  // 3 pp × 2 artisti
        assertEquals(9, p2.getPoints());  // 3 pp × 3 artisti
        assertEquals(15, p3.getPoints()); // 3 pp × 5 artisti
    }

    @Test
    void shouldHandleAllPlayersBelowThreshold() {
        // tutti i player sotto soglia perdono ppLost
        addArtists(p1, 1);
        addArtists(p2, 0);
        addArtists(p3, 0);
        strategy.effect(context);
        assertEquals(-1, p1.getPoints());
        assertEquals(-1, p2.getPoints());
        assertEquals(-1, p3.getPoints());
    }
}