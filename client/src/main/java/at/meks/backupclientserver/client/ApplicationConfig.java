package at.meks.backupclientserver.client;

import at.meks.validation.result.ValidationException;
import at.meks.validation.validations.list.ListValidations;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static at.meks.validation.validations.common.CommonValidations.isTrue;
import static at.meks.validation.validations.string.StringValidations.isNotBlank;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@Singleton
public class ApplicationConfig {

    private Properties properties;

    @Inject
    FileService fileService;

    Path[] getBackupedDirs() {
        return getProperties().stringPropertyNames().stream()
                    .filter(s -> s.startsWith("backupset.dir"))
                    .map(properties::get)
                    .map(o -> (String) o)
                    .map(Paths::get)
                    .toArray(Path[]::new);
    }

    public String getServerHost() {
        return getProperties().getProperty("server.host");
    }

    public int getServerPort() {
        return ofNullable(getProperties().getProperty("server.port"))
                .map(Integer::parseInt)
                .orElse(8080);
    }

    private Properties getProperties() {
        if (properties == null) {
            initializeProperties();
        }
        return properties;
    }

    private void initializeProperties() {
        try {
            Properties configProps = new Properties();
            configProps.load(new FileInputStream(fileService.getConfigFile().toFile()));
            properties = configProps;
        } catch (IOException e) {
            throw new ClientBackupException("couldn't read config file", e);
        }
    }

    void validate() throws ValidationException {
        isNotBlank().test(getServerHost()).throwIfInvalid("Config property server.host");
        List<Path> backupedDirs = Arrays.stream(getBackupedDirs()).collect(Collectors.toList());
        ListValidations.<Path>hasMinSize(1).test(backupedDirs).throwIfInvalid("Configured directories for backup");
        for (Path backupedDir : backupedDirs) {
            isTrue().test(backupedDir.toFile().exists())
                    .throwIfInvalid(format("Configured directory %s must exist", backupedDir));
            isTrue().test(backupedDir.toFile().isDirectory())
                    .throwIfInvalid(format("Path %s must be a directory", backupedDir));
        }
    }

    List<String> getPathExcludesForBackup() {
        return getProperties().keySet().stream()
                .map(o -> (String) o)
                .filter(s -> s.startsWith("excludes.exclude"))
                .map(key -> (String) getProperties().get(key))
                .collect(Collectors.toList());
    }

    public Set<String> getExcludedFileExtensions() {
        Optional<String> extensions = ofNullable(getProperties().get("excludes.fileextensions"))
                .map(o -> (String) o);
        return extensions
                .map(s -> Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(extension -> extension.length() > 0)
                .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    public Set<String> getExcludes() {
        return getProperties().stringPropertyNames().stream()
                .filter(s -> s.startsWith("excludes.exclude"))
                .map(properties::get)
                .map(o -> (String) o)
                .collect(Collectors.toSet());
    }
}
