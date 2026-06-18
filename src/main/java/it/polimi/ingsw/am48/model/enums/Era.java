package it.polimi.ingsw.am48.model.enums;

/**
 * Represents the chronological periods (eras) of the game.
 * <p>
 * The game progresses through these eras sequentially. Each era can dictate
 * specific setup rules, such as the exact number of building cards available
 * on the board during that specific timeframe.
 */
public enum Era {
    FIRST(0),
    SECOND(1),
    THIRD(2);

    private final int index;

    /**
     * Associates the era with its corresponding zero-based index.
     *
     * @param index the numeric index of the era
     */
    Era(int index) {
        this.index = index;
    }

    /**
     * Retrieves the numeric index of the era.
     * <p>
     * This is primarily used as an array offset or map key to fetch era-specific
     * configurations, such as extracting the building setup matrix.
     *
     * @return the zero-based index of the era
     */
    public int getIndex() { return index; }

    /**
     * Progresses the timeline to the next chronological era.
     * <p>
     * If invoked on the final era ({@code THIRD}), it safely returns {@code THIRD}
     * to prevent out-of-bounds errors, as the game's timeline cannot advance further.
     *
     * @return the subsequent {@code Era}, or {@code THIRD} if already at the end
     */
    public Era next() {
        if (this == THIRD) return THIRD;
        return values()[this.ordinal() + 1];
    }
}
