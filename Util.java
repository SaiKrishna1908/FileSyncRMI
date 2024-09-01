import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Util {
    public static String getServerPath(Map<String, Object> data) {
        return ((HashMap<String, String>) data.get("server")).get("path");
    }

    public static String getServerAddr(Map<String, Object> data) {
        return ((HashMap<String, String>) data.get("server")).get("addr");
    }

    public static String getServerDir(Map<String, Object> data) {
        return ((HashMap<String, String>) data.get("server")).get("directory");
    }

    public static Integer getServerPort(Map<String, Object> data) {
        return ((HashMap<String, Integer>) data.get("server")).get("port");
    }

    public static String getClientDir(Map<String, Object> data) {
        return ((HashMap<String, String>) data.get("client")).get("directory");
    } 

    public static Integer getTimeOut(Map<String, Object> data) {
        return ((HashMap<String, Integer>) data.get("client")).get("watchTimeOut");
    }

    public static Integer getComputeServerPort(Map<String, Object> data) {
        return ((HashMap<String, Integer>) data.get("computeServer")).get("port");
    }

    public static String getComputeServerPath(Map<String, Object> data) {
        return ((HashMap<String, String>) data.get("computeServer")).get("path");
    }

    public static String getComputeServerAddr(Map<String, Object> data) {
        return ((HashMap<String, String>) data.get("computeServer")).get("addr");
    }

    public static String extractFileNameFromPath(String path) {
        String chunks[] = path.split("/");
        return chunks[chunks.length-1]; 
    }
}
