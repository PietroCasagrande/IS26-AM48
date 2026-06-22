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

    private final Path savesDir;  // path of the folder where the JSON files live
    private final ObjectMapper objectMapper;  // translates between Java objects and JSON

    /**
     * Creates a repository rooted at the given directory, creating the directory if it is
     * absent.
     *
     * @param savesDirPath the path of the folder where the JSON save files are stored
     * @throws RuntimeException if the saves directory cannot be created
     */
    public JsonGameRepository(String savesDirPath) {
        // converts the string into a Path object handled by the operating system
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
            // converts the snapshot object into JSON and physically writes it to the file at the Path
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file.toFile(), snapshot);
        } catch (IOException e) {
            throw new RuntimeException("Error while saving game " + gameId, e);
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
        // Path and Files let us check that the file exists before attempting to read it
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try {
            // reads the file content and rebuilds it as an instance of the GameSnapshot class
            GameSnapshot snapshot = objectMapper.readValue(file.toFile(), GameSnapshot.class);
            return Optional.of(snapshot);
        } catch (IOException e) {
            throw new RuntimeException("Error while loading game " + gameId, e);
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
            // deletes the file if it exists; Files.deleteIfExists avoids exceptions if the file was already removed
            Files.deleteIfExists(fileFor(gameId));
        } catch (IOException e) {
            throw new RuntimeException("Error while deleting game " + gameId, e);
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
        // opens a Stream over the folder contents. try-with-resources guarantees the file system stream is closed
        try (Stream<Path> files = Files.list(savesDir)) {
            return files
                    .filter(p -> p.toString().endsWith(".json")) // only consider JSON saves
                    .map(p -> p.getFileName().toString().replace(".json", "")) // extract only the ID (file name without extension)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error while reading the saves folder", e);
        }
    }

    /**
     * Builds the path of the save file backing the given game id.
     *
     * @param gameId the game identifier
     * @return the path {@code <savesDir>/<gameId>.json}
     */
    private Path fileFor(String gameId) {
        // appends the specific file name to the path
        return savesDir.resolve(gameId + ".json");
    }

    /**
     * Creates the saves directory (and any missing parent) if it does not already exist.
     *
     * @throws RuntimeException if the directory cannot be created
     */
    private void createSavesDirIfAbsent() {
        try {
            // create the saves folder if missing
            Files.createDirectories(savesDir);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create the saves folder: " + savesDir, e);
        }
    }
    
}