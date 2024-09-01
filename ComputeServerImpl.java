import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ComputeServerImpl extends UnicastRemoteObject implements ComputeServer {
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final ConcurrentHashMap<String, Future<?>> resultStore = new ConcurrentHashMap<>();
    private final AtomicInteger taskIdCounter = new AtomicInteger(0);

    protected ComputeServerImpl() throws RemoteException {
        super();
    }

    @Override
    public int add(int i, int j) throws RemoteException {
        return i + j;
    }

    @Override
    public List<Integer> sort(List<Integer> array) throws RemoteException {
        array.sort(Integer::compareTo);
        return array;
    }

    @Override
    public String addAsync(int i, int j) throws RemoteException {
        Callable<Integer> task = () -> add(i, j);
        Future<Integer> future = executor.submit(task);
        String taskId = generateTaskId();
        resultStore.put(taskId, future);
        return taskId;
    }

    @Override
    public String sortAsync(List<Integer> array) throws RemoteException {
        Callable<List<Integer>> task = () -> sort(array);
        Future<List<Integer>> future = executor.submit(task);
        String taskId = generateTaskId();
        resultStore.put(taskId, future);
        return taskId;
    }

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

    private String generateTaskId() {
        return String.valueOf(taskIdCounter.incrementAndGet());
    }

    public void shutdown() {
        executor.shutdown();
    }
}
