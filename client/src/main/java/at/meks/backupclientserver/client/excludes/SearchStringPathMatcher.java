package at.meks.backupclientserver.client.excludes;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

@Singleton
class SearchStringPathMatcher {

    boolean matches(String searchString, Path askedPath) {
        PathMatcher pathMatcher = askedPath.getFileSystem().getPathMatcher("glob:"+searchString);
        return pathMatcher.matches(askedPath);
    }

}
