package it.polimi.ingsw.am48.model.notificator;

import it.polimi.ingsw.am48.model.enums.EventType;
import it.polimi.ingsw.am48.model.strategy.CardStrategy;

import java.util.List;
import java.util.Map;

public class OnEventNotificator {
    private Map<EventType, List<CardStrategy>> listeners;

    public void attach(EventType e, CardStrategy cs){
        // TODO
    }

    public void detach(){
        // TODO
    }

    public void notifyListeners(EventType p){
        // TODO
    }
}
