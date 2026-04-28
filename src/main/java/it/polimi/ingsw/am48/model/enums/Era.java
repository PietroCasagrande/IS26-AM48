package it.polimi.ingsw.am48.model.enums;

public enum Era {
    FIRST(0),
    SECOND(1),
    THIRD(2);

    private final int index;

    Era(int index) { this.index = index; }

    // Returns index to find the correct number of building cards to display
    public int getIndex() { return index; }

    // Returns the new Era
    public Era next() {
        if (this == THIRD) return THIRD;
        return values()[this.ordinal() + 1];
    }
}
