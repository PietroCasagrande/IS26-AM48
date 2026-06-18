package it.polimi.ingsw.am48.repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * File-system implementation of {@link GameRepository} that persists each game as a JSON
 * file.
 * <p>
 * Every game is stored as a single {@code <gameId>.json} file inside a configurable saves
 * directory, created on construction if it does not yet exist. Serialization to and from
 * {@link GameSnapshot} is handled by a Jackson {@link ObjectMapper}; I/O failures are
 * surfaced as unchecked {@link RuntimeException}s.
 *
 * @see GameRepository
 * @see GameSnapshot
 */
public class JsonGameRepository implements GameRepository {

    private final Path savesDir;  // percorso della cartella dove risiedono i file JSON
    private final ObjectMapper objectMapper;  // traduce tra Oggetti Java e JSON

    /**
     * Creates a repository rooted at the given directory, creating the directory if it is
     * absent.
     *
     * @param savesDirPath the path of the folder where the JSON save files are stored
     * @throws RuntimeException if the saves directory cannot be created
     */
    public JsonGameRepository(String savesDirPath) {
        // converte la stringa in un oggetto Path gestibile dal sistema operativo
        this.savesDir = Path.of(savesDirPath);
        this.objectMapper = new ObjectMapper();
        createSavesDirIfAbsent();
    }

    /**
     * Serializes the snapshot to JSON and writes it to {@code <gameId>.json}, overwriting any
     * existing file for the same game.
     *
     * @param gameId   the unique identifier of the game
     * @param snapshot the snapshot to persist
     * @throws RuntimeException if the snapshot cannot be written to disk
     */
    @Override
    public void save(String gameId, GameSnapshot snapshot) {
        Path file = fileFor(gameId);
        try {
            // converte l'oggetto snapshot in JSON e lo scrive fisicamente nel file indicato dal Path
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file.toFile(), snapshot);
        } catch (IOException e) {
            throw new RuntimeException("Errore nel salvataggio della partita " + gameId, e);
        }
    }

    /**
     * Reads {@code <gameId>.json} and deserializes it back into a {@link GameSnapshot}.
     *
     * @param gameId the unique identifier of the game to load
     * @return an {@link Optional} containing the snapshot, or an empty optional if no file
     *         exists for the given id
     * @throws RuntimeException if the file exists but cannot be read or parsed
     */
    @Override
    public Optional<GameSnapshot> load(String gameId) {
        Path file = fileFor(gameId);
        // Path e Files ci permettono di verificare l'esistenza del file prima di tentare la lettura
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try {
            // legge il contenuto del file e lo ricostruisce come istanza della classe GameSnapshot
            GameSnapshot snapshot = objectMapper.readValue(file.toFile(), GameSnapshot.class);
            return Optional.of(snapshot);
        } catch (IOException e) {
            throw new RuntimeException("Errore nel caricamento della partita " + gameId, e);
        }
    }

    /**
     * Deletes {@code <gameId>.json} if it exists; does nothing if the file is already absent.
     *
     * @param gameId the unique identifier of the game to delete
     * @throws RuntimeException if the file exists but cannot be deleted
     */
    @Override
    public void delete(String gameId) {
        try {
            // elimina il file se esiste; Files.deleteIfExists evita eccezioni se il file è già stato rimosso
            Files.deleteIfExists(fileFor(gameId));
        } catch (IOException e) {
            throw new RuntimeException("Errore nella cancellazione della partita " + gameId, e);
        }
    }

    /**
     * Lists the ids of all stored games by scanning the saves directory for {@code .json}
     * files and stripping their extension.
     *
     * @return the list of game ids found in the saves directory
     * @throws RuntimeException if the saves directory cannot be read
     */
    @Override
    public List<String> listActiveGameIds() {
        // apre uno Stream sulle risorse della cartella. try-with-resources garantisce la chiusura del file system stream
        try (Stream<Path> files = Files.list(savesDir)) {
            return files
                    .filter(p -> p.toString().endsWith(".json")) // considera solo i salvataggi JSON
                    .map(p -> p.getFileName().toString().replace(".json", "")) // estrae solo l'ID (nome file senza estensione)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Errore nella lettura della cartella saves", e);
        }
    }

    /**
     * Builds the path of the save file backing the given game id.
     *
     * @param gameId the game identifier
     * @return the path {@code <savesDir>/<gameId>.json}
     */
    private Path fileFor(String gameId) {
        // aggiunge nome file specifico al percorso
        return savesDir.resolve(gameId + ".json");
    }

    /**
     * Creates the saves directory (and any missing parent) if it does not already exist.
     *
     * @throws RuntimeException if the directory cannot be created
     */
    private void createSavesDirIfAbsent() {
        try {
            // crea cartella dei salvataggi se mancante
            Files.createDirectories(savesDir);
        } catch (IOException e) {
            throw new RuntimeException("Impossibile creare la cartella saves: " + savesDir, e);
        }
    }
    
}