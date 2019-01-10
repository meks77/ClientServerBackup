package at.meks.backupclientserver.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.NumberFormat;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Singleton
class ConfigFileInitializer {

    @Inject
    private SystemService systemService;

    private NumberFormat nf = NumberFormat.getIntegerInstance();

    void initializeConfigFile(Path configFile) {
        try {
            int excludeCounter = 0;
            Properties defaultExcludeProps = getDefaultExcludeProps();

            for (String excludeKey : getRelevantExcludesKeys(defaultExcludeProps)) {
                Files.write(configFile, getExcludeEntry(excludeCounter, defaultExcludeProps, excludeKey).getBytes(),
                        StandardOpenOption.APPEND);
                excludeCounter++;
            }
        } catch (IOException e) {
            throw new ClientBackupException(e);
        }
    }

    private Properties getDefaultExcludeProps() throws IOException {
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("default-excludes.properties"));
        return props;
    }

    private List<String> getRelevantExcludesKeys(Properties props) {
        return props.keySet().stream()
                .map(o -> (String) o)
                .filter(this::isExcludeForCurrentOs)
                .sorted()
                .collect(Collectors.toList());
    }

    private boolean isExcludeForCurrentOs(String key) {
        String additionalFilter = null;
        if (systemService.isOsWindows()) {
            additionalFilter = "excludes.windows.exclude";
        }
        return (key.startsWith("excludes.all.exclude")) ||
                ofNullable(additionalFilter)
                        .map(key::startsWith).orElse(false);
    }

    private String getExcludeEntry(int excludeCounter, Properties defaultExcludeProps, String excludeKey) {
        return "excludes.exclude" + nf.format(excludeCounter) + " = " + defaultExcludeProps.getProperty(excludeKey) +
                System.lineSeparator();
    }
}
