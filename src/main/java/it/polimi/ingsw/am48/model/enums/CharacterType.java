package it.polimi.ingsw.am48.model.enums;

/**
 * Categories of character cards that players can acquire for their tribe.
 *
 * <p>The type determines the card's role and scoring contribution:
 * <ul>
 *   <li>{@link #INVENTOR} cards each carry an {@link Artifact} symbol; their end-game
 *       score depends on the number of Inventors in the tribe and the variety of
 *       artifacts among them.</li>
 *   <li>The remaining types ({@link #ARTIST}, {@link #HUNTER}, {@link #PICKER},
 *       {@link #SHAMAN}, {@link #BUILDER}) influence scoring and game effects in
 *       type-specific ways.</li>
 * </ul>
 *
 * <p>Character types are also referenced by {@link EventType} event cards: each event
 * card's effect is computed based on the number or characteristics of the matching
 * character type present in a player's tribe, but the character cards themselves do
 * not trigger events directly.
 *
 * @see EventType
 * @see Artifact
 */
public enum CharacterType {
    ARTIST,
    BUILDER,
    INVENTOR,
    HUNTER,
    PICKER,
    SHAMAN
}
