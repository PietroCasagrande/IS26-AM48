CREATE DATABASE mesos_db;
USE mesos_db;

CREATE TABLE game_results (
                              id          INT AUTO_INCREMENT PRIMARY KEY,
                              game_id     VARCHAR(50)  NOT NULL,
                              nickname    VARCHAR(50)  NOT NULL,
                              final_score INT          NOT NULL,
                              num_players INT          NOT NULL,
                              game_date   DATETIME     NOT NULL
);

-- Optimization to run getLeaderboard(int numPlayers) faster
CREATE INDEX idx_num_players_score
    ON game_results(num_players, final_score DESC);