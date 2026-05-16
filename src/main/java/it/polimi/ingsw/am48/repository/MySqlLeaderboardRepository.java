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

public class MySqlLeaderboardRepository implements LeaderboardRepository{

    private final HikariDataSource dataSource;

    /*
    * loadProperties() reads db.properties file, and returns a Properties object.
    * HikariConfig is the configuration object: we are telling him where is the DB (url), what's the username and password,
    * and how many connections are needed in the pool.
    *
    * HikariDataSource(config) creates the real pool: now HikariCP is able to open connections with MySql
    * */
    public MySqlLeaderboardRepository(){
        Properties props = loadProperties();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setMaximumPoolSize(5);
        this.dataSource = new HikariDataSource(config);
    }


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

    // Whenever the server is turned off, every connection with MySql is closed
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
