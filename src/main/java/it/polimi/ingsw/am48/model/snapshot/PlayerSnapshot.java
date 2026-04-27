package it.polimi.ingsw.am48.model.snapshot;

import java.io.Serializable;

public class PlayerSnapshot implements Serializable {
    private final String nickname;
    private final String totemColor;
    private final TribeSnapshot tribe;

    public PlayerSnapshot(String nickname, String totemColor, TribeSnapshot tribe) {
        this.nickname = nickname;
        this.totemColor = totemColor;
        this.tribe = tribe;
    }

    public String getNickname() { return nickname; }
    public String getTotemColor() { return totemColor; }
    public TribeSnapshot getTribe() { return tribe; }
}