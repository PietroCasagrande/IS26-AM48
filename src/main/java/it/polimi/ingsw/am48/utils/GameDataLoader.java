package it.polimi.ingsw.am48.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import it.polimi.ingsw.am48.dto.BoardDTO;

import java.io.IOException;
import java.io.InputStream;

public class GameDataLoader {
    public BoardDTO loadData() {

        // Creazione tramite Builder Pattern: sicuro, immutabile e moderno
        ObjectMapper mapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("game_data.json")) {

            if (inputStream == null) {
                throw new RuntimeException("Errore critico: File game_data.json non trovato!");
            }

            return mapper.readValue(inputStream, BoardDTO.class);

        } catch (IOException e) {
            throw new RuntimeException("Impossibile parsare il file JSON: " + e.getMessage(), e);
        }
    }
}
