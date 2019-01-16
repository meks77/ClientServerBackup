package at.meks.backupclientserver.client.backupmanager;

import java.nio.file.Path;
import java.nio.file.PathMatcher;

class SearchStringPathMatcher {

    boolean matches(String searchString, Path askedPath) {
        PathMatcher pathMatcher = askedPath.getFileSystem().getPathMatcher("glob:"+searchString);
        return pathMatcher.matches(askedPath);
    }

}
