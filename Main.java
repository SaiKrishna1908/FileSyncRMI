import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class Main {

    private static final String CONFIG_FILE_NAME = "config.yaml";

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            InputStream inputStream = new FileInputStream(new File(CONFIG_FILE_NAME));
            var yaml = new Yaml();
            Map<String, Object> data = yaml.load(inputStream);
            
            String fileServerDir = ((HashMap<String, String>) data.get("server")).get("directory");
            createDirIfNotExists(fileServerDir);

            String fileClientDir = ((HashMap<String, String>) data.get("client")).get("directory");
            createDirIfNotExists(fileClientDir);
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
}
