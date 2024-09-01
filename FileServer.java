import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileServer extends Remote {
    void uploadFile(byte[] file, String fileName) throws RemoteException;

    void delete(String fileName) throws RemoteException;
}
