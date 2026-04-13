package it.polimi.ingsw.am48.model.enums;

public enum EventType {
    ARTIST_EVENT,
    HUNTER_EVENT,
    SHAMAN_EVENT,
    PICKER_EVENT        // MUST be last: resolved after all other events (see rules p.5)
}
