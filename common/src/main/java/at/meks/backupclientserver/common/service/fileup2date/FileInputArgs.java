package at.meks.backupclientserver.common.service.fileup2date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class FileInputArgs {
    private String hostName;
    private String backupedPath;
    private String[] relativePath;
    private String fileName;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getBackupedPath() {
        return backupedPath;
    }

    public void setBackupedPath(String backupedPath) {
        this.backupedPath = backupedPath;
    }

    public String[] getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String[] relativePath) {
        this.relativePath = relativePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof FileInputArgs)) {
            return false;
        }

        FileInputArgs that = (FileInputArgs) o;
        return new EqualsBuilder()
                .append(hostName, that.hostName)
                .append(backupedPath, that.backupedPath)
                .append(relativePath, that.relativePath)
                .append(fileName, that.fileName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(hostName)
                .append(backupedPath)
                .append(relativePath)
                .append(fileName)
                .toHashCode();
    }
}
