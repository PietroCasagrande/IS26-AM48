package it.polimi.ingsw.am48.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Singleton utility for providing a globally configured Jackson {@link ObjectMapper}.
 * <p>
 * Since {@code ObjectMapper} instances are thread-safe after their initial configuration
 * and relatively heavy to instantiate, this class ensures a single shared instance is
 * utilized throughout the entire application. It comes pre-configured with the Java Time
 * module to serialize dates and times as readable ISO-8601 strings rather than numeric timestamps.
 */
public class JsonMapper {
    private static final ObjectMapper INSTANCE = createMapper();

    /**
     * Configures and creates the singleton instance.
     *
     * @return the fully configured mapper
     */
    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Retrieves the globally shared ObjectMapper instance.
     *
     * @return the configured {@code ObjectMapper}
     */
    public static ObjectMapper get() { return INSTANCE; }
}