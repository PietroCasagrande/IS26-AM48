package it.polimi.ingsw.am48.model.notificator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link NotificatorCenter}.
 * Verifies that the constructor initializes all five notificators as non-null,
 * distinct instances, that each getter returns the correct type, that repeated
 * calls return the same instance, and that independent centers are isolated.
 */
class NotificatorCenterTest {

    private NotificatorCenter notificatorCenter;

    @BeforeEach
    void setUp() {
        notificatorCenter = new NotificatorCenter();
    }

    // INITIALIZATION

    /**
     * Tests that {@link NotificatorCenter#NotificatorCenter()} initializes all
     * five notificator fields to non-null instances.
     */
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

    /**
     * Tests that the five notificator instances created by the constructor
     * are all distinct objects.
     */
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

    /**
     * Tests that {@link NotificatorCenter#getPickNotificator()} returns an
     * instance of {@link OnPickNotificator}.
     */
    @Test
    @DisplayName("getPickNotificator: returns the correct type")
    void getPickNotificator_returnsCorrectType() {
        assertInstanceOf(OnPickNotificator.class, notificatorCenter.getPickNotificator());
    }

    /**
     * Tests that {@link NotificatorCenter#getEventNotificator()} returns an
     * instance of {@link OnEventNotificator}.
     */
    @Test
    @DisplayName("getEventNotificator: returns the correct type")
    void getEventNotificator_returnsCorrectType() {
        assertInstanceOf(OnEventNotificator.class, notificatorCenter.getEventNotificator());
    }

    /**
     * Tests that {@link NotificatorCenter#getEndOfferPhaseNotificator()} returns an
     * instance of {@link OnEndOfferPhaseNotificator}.
     */
    @Test
    @DisplayName("getEndOfferPhaseNotificator: returns the correct type")
    void getEndOfferPhaseNotificator_returnsCorrectType() {
        assertInstanceOf(OnEndOfferPhaseNotificator.class, notificatorCenter.getEndOfferPhaseNotificator());
    }

    /**
     * Tests that {@link NotificatorCenter#getTotemReturnedNotificator()} returns an
     * instance of {@link OnTotemReturnedNotificator}.
     */
    @Test
    @DisplayName("getTotemReturnedNotificator: returns the correct type")
    void getTotemReturnedNotificator_returnsCorrectType() {
        assertInstanceOf(OnTotemReturnedNotificator.class, notificatorCenter.getTotemReturnedNotificator());
    }

    /**
     * Tests that {@link NotificatorCenter#getEndGameNotificator()} returns an
     * instance of {@link OnEndGameNotificator}.
     */
    @Test
    @DisplayName("getEndGameNotificator: returns the correct type")
    void getEndGameNotificator_returnsCorrectType() {
        assertInstanceOf(OnEndGameNotificator.class, notificatorCenter.getEndGameNotificator());
    }

    /**
     * Tests that repeated calls to each getter return the exact same instance
     * (no re-instantiation on each call).
     */
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

    /**
     * Tests that two independent {@link NotificatorCenter} instances each hold
     * their own separate notificator objects (no accidental sharing).
     */
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