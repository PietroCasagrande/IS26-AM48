package it.polimi.ingsw.am48.model.notificator;

public class NotificatorCenter {
    private OnPickNotificator pickNotificator;
    private OnEventNotificator eventNotificator;
    private OnEndOfferPhaseNotificator endOfferPhaseNotificator;
    private OnTotemReturnedNotificator totemReturnedNotificator;
    private OnEndGameNotificator endGameNotificator;

    public NotificatorCenter() {
        this.pickNotificator = new OnPickNotificator();
        this.eventNotificator = new OnEventNotificator();
        this.endOfferPhaseNotificator = new OnEndOfferPhaseNotificator();
        this.totemReturnedNotificator = new OnTotemReturnedNotificator();
        this.endGameNotificator = new OnEndGameNotificator();
    }

    public OnPickNotificator getPickNotificator() {
        return pickNotificator;
    }

    public OnEventNotificator getEventNotificator() {
        return eventNotificator;
    }

    public OnEndOfferPhaseNotificator getEndOfferPhaseNotificator() {
        return endOfferPhaseNotificator;
    }

    public OnTotemReturnedNotificator getTotemReturnedNotificator() {
        return totemReturnedNotificator;
    }

    public OnEndGameNotificator getEndGameNotificator() {
        return endGameNotificator;
    }
}
