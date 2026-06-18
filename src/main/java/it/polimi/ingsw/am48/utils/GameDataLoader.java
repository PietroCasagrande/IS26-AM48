package it.polimi.ingsw.am48.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import it.polimi.ingsw.am48.dto.BoardDTO;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class responsible for loading and parsing the raw game configuration data.
 * <p>
 * This loader reads the {@code game_data.json} file from the classpath and utilizes
 * the Jackson library to deserialize it directly into the root {@link BoardDTO}.
 * It serves as the primary data ingress point for the entire factory setup pipeline.
 */
public class GameDataLoader {

    /**
     * Loads the game configuration blueprints from the internal JSON file.
     *
     * @return the fully populated {@link BoardDTO} containing all raw game data
     * @throws RuntimeException if the file cannot be located on the classpath or if parsing fails
     */
    public BoardDTO loadData() {

        ObjectMapper mapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("game_data.json")) {

            if (inputStream == null) {
                throw new RuntimeException("Critical error: cannot find game_data.json!");
            }

            return mapper.readValue(inputStream, BoardDTO.class);

        } catch (IOException e) {
            throw new RuntimeException("Unable to parse the json file: " + e.getMessage(), e);
        }
    }
}
