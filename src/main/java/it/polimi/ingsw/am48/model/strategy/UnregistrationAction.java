package it.polimi.ingsw.am48.model.strategy;

import java.util.List;

public interface UnregistrationAction {
    void unregisterMethod(List<CardStrategy> toDetach);
}
