package it.polimi.ingsw.am48.model.player;

import it.polimi.ingsw.am48.model.card.BuildingCard;
import it.polimi.ingsw.am48.model.card.Card;
import it.polimi.ingsw.am48.model.card.CharacterCard;
import it.polimi.ingsw.am48.model.enums.Artifact;
import it.polimi.ingsw.am48.model.enums.CharacterType;
import it.polimi.ingsw.am48.model.snapshot.TribeSnapshot;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Holds the entire mutable state of a single player.
 * <p>
 * A tribe groups the player's acquired characters (indexed by {@link CharacterType}),
 * their buildings and collected artifacts, the current food and prestige points, and all
 * the running modifiers granted by card effects (discounts, shaman stars and the various
 * boolean bonuses such as shaman safety/doubling or extra food/pick rights). Card strategies
 * read and update this state, and the tribe also knows how to compute the derived figures
 * needed to resolve effects (completed sets, inventor pairs) and the final end-game score.
 *
 * @see Player
 */
public class Tribe {
    // final sugli oggetti per garantire che la variabile di riferimento non possa esser riassegnata a un altro oggetto
    private final Map<CharacterType, List<CharacterCard>> characters;
    private final List<BuildingCard> buildings;
    private final Map<Artifact, Integer> artifacts;
    private int currentFood;        // cibo totale tribù
    private int foodDiscount;       // totale dello sconto sul cibo dato dai picker
    private int buildingDiscount;
    private int shamanStars;
    private boolean shamanSafety;    // indica se il Player è immune all'evento sciamanico
    private boolean shamanDoubling;
    private boolean extraFoodRight;
    private boolean extraPickRight;
    private int builderPoints;
    private int buildingPoints;
    private int currentPrestigePoints;

    /**
     * Creates an empty tribe: every character-type list is initialized empty, no buildings
     * or artifacts are owned, and all resources, modifiers and flags start at their neutral
     * values (zero / {@code false}).
     */
    public Tribe() {
        this.characters = new EnumMap<>(CharacterType.class);
        for(CharacterType t : CharacterType.values()){
            characters.put(t, new ArrayList<>());
        }
        this.buildings = new ArrayList<>();
        this.artifacts = new EnumMap<>(Artifact.class);
        this.currentFood = 0;
        this.foodDiscount = 0;
        this.currentPrestigePoints = 0;
        this.shamanStars = 0;
        this.shamanSafety = false;
        this.shamanDoubling = false;
        this.extraFoodRight = false;
        this.extraPickRight = false;
        this.builderPoints = 0;
        this.buildingPoints = 0;
    }

    /**
     * Counts how many characters of a given type the tribe owns.
     *
     * @param type the character type to count
     * @return the number of owned characters of that type (0 if none)
     */
    public int countByType(CharacterType type) {
        return characters.get(type).size();
    }

    /**
     * Adds a character to the tribe, filing it under its {@link CharacterType}.
     *
     * @param card the character card to add
     */
    public void addToTribe(CharacterCard card) {
        characters.get(card.getType()).add(card);
    }

    /**
     * Adds a building to the tribe and immediately accrues its prestige points.
     * <p>
     * The building's prestige points are accumulated into {@code buildingPoints} at purchase
     * time, so they are already accounted for when the end-game score is computed.
     *
     * @param card the building card to add
     */
    public void addToTribe(BuildingCard card) {
        buildings.add(card);
        this.buildingPoints += card.getPrestigePoints();
    }

    // setter utilizzati nelle strategy, in particolare UpdateResources, per aggiornare le risorse della tribe
    public void updateFoodDiscount(int amount) {
        this.foodDiscount += amount;
    }

    public void updateBuildingDiscount(int amount) {
        this.buildingDiscount += amount;
    }

    public void updateShamanStars(int amount) {
        this.shamanStars += amount;
    }

    public void updateBuilderPoints(int amount) {
        this.builderPoints += amount;
    }

    public void updateBuildingPoints(int amount) {
        this.buildingPoints += amount;
    }

    public void addArtifact(Artifact artifact) {
        artifacts.merge(artifact, 1, Integer::sum);
    }

    public void setShamanSafety() { this.shamanSafety = true; }

    public void setShamanDoubling() { this.shamanDoubling = true; }

    public void setExtraFoodRight(boolean extraFoodRight) { this.extraFoodRight = extraFoodRight; }

    public void setExtraPickRight() { this.extraPickRight = true; }


    // getter degli attributi di tribe, utilizzati nelle strategy degli eventi, per fare check sulla quantità
    public int getFoodDiscount() { return foodDiscount; }
    public int getBuildingDiscount() { return buildingDiscount; }
    public int getShamanStars() { return shamanStars; }
    public int getBuilderPoints() { return builderPoints; }
    public int getBuildingPoints() { return buildingPoints; }
    public boolean isShamanSafe() { return shamanSafety; }
    public boolean deservesDoubleShamanPp() { return shamanDoubling; }
    public boolean deservesExtraFood() { return extraFoodRight; }
    public boolean deservesExtraPick() { return extraPickRight; }

    /**
     * Returns the total number of characters owned, across every type.
     *
     * @return the overall count of characters in the tribe
     */
    // sostituisce il vecchio tribeSize(), getTotalCharacters() in player delega a questo
    public int getTotalCharacters(){
        return characters.values().stream()
                 .mapToInt(List::size)
                 .sum();
    }

    // metodi per statistiche del giocatore, ovvero punti e cibo
    public int getCurrentFood() { return currentFood; }
    public void updateCurrentFood(int currentFood) { this.currentFood += currentFood; }
    public int getCurrentPrestigePoints() { return currentPrestigePoints; }
    public void updateCurrentPrestigePoints(int currentPrestigePoints) { this.currentPrestigePoints += currentPrestigePoints; }

    /**
     * Pays an amount of food, falling back to prestige points when food runs short.
     * <p>
     * If the tribe holds enough food, the amount is simply deducted. Otherwise the available
     * food is consumed down to zero and the missing units are charged as a prestige point
     * loss, at a rate of {@code ppPerFood} points per missing unit.
     *
     * @param food      the amount of food to pay (must be positive)
     * @param ppPerFood the prestige points lost per unit of food that cannot be covered (must be positive)
     */
    // metodo per pagare cibo e perdere punti in caso di cibo insufficiente
    // food e ppPerFood devono essere positivi
    public void payFood(int food, int ppPerFood){
        if(this.currentFood - food >= 0) this.currentFood -= food;
        else {
            this.currentPrestigePoints += (this.currentFood - food) * ppPerFood;
            this.currentFood = 0;
        }
    }

    /**
     * Returns the number of complete character sets owned by the tribe.
     * <p>
     * A complete set contains one character of every {@link CharacterType}, so the count of
     * full sets equals the size of the smallest per-type list. If the tribe is somehow
     * missing an entry for a type, the method returns 0.
     *
     * @return the number of complete character sets (0 if at least one type is absent)
     */
    // metodo che restituisce numero di personaggi la cui occorrenza di CharacterType è presente in minore quantità all'interno della mappa characters (anche 0 se nessuno di quel tipo)
    public int minListSize() {
        if (characters.size() < CharacterType.values().length) return 0;
        return characters.values().stream()
                .mapToInt(List::size)
                .min()
                .orElse(0);
    }

    /**
     * Returns the number of inventor pairs the tribe owns.
     * <p>
     * Two artifacts of the same kind form a pair, so the total is the sum, over every
     * artifact, of its count divided by two (integer division).
     *
     * @return the number of matching artifact (inventor) pairs
     */
    // conta il numero di coppie di inventori con lo stesso Artifact
    public int countInventorPairs() {
        return artifacts.values().stream()
                .mapToInt(count -> count / 2)
                .sum();
    }

    /**
     * Finalizes the tribe's score at the end of the game by folding every deferred bonus
     * into {@code currentPrestigePoints}.
     * <p>
     * On top of the points already accumulated during play, this adds: the points granted by
     * builders and by buildings; 10 points for every pair of artists; and, for inventors,
     * the number of inventors multiplied by the number of distinct artifacts collected.
     */
    // metodo utilizzato per il calcolo dei punti finali della tribù: currentPrestigePoints + puntiInventori + puntiPicker
    public void computeTotalEndGameScore() {
        // punti edifici (accumulati all'acquisto degli edifici, tramite addToTribe(BuildingCard))
        this.currentPrestigePoints += builderPoints;
        // punti costruttori (accumulati alla selezione di un personaggio costruttore)
        this.currentPrestigePoints += buildingPoints;

        // punti artisti: 10PP per ogni coppia di artisti
        int artistsPair = countByType(CharacterType.ARTIST) / 2;
        this.currentPrestigePoints += artistsPair * 10;

        // punti inventori: numero di inventori * artefatti diversi
        int numOfInventors = countByType(CharacterType.INVENTOR);
        int numOfArtifacts = artifacts.size();
        this.currentPrestigePoints += numOfInventors * numOfArtifacts;
    }

    // getter per i test di TribeTest e per le strategy
    public Map<Artifact, Integer> getArtifacts() { return Collections.unmodifiableMap(artifacts); }
    public List<BuildingCard> getBuildings() { return Collections.unmodifiableList(buildings); }
    public Map<CharacterType, List<CharacterCard>> getCharacters() { return Collections.unmodifiableMap(characters); }

    /**
     * Captures the full state of the tribe into an immutable snapshot.
     * <p>
     * Characters and buildings are stored by their card id (to be resolved later through a
     * card map), while artifacts, resources, modifiers and flags are copied as-is.
     *
     * @return a {@link TribeSnapshot} describing the current state
     */
    public TribeSnapshot toSnapshot() {
        List<String> charactersIds = characters.values().stream()
                .flatMap(List::stream)
                .map(Card::getCardId)
                .toList();

        List<String> buildingIds = buildings.stream()
                .map(Card::getCardId)
                .toList();

        Map<String, Integer> artifactSnapshot = artifacts.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(), Map.Entry::getValue
                ));

        return new TribeSnapshot(
                charactersIds,
                buildingIds,
                artifactSnapshot,
                currentFood,
                currentPrestigePoints,
                shamanStars,
                foodDiscount,
                shamanSafety,
                shamanDoubling,
                extraFoodRight,
                extraPickRight,
                buildingDiscount,
                builderPoints,
                buildingPoints
        );
    }

    /**
     * Rebuilds a tribe from a snapshot, resolving card ids through the given card map.
     * <p>
     * Character and building ids are looked up in {@code cardMap} and re-filed into the new
     * tribe, artifacts are converted back to their enum keys, and every resource, modifier
     * and flag is restored from the snapshot.
     *
     * @param snapshot the snapshot describing the persisted tribe state
     * @param cardMap  a lookup from card id to the corresponding {@link Card} instance
     * @return the reconstructed {@link Tribe}
     */
    public static Tribe fromSnapshot(TribeSnapshot snapshot, Map<String, Card> cardMap) {
        Tribe tribe = new Tribe();

        for (String id : snapshot.getCharacterCardIds()) {
            CharacterCard card = (CharacterCard) cardMap.get(id);
            tribe.characters.get(card.getType()).add(card);
        }

        for (String id : snapshot.getBuildingCardIds()) {
            tribe.buildings.add((BuildingCard) cardMap.get(id));
        }

        snapshot.getArtifacts().forEach((key, val) ->
                tribe.artifacts.put(Artifact.valueOf(key), val)
        );

        tribe.currentFood = snapshot.getCurrentFood();
        tribe.currentPrestigePoints = snapshot.getCurrentPrestigePoints();
        tribe.shamanStars = snapshot.getShamanStars();
        tribe.buildingDiscount = snapshot.getBuildingDiscount();
        tribe.builderPoints = snapshot.getBuilderPoints();
        tribe.buildingPoints = snapshot.getBuildingPoints();
        tribe.foodDiscount = snapshot.getFoodDiscount();
        tribe.shamanSafety = snapshot.getShamanSafety();
        tribe.shamanDoubling = snapshot.getShamanDoubling();
        tribe.extraFoodRight = snapshot.getExtraFoodRight();
        tribe.extraPickRight = snapshot.getExtraPickRight();

        return tribe;
    }
}
