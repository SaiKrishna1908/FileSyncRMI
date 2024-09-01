import java.nio.file.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DirectoryWatcher {

    private final String directoryPath;
    private final Integer timeOut;

    public DirectoryWatcher(String directoryPath, int timeout) {
        this.directoryPath = directoryPath;
        this.timeOut = timeout;
    }

    public List<WatchEvent<?>> watch() {
        // Directory to watch
        Path path = Paths.get(directoryPath);

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            // Register the directory with the watch service for creation, deletion, and
            // modification events
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            System.out.println("Watching directory: " + path);

            // Wait for key to be signaled (polling every 5 seconds)
            WatchKey key = watchService.poll(timeOut, TimeUnit.SECONDS);

            if (key != null) {
                List<WatchEvent<?>> events = new ArrayList<>(key.pollEvents());

                // Reset the key to receive further events
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
