package it.polimi.ingsw.am48.network;

public interface VirtualServer {
    void joinGame(int numPlayers, String nickname) throws Exception;
    void placeTotem(String nickname, char position) throws Exception;
    void takeCard(String nickname, String cardId) throws Exception;
}
