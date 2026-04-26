package it.polimi.ingsw.am48.network;

import it.polimi.ingsw.am48.model.delta.GameDelta;
import it.polimi.ingsw.am48.model.snapshot.GameSnapshot;

import java.rmi.Remote;
import java.rmi.RemoteException;

/*
* Stessa logica di VirtualServerRmi, estende Remote e l'interfaccia technology-agnostic.
*/
public interface VirtualViewRmi extends Remote, VirtualView{
    @Override void showGameDelta(GameDelta delta) throws RemoteException;
    @Override void showInitialSnapshot(GameSnapshot snapshot) throws RemoteException;
    @Override void reportError(String errorMessage) throws RemoteException;
}
