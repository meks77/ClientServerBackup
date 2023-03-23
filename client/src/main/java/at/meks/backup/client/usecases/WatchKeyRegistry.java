package at.meks.backup.client.usecases;

import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.HashMap;
import java.util.Map;

public class WatchKeyRegistry {

    Map<WatchKey, Path> registry = new HashMap<>();

    Path directory(WatchKey watchKey) {
        return registry.get(watchKey);
    }

    void add(WatchKey watchKey, Path directory) {
        registry.put(watchKey, directory);
    }

}
