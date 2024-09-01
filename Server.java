import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.yaml.snakeyaml.Yaml;

public class Server {

    private static final String CONFIG_FILE_NAME = "config.yaml";

    
    public static void main(String[] args) {
        try {
            InputStream inputStream = new FileInputStream(new File(CONFIG_FILE_NAME));
            var yaml = new Yaml();
            Map<String, Object> data = yaml.load(inputStream);
            
            String fileServerDir = Util.getServerDir(data);
            Integer port = Util.getServerPort(data);
            createDirIfNotExists(fileServerDir);
            spawnServerThreads(port, fileServerDir, Util.getComputeServerPort(data), Util.getComputeServerPath(data));
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

    private static void spawnServerThreads(int fsPort, String serverDirName, int computePort, String computeServerPath) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(() -> {
            try {
                FileServer fileServer = new FileServerImpl(serverDirName);
                LocateRegistry.createRegistry(fsPort);
                String serverPath = "rmi://localhost:" + fsPort+"/fileOp";
                System.out.println("Running file server at " +  serverPath);
                Naming.rebind(serverPath, fileServer);
            } catch (RemoteException e) {
                System.out.println("Error spawning thread to create File Server");
                e.printStackTrace();
            } catch (MalformedURLException malformedURLException) {
                System.out.println("Invalid url, cannot run server");
            } 
        });

        executorService.submit(() -> {
            try {
                ComputeServer computeServer = new ComputeServerImpl();
                LocateRegistry.createRegistry(computePort);
                String serverPath = "rmi://localhost:" + computePort+"/compute";
                System.out.println("Running compute server at " +  serverPath);
                Naming.rebind(serverPath, computeServer);
            } catch (RemoteException e) {
                System.out.println("Error spawning thread to create Compute Server");
            } catch (MalformedURLException malformedURLException) {
                System.out.println("Invalid url, cannot run server");
            } 
        });

        executorService.shutdown();
    }
}
