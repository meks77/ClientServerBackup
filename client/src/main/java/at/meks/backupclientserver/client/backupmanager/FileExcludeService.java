package at.meks.backupclientserver.client.backupmanager;

import at.meks.backupclientserver.client.ApplicationConfig;
import com.google.inject.Inject;

import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;

class FileExcludeService {

    @Inject
    private ApplicationConfig applicationConfig;

    private Set<String> excludedExtensions;

    @Inject
    void initExcludedExtensions() {
        Set<String> newExtensionsSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        newExtensionsSet.addAll(applicationConfig.getExcludedFileExtensions());
        excludedExtensions = newExtensionsSet;
    }

    boolean isFileExcludedFromBackup(Path path) {
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
