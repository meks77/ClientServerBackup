package at.meks.backupclientserver.client.backupmanager;

import org.junit.Test;

import java.nio.file.Paths;

import static org.fest.assertions.api.Assertions.assertThat;

public class SearchStringPathMatcherTest {

    private SearchStringPathMatcher pathMatcher = new SearchStringPathMatcher();

    @Test
    public void givenOneStarAsWholeDirectoryWhenPathMatchesReturnsTrue() {
        assertThat(pathMatcher.matches("C:/Users/*/Downloads", Paths.get("C:", "Users", "user1", "Downloads")))
                .isTrue();
    }

    @Test
    public void givenOneStarAsWholeDirectoryWhenPathContainsMoreDirsReturnsFalse() {
        assertThat(pathMatcher.matches("C:/Users/*/Downloads", Paths.get("C:", "Users", "ldap", "user1", "Downloads")))
                .isFalse();
    }

    @Test
    public void givenOneStarAsWholeDirectoryWhenPathNoDirForStartReturnsFalse() {
        assertThat(pathMatcher.matches("C:/Users/*/Downloads", Paths.get("C:", "Users", "Downloads")))
                .isFalse();
    }

    @Test
    public void given2StarsWhenPathWith2DirsMatchesReturnsTrue() {
        assertThat(pathMatcher.matches("C:/**/Downloads", Paths.get("C:", "Users", "user1", "Downloads")))
                .isTrue();
    }

    @Test
    public void given2StarsWhenPathWith1DirMatchesReturnsTrue() {
        assertThat(pathMatcher.matches("C:/**/Downloads", Paths.get("C:", "Users", "Downloads")))
                .isTrue();
    }

    @Test
    public void given2StarsWhenPathNotMatchesReturnsFalse() {
        assertThat(pathMatcher.matches("C:/**/Downloads", Paths.get("C:", "Users", "user1", "Download")))
                .isFalse();
    }

    @Test
    public void giventRepeatedDoubleStarAndSingleStarWhenPathMatchesReturnsTrue() {
        assertThat(pathMatcher.matches("C:/**/FireFox/**/cache/*",
                    Paths.get("C:", "Users", "user1", "AppData", "Firefox", "runtime", "cache", "xyz.txt")))
                .isTrue();
    }

    @Test
    public void giventRepeatedDoubleStarAndSingleStarWhenPathNotMatchesReturnsFalse() {
        assertThat(pathMatcher.matches("C:/**/FireFox/**/cache/*",
                    Paths.get("C:", "Users", "user1", "AppData", "Firefox", "runtime", "cached", "xyz.txt")))
                .isFalse();
    }

    @Test
    public void givenRepeatedSingleStarWhenPathMatchesThenReturnTrue() {
        assertThat(pathMatcher.matches("C:/Users/*/Eigene Dateien/*/tmp",
                    Paths.get("C:", "Users", "user1", "Eigene Dateien", "AppData", "tmp")))
                .isTrue();
    }

    @Test
    public void givenRepeatedSingleStarWhenPathNotMatchesThenReturnFalse() {
        assertThat(pathMatcher.matches("C:/Users/*/Documents/*/tmp",
                    Paths.get("C:", "Users", "user1", "Eigene Dateien", "AppData", "tmp")))
                .isFalse();
    }

    @Test
    public void givenDirDoubleStarAndFilePartStarWhenPathMatchesThenReturnsTrue() {
        assertThat(pathMatcher.matches("C:/Users/**/*tmp___",
                    Paths.get("C:", "Users", "user1", "tomcat", "435098345345tmp___")))
                .isTrue();
    }

    @Test
    public void givenDirDoubleStarAndFilePartStarWhenPathNotMatchesThenReturnsFalse() {
        assertThat(pathMatcher.matches("C:/Users/**/*tmp___",
                    Paths.get("C:", "Users", "user1", "tomcat", "435098345345mp___")))
                .isFalse();
    }

    @Test
    public void givenRecusiveExcdludeWhenMatchesThenReturnsTrue() {
        assertThat(pathMatcher.matches("C:/tmp/**", Paths.get("C:", "tmp", "pictures", "apciture.jpg"))).isTrue();
    }

    @Test
    public void givenRecusiveExcdludeWhenNotMatchesThenReturnsFalse() {
        assertThat(pathMatcher.matches("C:/tmp/**", Paths.get("C:", "temp", "pictures", "apciture.jpg"))).isFalse();
    }

    @Test
    public void givenExcludedFileNameWhenMatchesThenReturnsTrue() {
        assertThat(pathMatcher.matches("**/excludedFile.txt", Paths.get("C:", "Users", "excludedFile.txt"))).isTrue();
    }

    @Test
    public void givenExcludedFileNameWhenNotMatchesThenReturnsFalse() {
        assertThat(pathMatcher.matches("**/excludedFile.txt", Paths.get("C:", "Users", "excludedFile.t"))).isFalse();
    }

    @Test
    public void givenExcludedFileWithAbsolutePathWhenMatchesThenReturnsTrue() {
        assertThat(pathMatcher.matches("C:/Users/LaraCroft/exclude.me",
                Paths.get("C:", "Users", "LaraCroft", "exclude.me"))).isTrue();
    }

    @Test
    public void givenExcludedFileWithAbsolutePathWhenNotMatchesThenReturnsFalse() {
        assertThat(pathMatcher.matches("C:/Users/LaraCroft/exclude.me",
                Paths.get("C:", "Users", "LaraCroft", "exclude.m"))).isFalse();
    }

}