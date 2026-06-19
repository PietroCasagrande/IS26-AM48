package it.polimi.ingsw.am48.model.enums;

/**
 * Artifact symbols found on {@link CharacterType#INVENTOR} character cards.
 *
 * <p>Each Inventor card bears exactly one artifact symbol. At the end of the game,
 * the inventor contribution to a player's score is calculated as:
 * <pre>
 *   score = (number of Inventor cards in the tribe)
 *           × (number of <em>distinct</em> artifact types among those Inventors)
 * </pre>
 * Collecting multiple Inventors with different artifacts therefore multiplies the
 * score more than collecting duplicates of the same artifact.
 */
public enum Artifact {
    ARROW,
    BREAD,
    BOWL,
    CANOE,
    DOLL,
    FLUTE,
    HOOK,
    LEATHER,
    NECKLACE,
    ROPE
}
