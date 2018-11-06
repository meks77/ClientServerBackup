package at.meks.backupclientserver.common.service.fileup2date;

public class FileUp2dateInput {

    private String hostName;
    private String backupedPath;
    private String relativePath;
    private String fileName;
    private String md5Checksum;

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

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMd5Checksum() {
        return md5Checksum;
    }

    public void setMd5Checksum(String md5Checksum) {
        this.md5Checksum = md5Checksum;
    }
}
