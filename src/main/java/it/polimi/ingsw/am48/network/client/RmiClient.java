package it.polimi.ingsw.am48.network.client;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;
import it.polimi.ingsw.am48.network.VirtualServer;
import it.polimi.ingsw.am48.network.VirtualServerRmi;
import it.polimi.ingsw.am48.network.VirtualViewRmi;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RmiClient extends UnicastRemoteObject implements VirtualViewRmi, VirtualServer {
    /*
    * Il client RMI fa due cose:
    * 1. è un proxy del server -> il client chiama i suoi metodi per mandare comandi
    * 2. riceve il callback -> il server chiama i suoi metodi per mandare aggiornamenti
    */

    private final VirtualServerRmi server;
    private final ClientModel model;

    public RmiClient(String host, int port, ClientModel model) throws RemoteException, NotBoundException {
        super();
        Registry registry = LocateRegistry.getRegistry(host, port);
        this.server = (VirtualServerRmi) registry.lookup("MesosServer");
        this.model = model;
    }

    // VirtualServer: comandi verso il server dal client
    @Override
    public void joinGame(int numPlayers, String nickname) throws Exception{
        server.connect(nickname, this);
        server.joinGame(numPlayers, nickname);
    }

    @Override
    public void placeTotem(String nickname, char position) throws Exception{
        server.placeTotem(nickname, position);
    }

    @Override
    public void takeCard(String nickname, String cardId) throws Exception{
        server.takeCard(nickname, cardId);
    }


    // VirtualView: callback dal server
    @Override
    public void showGameDelta(GameDelta delta) throws RemoteException{
        model.applyDelta(delta);
    }

    @Override
    public void showInitialSnapshot(GameSnapshot snapshot) throws RemoteException{
        model.setInitialState(snapshot);
    }

    @Override
    public void reportError(String errorMessage) throws RemoteException {
        model.notifyError(errorMessage);
    }


}
