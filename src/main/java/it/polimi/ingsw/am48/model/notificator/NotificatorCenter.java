package it.polimi.ingsw.am48.model.notificator;

/**
 * Central registry that provides access to all notificators.
 * Each notificator handles a specific trigger event
 * and manages the {@link it.polimi.ingsw.am48.model.strategy.CardStrategy} instances
 * registered to respond to that trigger.
 *
 * <p>This class follows the mediator pattern: it coordinates the five notificators
 * without coupling them to each other, and acts as a single point of access for
 * the phases.</p>
 */

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
