import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The {@code ComputeServerImpl} class implements the {@link ComputeServer} interface
 * to provide synchronous and asynchronous computation services via RMI (Remote Method Invocation).
 * It extends {@link UnicastRemoteObject} to allow remote access.
 * 
 */
public class ComputeServerImpl extends UnicastRemoteObject implements ComputeServer {
   
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private final ConcurrentHashMap<String, Future<?>> resultStore = new ConcurrentHashMap<>();

    private final AtomicInteger taskIdCounter = new AtomicInteger(0);

    /**
     * Constructs a new {@ComputeServerImpl ComputeServerImpl} object and exports it to accept incoming RMI calls.
     * 
     * @throws RemoteException if the export to the RMI runtime fails.
     */
    protected ComputeServerImpl() throws RemoteException {
        super();
    }

    /**
     * Adds two integers and returns the result.
     * 
     * @param i the first integer.
     * @param j the second integer.
     * @return the sum of {@Integer i} and {@Integer j}.
     * @throws RemoteException if a remote communication error occurs.
     */
    @Override
    public int add(int i, int j) throws RemoteException {
        return i + j;
    }

    /**
     * Sorts a list of integers in ascending order.
     * 
     * @param array the list of integers to be sorted.
     * @return the sorted list of integers.
     * @throws RemoteException if a remote communication error occurs.
     */
    @Override
    public List<Integer> sort(List<Integer> array) throws RemoteException {
        array.sort(Integer::compareTo);
        return array;
    }

    /**
     * Initiates an asynchronous addition of two integers and returns a unique task ID to track the result.
     * 
     * @param i the first integer.
     * @param j the second integer.
     * @return {@link String} a unique task ID for the asynchronous addition operation.
     * @throws RemoteException if a remote communication error occurs.
     */
    @Override
    public String addAsync(int i, int j) throws RemoteException {
        Callable<Integer> task = () -> add(i, j);
        Future<Integer> future = executor.submit(task);
        String taskId = generateTaskId();
        resultStore.put(taskId, future);
        return taskId;
    }

    /**
     * Initiates an asynchronous sorting of a list of integers and returns a unique task ID to track the result.
     * 
     * @param array the list of integers to be sorted.
     * @return {@link String} a unique task ID for the asynchronous sorting operation.
     * @throws RemoteException if a remote communication error occurs.
     */
    @Override
    public String sortAsync(List<Integer> array) throws RemoteException {
        Callable<List<Integer>> task = () -> sort(array);
        Future<List<Integer>> future = executor.submit(task);
        String taskId = generateTaskId();
        resultStore.put(taskId, future);
        return taskId;
    }

    /**
     * Retrieves the result of an asynchronous operation identified by the given task ID.
     * If the task is not yet complete, {@code null} is returned.
     * 
     * @param taskId {@link String} the unique task ID associated with the asynchronous operation.
     * @return the result of the asynchronous operation, or {@code null} if the task is not yet completed.
     * @throws RemoteException if an error occurs while retrieving the result.
     */
    @Override
    public Object getResult(String taskId) throws RemoteException {
        Future<?> future = resultStore.get(taskId);
        if (future != null && future.isDone()) {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RemoteException("Error getting result", e);
            }
        }
        return null;
    }

    /**
     * Generates a unique task ID for asynchronous operations.
     * 
     * @return a unique task ID as a {@link String}.
     */
    private String generateTaskId() {
        return String.valueOf(taskIdCounter.incrementAndGet());
    }

    /**
     * Shuts down the executor service, preventing new tasks from being submitted.
     */
    public void shutdown() {
        executor.shutdown();
    }
}
