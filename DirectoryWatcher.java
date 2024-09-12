import java.nio.file.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Utility class which performs periodic scan on a directory for file changes
 */
public class DirectoryWatcher {

    private final String directoryPath;
    private final Integer timeOut;

    public DirectoryWatcher(String directoryPath, int timeout) {
        this.directoryPath = directoryPath;
        this.timeOut = timeout;
    }

    public List<WatchEvent<?>> watch() {
        Path path = Paths.get(directoryPath);

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            System.out.println("Watching directory: " + path);

            WatchKey key = watchService.poll(timeOut, TimeUnit.SECONDS);

            if (key != null) {
                List<WatchEvent<?>> events = new ArrayList<>(key.pollEvents());

                boolean valid = key.reset();
                if (!valid) {
                    return null;
                }

                return events;
            } 

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}
