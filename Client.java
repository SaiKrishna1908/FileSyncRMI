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

public class Client {

    private static final String CONFIG_FILE_NAME = "config.yaml";

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

    private static byte[] readFile(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return fis.readAllBytes();
        }
    }

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
