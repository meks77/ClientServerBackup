package at.meks.backupclientserver.client.excludes;

import at.meks.backupclientserver.client.ApplicationConfig;
import com.google.inject.Inject;

import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;

public class FileExcludeService {

    @Inject
    private ApplicationConfig applicationConfig;

    @Inject
    private SearchStringPathMatcher searchStringPathMatcher;

    private Set<String> excludedExtensions;
    private Set<String> excludes;

    @Inject
    void initExcludedExtensions() {
        Set<String> newExtensionsSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        newExtensionsSet.addAll(applicationConfig.getExcludedFileExtensions());
        excludedExtensions = newExtensionsSet;
        excludes = new TreeSet<>();
        excludes.addAll(applicationConfig.getExcludes());
    }

    public boolean isFileExcludedFromBackup(Path path) {
        return isFileExtensionExcluded(path) || isExcludeMatching(path);
    }

    private boolean isExcludeMatching(Path path) {
        return excludes.stream().anyMatch(exclude -> searchStringPathMatcher.matches(exclude, path));
    }

    private boolean isFileExtensionExcluded(Path path) {
        if (path.toFile().isFile()) {
            String fileExtension = getFileExtension(path);
            if (fileExtension != null) {
                return excludedExtensions.contains(fileExtension);
            }
        }
        return false;
    }

    private String getFileExtension(Path path) {
        String fileName = path.getFileName().toString();
        int posOfExtensionSeparator = fileName.lastIndexOf('.');
        if (posOfExtensionSeparator >= 0 && fileName.length() > posOfExtensionSeparator + 1) {
            return fileName.substring(posOfExtensionSeparator + 1);
        }
        return null;
    }

}
