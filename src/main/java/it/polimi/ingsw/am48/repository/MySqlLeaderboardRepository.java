package it.polimi.ingsw.am48.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.polimi.ingsw.am48.model.game.GameResult;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * MySQL implementation of {@link LeaderboardRepository}, backed by a HikariCP connection pool.
 *
 * <p>On construction, this class reads database connection parameters from a
 * {@code db.properties} file on the classpath (see {@code db.properties.example} for the
 * required format) and initialises a pool of up to five connections. All queries use
 * {@link PreparedStatement} to prevent SQL injection.
 *
 * <p>The underlying table schema is defined in {@code schema.sql}. In particular, a composite
 * index on {@code (num_players, final_score DESC)} ensures that {@link #getLeaderboard} remains
 * efficient even for large result sets.
 *
 * <p>Call {@link #close()} when the server shuts down to release all pooled connections.
 */
public class MySqlLeaderboardRepository implements LeaderboardRepository{

    private final HikariDataSource dataSource;

    /**
     * Constructs the repository by reading {@code db.properties} from the classpath and
     * initialising a HikariCP connection pool configured with the URL, credentials, and a
     * maximum pool size of 5.
     *
     * @throws RuntimeException if {@code db.properties} is missing or cannot be parsed,
     *                          or if the pool cannot establish a connection to MySQL
     */
    public MySqlLeaderboardRepository(){
        Properties props = loadProperties();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setMaximumPoolSize(5);
        this.dataSource = new HikariDataSource(config);
    }

    /**
     * Reads database connection properties from {@code db.properties} on the classpath.
     *
     * @return a {@link Properties} object populated with at least {@code db.url},
     *         {@code db.username}, and {@code db.password}
     * @throws RuntimeException if the file is not found or an I/O error occurs while reading it
     */
    private Properties loadProperties() {
        Properties props = new Properties();
        // looks for db.properties file in src/main/resources path
        // returns an InputStream, which is a byte stream
        try(InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if(input == null) {
                throw new RuntimeException("db.properties not found in classpath");
            }
            // props.load(input) reads the file and populates the Properties object
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error in db.properties loading", e);
        }
        return props;
    }

    /**
     * Inserts one row into the {@code game_results} table for the given player result.
     *
     * <p>Uses a {@link PreparedStatement} with positional placeholders to prevent SQL injection.
     * The {@link java.time.LocalDateTime} timestamp is converted to a JDBC {@link Timestamp}
     * before being bound to the statement.
     *
     * @param result the game result to persist; must not be {@code null}
     * @throws RuntimeException wrapping the underlying {@link SQLException} if the INSERT fails
     */
    @Override
    public void saveResult(GameResult result) {
        String sql = "INSERT INTO game_results " +
                     "(game_id, nickname, final_score, num_players, game_date) " +
                     "VALUES (?, ?, ?, ?, ?)";  // question marks are placeHolders needed to avoid SQL injections

        // takes a connection from HikariCP's pool and compiles the sql query on the db
        try(Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            // setters substitute question marks in the sql query
            stmt.setString(1, result.getGameId());
            stmt.setString(2, result.getPlayerNickname());
            stmt.setInt(3, result.getFinalScore());
            stmt.setInt(4, result.getNumPlayers());
            stmt.setTimestamp(5, Timestamp.valueOf(result.getGameDate()));  // converts java's LocalDateTime into JDBC's Timestamp
            // executes the INSERT on the DB (it's an update and not a query because it doesn't return data)
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error in result's saving on the DB");
        }
    }

    /**
     * Queries the {@code game_results} table for all rows matching the given player count,
     * ordered by {@code final_score} descending, and maps each row to a {@link GameResult}.
     *
     * <p>The query leverages the composite index on {@code (num_players, final_score DESC)}
     * defined in {@code schema.sql} to avoid a full-table scan.
     *
     * @param numPlayers the number of players to filter by
     * @return an ordered list of {@link GameResult} entries; never {@code null}, may be empty
     * @throws RuntimeException wrapping the underlying {@link SQLException} if the query fails
     */
    @Override
    public List<GameResult> getLeaderboard(int numPlayers) {
        String sql = "SELECT game_id, nickname, final_score, num_players, game_date " +
                     "FROM game_results " +
                     "WHERE num_players = ? " +
                     "ORDER BY final_score DESC";

        List<GameResult> leaderboard = new ArrayList<>();

        // takes a connection from HikariCP's pool and compiles the sql query on the db (same as SaveResult)
        try(Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, numPlayers);
            // returns a ResultSet (pointer to the rows obtained after the SELECT query)
            ResultSet rs = stmt.executeQuery();

            // rs.next() moves the pointer ahead: for each row, it reads the data, and builds a GameResult with it, which is then added to the leaderboard arrayList
            while(rs.next()) {
                leaderboard.add(new GameResult(
                        rs.getString("game_id"),
                        rs.getString("nickname"),
                        rs.getInt("final_score"),
                        rs.getInt("num_players"),
                        rs.getTimestamp("game_date").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error in leaderboard recover from DB", e);
        }

        return leaderboard;
    }

    /**
     * Closes the HikariCP connection pool, releasing all pooled connections to MySQL.
     * Should be called exactly once when the server shuts down.
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
