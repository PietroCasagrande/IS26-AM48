package it.polimi.ingsw.am48.model.notificator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificatorCenterTest {

    private NotificatorCenter notificatorCenter;

    @BeforeEach
    void setUp() {
        notificatorCenter = new NotificatorCenter();
    }

    // INITIALIZATION

    @Test
    @DisplayName("constructor: all notificators are initialized (not null)")
    void constructor_allNotificatorsInitialized() {
        assertAll(
                () -> assertNotNull(notificatorCenter.getPickNotificator()),
                () -> assertNotNull(notificatorCenter.getEventNotificator()),
                () -> assertNotNull(notificatorCenter.getEndOfferPhaseNotificator()),
                () -> assertNotNull(notificatorCenter.getTotemReturnedNotificator()),
                () -> assertNotNull(notificatorCenter.getEndGameNotificator())
        );
    }

    @Test
    @DisplayName("constructor: each notificator is a distinct instance")
    void constructor_notificatorsAreDistinctInstances() {
        assertAll(
                () -> assertNotSame(notificatorCenter.getPickNotificator(),
                        notificatorCenter.getEventNotificator()),
                () -> assertNotSame(notificatorCenter.getPickNotificator(),
                        notificatorCenter.getEndOfferPhaseNotificator()),
                () -> assertNotSame(notificatorCenter.getPickNotificator(),
                        notificatorCenter.getTotemReturnedNotificator()),
                () -> assertNotSame(notificatorCenter.getPickNotificator(),
                        notificatorCenter.getEndGameNotificator()),
                () -> assertNotSame(notificatorCenter.getEventNotificator(),
                        notificatorCenter.getEndOfferPhaseNotificator()),
                () -> assertNotSame(notificatorCenter.getEventNotificator(),
                        notificatorCenter.getTotemReturnedNotificator()),
                () -> assertNotSame(notificatorCenter.getEventNotificator(),
                        notificatorCenter.getEndGameNotificator()),
                () -> assertNotSame(notificatorCenter.getEndOfferPhaseNotificator(),
                        notificatorCenter.getTotemReturnedNotificator()),
                () -> assertNotSame(notificatorCenter.getEndOfferPhaseNotificator(),
                        notificatorCenter.getEndGameNotificator()),
                () -> assertNotSame(notificatorCenter.getTotemReturnedNotificator(),
                        notificatorCenter.getEndGameNotificator())
        );
    }

    // GETTERS — same instance returned on repeated calls

    @Test
    @DisplayName("getPickNotificator: returns the correct type")
    void getPickNotificator_returnsCorrectType() {
        assertInstanceOf(OnPickNotificator.class, notificatorCenter.getPickNotificator());
    }

    @Test
    @DisplayName("getEventNotificator: returns the correct type")
    void getEventNotificator_returnsCorrectType() {
        assertInstanceOf(OnEventNotificator.class, notificatorCenter.getEventNotificator());
    }

    @Test
    @DisplayName("getEndOfferPhaseNotificator: returns the correct type")
    void getEndOfferPhaseNotificator_returnsCorrectType() {
        assertInstanceOf(OnEndOfferPhaseNotificator.class, notificatorCenter.getEndOfferPhaseNotificator());
    }

    @Test
    @DisplayName("getTotemReturnedNotificator: returns the correct type")
    void getTotemReturnedNotificator_returnsCorrectType() {
        assertInstanceOf(OnTotemReturnedNotificator.class, notificatorCenter.getTotemReturnedNotificator());
    }

    @Test
    @DisplayName("getEndGameNotificator: returns the correct type")
    void getEndGameNotificator_returnsCorrectType() {
        assertInstanceOf(OnEndGameNotificator.class, notificatorCenter.getEndGameNotificator());
    }

    @Test
    @DisplayName("getters: same instance returned on repeated calls (no re-instantiation)")
    void getters_sameInstanceOnRepeatedCalls() {
        assertAll(
                () -> assertSame(notificatorCenter.getPickNotificator(),
                        notificatorCenter.getPickNotificator()),
                () -> assertSame(notificatorCenter.getEventNotificator(),
                        notificatorCenter.getEventNotificator()),
                () -> assertSame(notificatorCenter.getEndOfferPhaseNotificator(),
                        notificatorCenter.getEndOfferPhaseNotificator()),
                () -> assertSame(notificatorCenter.getTotemReturnedNotificator(),
                        notificatorCenter.getTotemReturnedNotificator()),
                () -> assertSame(notificatorCenter.getEndGameNotificator(),
                        notificatorCenter.getEndGameNotificator())
        );
    }

    @Test
    @DisplayName("constructor: two NotificatorCenter instances have independent notificators")
    void constructor_twoInstances_haveIndependentNotificators() {
        NotificatorCenter second = new NotificatorCenter();

        assertAll(
                () -> assertNotSame(notificatorCenter.getPickNotificator(),
                        second.getPickNotificator()),
                () -> assertNotSame(notificatorCenter.getEventNotificator(),
                        second.getEventNotificator()),
                () -> assertNotSame(notificatorCenter.getEndOfferPhaseNotificator(),
                        second.getEndOfferPhaseNotificator()),
                () -> assertNotSame(notificatorCenter.getTotemReturnedNotificator(),
                        second.getTotemReturnedNotificator()),
                () -> assertNotSame(notificatorCenter.getEndGameNotificator(),
                        second.getEndGameNotificator())
        );
    }
}