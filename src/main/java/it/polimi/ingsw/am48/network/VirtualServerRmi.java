package it.polimi.ingsw.am48.network;

import java.rmi.Remote;
import java.rmi.RemoteException;

/*
* Estende Remote per (obbligo di) RMI e VirtualServer.
* Stessi metodi overridati per lanciare RemoteException (per RMI).
* Aggiunto il metodo connect, necessario per RMI.
*/
public interface VirtualServerRmi extends Remote, VirtualServer{
    void connect(String nickname, VirtualViewRmi client) throws RemoteException;
    @Override void joinGame(int numPlayers, String nickname) throws RemoteException;
    @Override void placeTotem(String nickname, char position) throws RemoteException;
    @Override void takeCard(String nickname, String cardId) throws RemoteException;
    void ping() throws RemoteException;
}
