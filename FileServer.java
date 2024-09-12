import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * API for File Server, supports upload and delete file
 */
public interface FileServer extends Remote {
    void uploadFile(byte[] file, String fileName) throws RemoteException;

    void delete(String fileName) throws RemoteException;
}
