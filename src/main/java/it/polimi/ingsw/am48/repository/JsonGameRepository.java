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

public class JsonGameRepository implements GameRepository {

    private final Path savesDir;  // percorso della cartella dove risiedono i file JSON
    private final ObjectMapper objectMapper;  // traduce tra Oggetti Java e JSON

    public JsonGameRepository(String savesDirPath) {
        // converte la stringa in un oggetto Path gestibile dal sistema operativo
        this.savesDir = Path.of(savesDirPath);
        this.objectMapper = new ObjectMapper();
        createSavesDirIfAbsent();
    }

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

    @Override
    public void delete(String gameId) {
        try {
            // elimina il file se esiste; Files.deleteIfExists evita eccezioni se il file è già stato rimosso
            Files.deleteIfExists(fileFor(gameId));
        } catch (IOException e) {
            throw new RuntimeException("Errore nella cancellazione della partita " + gameId, e);
        }
    }

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

    private Path fileFor(String gameId) {
        // aggiunge nome file specifico al percorso
        return savesDir.resolve(gameId + ".json");
    }

    private void createSavesDirIfAbsent() {
        try {
            // crea cartella dei salvataggi se mancante
            Files.createDirectories(savesDir);
        } catch (IOException e) {
            throw new RuntimeException("Impossibile creare la cartella saves: " + savesDir, e);
        }
    }
    
}