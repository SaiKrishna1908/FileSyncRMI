import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.yaml.snakeyaml.Yaml;

/**
 * The {@code Client} class is responsible for initializing either a file server or a compute server
 * based on the input parameters. It reads configuration from a YAML file and starts threads to handle
 * directory watching for the file server or computation tasks for the compute server using RMI (Remote Method Invocation).
 */
public class Client {

    /**
     * Name of the YAML configuration file.
     */
    private static final String CONFIG_FILE_NAME = "config.yaml";

    /**
     * The main method is the entry point of the application. It reads the configuration from
     * {@file config.yaml} and determines whether to connect to the compute server or the file server.
     * 
     * @param args Command line arguments. If the first argument is {@String "computeServer"}, it connects to
     *             the compute server. Otherwise, it connects to the file server.
     */
    public static void main(String[] args) {
        try {
            boolean connectToComputeServer = (args.length > 0) && args[0].equals("computeServer");
            InputStream inputStream = new FileInputStream(new File(CONFIG_FILE_NAME));
            var yaml = new Yaml();
            Map<String, Object> data = yaml.load(inputStream);

            if (connectToComputeServer) {
                spawnComputeServerThread(Util.getComputeServerAddr(data), Util.getComputeServerPath(data), Util.getComputeServerPort(data));
            } else {
                String fileClientDir = Util.getClientDir(data);
                createDirIfNotExists(fileClientDir);

                Integer timeOut = Util.getTimeOut(data);
                spawnFileServerThread(fileClientDir, timeOut, Util.getServerAddr(data), Util.getServerPath(data), Util.getServerPort(data));
            }
        } catch (IOException ioException) {
            System.out.println("Could not read config file");
        }
    }

    /**
     * creates a file if it does not exists
     * @param fileServerDir file to create
     */
    private static void createDirIfNotExists(String fileServerDir) {
        Path path = Paths.get(fileServerDir);

        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path);
                System.out.println("Directory created: " + path);
            } else {
                System.out.println("Directory already exists: " + path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Responsible for sending rmi requests and also watching directory
     * @param directoryToWatch the directory to watch changes for
     * @param timeOut time interval to check for changes
     * @param serverAddr remote server address. (localhost by default see {@Code config.yaml})
     * @param fileServerPath remote server file path. (See {@Code config.yaml}) 
     * @param serverPort port of the remote server. (See {@Code config.yaml})
     */
    private static void spawnFileServerThread(String directoryToWatch, int timeOut, String serverAddr, String fileServerPath,
            int serverPort) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(() -> {
            DirectoryWatcher directoryWatcher = new DirectoryWatcher(directoryToWatch, timeOut);
            String path = "rmi://" + serverAddr + ":" + serverPort + fileServerPath;
            System.out.println("Connecting to server at: " + path);
            FileServer fileServer = null;
            try {
                fileServer = (FileServer) Naming.lookup(path);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            while (true) {
                List<WatchEvent<?>> events = directoryWatcher.watch();
                if (events != null && !events.isEmpty()) {
                    for (WatchEvent<?> event : events) {
                        System.out.println("Event kind: " + event.kind() + ". File affected: " + event.context() + ".");
                        System.out.println(event.kind().toString());
                        System.out.println();
                        String filePath = Paths.get(directoryToWatch + "/" + event.context()).toAbsolutePath().toString();
                        sync(event.kind().toString(), filePath, fileServer);
                    }
                } else {
                    System.out.println("No changes detected in the directory.");
                }

            }
        });

        executorService.shutdown();
    }

    /**
     * Sync's changes with remote server. Invoked when any changes in directory changes
     * @param operation file operation to perform on the server side
     * @param fileName fileName for the server to create
     * @param fileServer FileServer obj. This is a rmi object
     */
    private static void sync(String operation, String fileName, FileServer fileServer) {
        if (operation.equals("ENTRY_CREATE") || operation.equals("ENTRY_MODIFY")) {
            try{
                fileServer.uploadFile(readFile(fileName), Util.extractFileNameFromPath(fileName));
            } catch( IOException ioException) {
                System.out.println("Error reading file for upload");
            }
        } else if (operation.equals("ENTRY_DELETE")) {
            try {
                fileServer.delete(Util.extractFileNameFromPath(fileName));
            } catch (RemoteException e) {
                System.out.println("Error deleting file");
            }
        }
    }

    /**
     * Read file from file-system
     * @param filePath file name to read
     * @return {@link byte[]} 
     * @throws IOException
     */
    private static byte[] readFile(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return fis.readAllBytes();
        }
    }

    /**
     * Spawns thread to perform rmi calls to compute server
     * @param serverAddr Server address 
     * @param serverPath Server path
     * @param serverPort Server port
     */
    private static void spawnComputeServerThread(String serverAddr, String serverPath, int serverPort) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
            System.out.println("Press 1 to sync add two numbers");
            System.out.println("Press 2 to sort an array");
            System.out.println("Press 3 to async add two numbers");
            System.out.println("Press 4 to async sort an array");
            ComputeServer computeServer = null;
            try {
                String path = "rmi://" + serverAddr + ":" + serverPort + serverPath;
                System.out.println("Connecting to server at: " + path);
                computeServer = (ComputeServer) Naming.lookup(path);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            while(true) {
                System.out.println("Select your option");
                try {
                    String option = reader.readLine();
                    Integer intOption = Integer.parseInt(option);
                    handleOption(intOption, computeServer, reader);
                } catch(NumberFormatException numberFormatException) {
                    System.out.println("Invalid option. Select 1, 2, 3 or 4");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        executorService.shutdown();
    }

    /**
     * Helper method to perform
     * @param intOption
     * @param computeServer
     * @param bufferedReader
     * @throws IOException
     * @throws InterruptedException
     */
    private static void handleOption(Integer intOption, ComputeServer computeServer, BufferedReader bufferedReader) throws IOException, InterruptedException {
       switch (intOption) {
        case 1:
            System.out.println("Enter a and b with space seperation");
            String chunks[] = bufferedReader.readLine().split(" ");
            Integer result = computeServer.add(Integer.parseInt(chunks[0]), Integer.parseInt(chunks[1]));
            System.out.println("Computation Result "+ result);
            break;
        case 2:
            System.out.println("Enter numbers to sort using space seperation");
            String syncList[] = bufferedReader.readLine().split(" ");

            List<Integer> arrayList = new ArrayList<>();
            for(String number : syncList) {
                arrayList.add(Integer.parseInt(number));
            }
            List<Integer> syncArrayList = computeServer.sort(arrayList);
            System.out.println("Computation Result "+ syncArrayList);
            break;
        case 3:
            System.out.println("Enter a and b with space seperation");
            String asyncList[] = bufferedReader.readLine().split(" ");
            String asyncAddResult = computeServer.addAsync(Integer.parseInt(asyncList[0]), Integer.parseInt(asyncList[1]));
            System.out.println("Async Computation Task Id: "+ asyncAddResult);
            System.out.println("Sleeping thread for 3 seconds");
            Thread.sleep(2000);
            Integer addResult = (Integer) computeServer.getResult(asyncAddResult);
            System.out.println("Asynchronous add result: " + addResult);
            break;
        case 4:
            System.out.println("Enter numbers to sort using space seperation");
            String asyncSortList[] = bufferedReader.readLine().split(" ");
            List<Integer> asyncArrayList = new ArrayList<>();
            for(String number : asyncSortList) {
                asyncArrayList.add(Integer.parseInt(number));
            }
            String asyncSortResultTaskId = computeServer.sortAsync(asyncArrayList);
            System.out.println("Async Computation Task Id: "+ asyncSortResultTaskId);
            System.out.println("Sleeping thread for 3 seconds");
            Thread.sleep(2000);
            List<Integer> asyncSortResult = (List<Integer>) computeServer.getResult(asyncSortResultTaskId);
            System.out.println("Asynchronous add result: " + asyncSortResult);
            break;
        default:
            break;
       }
    }

}
