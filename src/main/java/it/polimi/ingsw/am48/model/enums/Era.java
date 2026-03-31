package it.polimi.ingsw.am48.model.enums;

public enum Era {
    FIRST(0),
    SECOND(1),
    THIRD(2);

    private final int index;

    Era(int index) { this.index = index; }
    public int getIndex() { return index; }

    public Era next() {
        if (this == THIRD) return THIRD;
        return values()[this.ordinal() + 1];
    }
}
