# Mesos — Software Engineering Final Project (AM48)

Software implementation of the board game **Mesos**, developed for the *Prova Finale di Ingegneria del Software* (Politecnico di Milano, A.Y. 2025/2026).

The project is a distributed, multiplayer **client–server** application built on the **Model–View–Controller** pattern. The server holds the authoritative game state and enforces the rules; clients connect over **Socket** or **RMI** and play through either a **TUI** (terminal) or a **JavaFX GUI**.

## Team — Group AM48

- [Luca Maria Brotto](https://github.com/LucaMariaBrotto)
- [Pietro Casagrande](https://github.com/PietroCasagrande)
- [Alessio Casati](https://github.com/alessiocasati)
- [Simone Clerico](https://github.com/simoneclerico)

## Implemented features

| Requirement | Status |
|---|---|
| Complete rules (full 2–5 player game) | ✅ |
| Socket connection | ✅ |
| RMI connection | ✅ |
| TUI (terminal interface) | ✅ |
| GUI (JavaFX) | ✅ |
| **AF — Multiple games** (concurrent matches on one server) | ✅ |
| **AF — Persistence** (server crash recovery from disk) | ✅ |
| **AF — Leaderboard on DB** (MySQL match history) | ✅ |

The three advanced features delivered are **Multiple games**, **Persistence** and **Leaderboard on DB**. On top of these, the server also handles in-game player disconnections gracefully (see [Disconnection handling](#disconnection-handling)); this is provided as system robustness and is **not** counted among the three advanced features.

> A single client and a single server may use **different transports** within the same match (a Socket player and an RMI player can play together), since the networking layer is fully transport-agnostic.

## Architecture overview

The codebase follows a strict **MVC** separation, with a transport-agnostic networking layer in between.

```
       CLIENT                            NETWORK                       SERVER
┌───────────────────┐            ┌─────────────────┐          ┌───────────────────┐
│                   │  commands  │  VirtualServer  │ actions  │   GameController  │
│       View        │ ---------> │  (Socket / RMI) │ -------> │         │         │
│         ^         │            └─────────────────┘          │         v         │
│         │ observer│            ┌─────────────────┐          │    GameManager    │
│    ClientModel    │ snapshots  │   VirtualView   │snapshots │         │         │
│                   │ <--------- │  (Socket / RMI) │ <--------│         v         │
│                   │  / deltas  └─────────────────┘ / deltas │   Game (Model)    │
└───────────────────┘                                         └───────────────────┘
```

The **Model** (`Game` and its aggregates) is pure: it holds state and enforces rules, and has no knowledge of networking or the views. The **`GameController`** is a thin mediator that translates network actions into model calls; the **views** observe a local replica (`ClientModel`) and never touch the server state directly. `GameManager` owns the lobbies and the live sessions, which is what makes concurrent matches possible (see below).

### State synchronization: Snapshot + Delta

Game state is never sent as a mutable shared object. Instead:

- a **`GameSnapshot`** is an immutable, fully serializable capture of the whole game state. It is sent once when a game starts, and again to every client when the server recovers from a crash and reloads the match from disk;
- a **`GameDelta`** is a small, atomic state change (totem placed, card picked, turn ended, …) broadcast after every move. Each delta knows how to `applyTo(ClientModel)`, mutating the client's local replica.

This keeps network traffic minimal during play while guaranteeing that every client can rebuild an exact replica of the server state. Both snapshots and deltas are serialized with **Jackson polymorphic JSON** (a `"type"` field selects the concrete subclass), which is also the foundation of the crash-recovery persistence.

### Transport abstraction

The model and the views never depend on a concrete transport. Two thin interfaces decouple them:

- **`VirtualServer`** — the server as seen by a client (send `joinGame` / `placeTotem` / `takeCard`);
- **`VirtualView`** — a client as seen by the server (push `showInitialSnapshot` / `showGameDelta` / `reportError`).

Each interface has a Socket and an RMI implementation. The server-side hub `MesosServer` holds one `VirtualView` per connected nickname and broadcasts updates without knowing the underlying technology.

### Concurrency model

The server runs **multiple matches simultaneously** (one of the advanced features). Each match is an independent session managed by `GameManager`; client requests are dispatched to the right `Game`, and access to a match's mutable state is synchronized so that concurrent moves from different players are serialized per match. Lobby join/leave and nickname allocation are likewise guarded, so two clients can never claim the same nickname or join a full game.

### Disconnection handling

When a player disconnects mid-match (Socket drop or RMI host unreachable), the server does **not** stall the game for the others:

- the disconnected player's match is terminated cleanly, its on-disk snapshot is deleted, and its nicknames are freed for reuse;
- the remaining players are notified that the game has ended because a player left;
- RMI disconnections are detected with a server-side **heartbeat** (`ScheduledExecutorService` ping), since a dead RMI client does not otherwise signal its absence;
- if the **server** crashes, clients detect the loss, display reconnection instructions and poll for the server's return; on restart the server reloads the persisted match and re-pushes the snapshot. A `gameEnded` flag in `ClientModel` prevents a normal end-of-game from being misreported as a crash.

> **Scope / limitation.** This is *graceful handling of a disconnection from the other players' point of view*, plus server crash recovery — **not** the "resilience to disconnections" advanced feature. A client that disconnects from a live match **cannot rejoin that match**. We chose to document this explicitly rather than overstate it.

### Design patterns

- **MVC** — overall system decomposition (`model` / `view` / `controller`).
- **State** — `GamePhase` and its implementations (`WaitingForPlayers`, `PlaceTotem`, `PlayerOffer`, `EndTurn`, `EndGame`); a `Game` delegates every action to its current phase, which validates it and drives the transitions.
- **Strategy** — `CardStrategy` models every card effect, decoupling the effect from the card type. Effects are either immediate or registered for later triggering.
- **Observer** — server-side `NotificatorCenter` fires card effects on game events; client-side `ModelObserver` lets the view react to `ClientModel` changes.
- **Factory / Builder** — the `model.factory` package builds the board, decks and card maps from the static game data.
- **Memento (Snapshot)** — `toSnapshot()` / `fromSnapshot()` enable persistence and server-side crash recovery.

### Source layout

```
src/main/java/it/polimi/ingsw/am48/
├── ClientMain.java / LauncherClient.java   # client entry points
├── controller/        # GameController — thin mediator between network and model
├── model/
│   ├── game/          # Game (aggregate root), GameManager (lobbies & sessions)
│   ├── board/         # Board, decks, offer track
│   ├── card/          # Building / Character / Event cards
│   ├── strategy/      # card effects (Strategy pattern)
│   ├── phase/         # game phases (State pattern)
│   ├── notificator/   # effect-trigger event bus (Observer)
│   ├── player/        # Player, Tribe, PlayerContext
│   ├── factory/       # builders for board, decks, cards
│   ├── snapshot/      # immutable full-state captures (persistence/recovery)
│   ├── delta/         # incremental state updates
│   └── enums/
├── network/
│   ├── server/        # ServerMain, MesosServer, Socket & RMI server handlers
│   ├── client/        # ClientModel, Socket & RMI client handlers
│   └── messages/      # JSON commands & notifications
├── repository/        # persistence: JSON game saves + MySQL leaderboard
├── view/
│   ├── tui/           # terminal interface
│   └── gui/           # JavaFX interface (FXML controllers)
├── dto/  /  utils/  /  exception/
```

## Communication protocol

The full client–server protocol — the command/notification message catalogue, the join/offer/turn sequences, and the snapshot-then-delta synchronization flow — is documented separately as part of the final delivery (sequence diagrams + message reference). See [`docs/protocol.md`](docs/protocol.md).

## Requirements

- **JDK 25** — the project targets Java 25 (see `pom.xml`); **every machine used for the demo must have JDK 25 installed**, the JARs will not run on an older JDK.
- **Apache Maven 3.8+** (or the bundled wrapper, see below)
- **MySQL 8** (server side only, only for the leaderboard feature)

## Build & run

The build produces two self-contained "fat" JARs via the Maven Shade plugin. JavaFX native libraries are platform-specific, so select the profile for your OS: `windows`, `linux`, `mac` (Intel) or `mac-arm` (Apple Silicon).

```bash
# Build both JARs for your platform (example: Windows)
mvn clean package -P windows
```

> Maven does not need to be installed: the bundled wrapper (`./mvnw` on Linux/macOS, `mvnw.cmd` on Windows) can be used in place of `mvn`.

This generates `target/mesos-server.jar` and `target/mesos-client.jar`.

### Server

```bash
java -jar target/mesos-server.jar
```

Listens on **Socket port 12345** and **RMI port 1099**. On startup it reloads any game persisted from a previous run (crash recovery). A reachable MySQL instance with a valid `db.properties` is required for the leaderboard (see below).

### Client

```bash
java -jar target/mesos-client.jar
```

At launch the client asks for the server host, the transport (Socket / RMI) and the interface (TUI / GUI). Multiple clients can run on the same machine.

> **Distributed play over RMI.** RMI needs the server to call back to each client, so on multi-machine setups the client must advertise the IP the server can actually reach. If RMI fails to connect across machines, start the client with:
> ```bash
> java -Djava.rmi.server.hostname=<CLIENT_IP> -jar target/mesos-client.jar
> ```
> RMI also requires bidirectional TCP, so it can fail behind firewalled / NAT'd networks (e.g. some university or mobile-hotspot networks); **Socket** is the reliable fallback for distributed testing.

## Database setup (leaderboard feature)

1. Create the schema:
   ```bash
   mysql -u root -p < src/main/resources/schema.sql
   ```
2. Copy `src/main/resources/db.properties.example` to `src/main/resources/db.properties` and fill in your credentials:
   ```properties
   db.url=jdbc:mysql://localhost:3306/mesos_db
   db.username=<your-username>
   db.password=<your-password>
   ```

`db.properties` is git-ignored; each developer runs a local MySQL instance. The server uses a **HikariCP** connection pool and **prepared statements** for all queries. At the end of each match every player's final score is stored, and the client shows the player's ranking among all matches with the same number of players.

## Testing

Unit and integration tests use **JUnit 5**, **Mockito** and **AssertJ**, covering the model logic, the phase/strategy machinery, snapshot/delta serialization and the Socket/RMI networking layer.

```bash
mvn test
```

## Documentation

- **Javadoc** — generated from the source (`mvn javadoc:javadoc`).
- **UML diagrams** (high-level and detailed) and the **client–server communication protocol** documentation are part of the final delivery.

---

*Politecnico di Milano — Prova Finale di Ingegneria del Software, A.Y. 2025/2026.*
