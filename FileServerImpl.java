import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implementation of {@link FileServer}
 */
public class FileServerImpl extends UnicastRemoteObject implements FileServer {

    private final String serverDirName;
    
    protected FileServerImpl(String serverDirName) throws RemoteException {
        super();
        this.serverDirName = serverDirName;
    }

    @Override
    public void uploadFile(byte[] file, String fileName) throws RemoteException {
        try (FileOutputStream fos = new FileOutputStream(serverDirName+"/"+fileName)) {
            fos.write(file);
            System.out.println("Created new file with name" + fileName);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.out.println("Error uploading file with name "+ fileName);
        }
    }

    @Override
    public void delete(String fileName) throws RemoteException {
        Path path = Paths.get(serverDirName + "/" +fileName);

        try {
            Files.delete(path);
            System.out.println("File deleted successfully!");
        } catch (NoSuchFileException e) {
            System.out.println("No such file exists: " + path);
        } catch (DirectoryNotEmptyException e) {
            System.out.println("Directory is not empty: " + path);
        } catch (IOException e) {
            System.out.println("Unable to delete file: " + path);
            e.printStackTrace();
        }
    }
}