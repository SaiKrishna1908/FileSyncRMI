import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * API for Compute Server, supports add & sort
 */
public interface ComputeServer extends Remote {
    int add(int a, int b) throws RemoteException;
    List<Integer> sort(List<Integer> array) throws RemoteException;
    String addAsync(int a, int b) throws RemoteException;
    String sortAsync(List<Integer> array) throws RemoteException;
    Object getResult(String taskId) throws RemoteException;
}
