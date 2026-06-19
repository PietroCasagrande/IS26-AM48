package it.polimi.ingsw.am48.model.enums;

/**
 * Types of event cards that can appear during a round.
 *
 * <p>Event cards are a distinct card category from character cards and building cards.
 * Each {@code EventType} shares its name with a {@link CharacterType} because the
 * card's effect is computed against the character cards of that type currently present
 * in a player's tribe (e.g. adding or subtracting food/prestige points based on how
 * many characters of the matching type the player owns).
 *
 * <p>Events are resolved in declaration order, with one critical constraint:
 * {@link #PICKER_EVENT} must always be resolved <b>last</b>, after all other events
 * have been applied (see rules, p. 5). This ordering is enforced by the position of
 * {@code PICKER_EVENT} as the final constant in this enum, so that iterating
 * {@link #values()} naturally produces the correct resolution sequence.
 *
 * @see CharacterType
 */
public enum EventType {
    ARTIST_EVENT,
    HUNTER_EVENT,
    SHAMAN_EVENT,
    PICKER_EVENT        // MUST be last: resolved after all other events (see rules p.5)
}
