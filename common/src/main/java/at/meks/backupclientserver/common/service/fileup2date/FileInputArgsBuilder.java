package at.meks.backupclientserver.common.service.fileup2date;

public final class FileInputArgsBuilder {
    private String hostName;
    private String backupedPath;
    private String[] relativePath;
    private String fileName;

    private FileInputArgsBuilder() {
    }

    public static FileInputArgsBuilder aFileInputArgs() {
        return new FileInputArgsBuilder();
    }

    public FileInputArgsBuilder withHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    public FileInputArgsBuilder withBackupedPath(String backupedPath) {
        this.backupedPath = backupedPath;
        return this;
    }

    public FileInputArgsBuilder withRelativePath(String[] relativePath) {
        this.relativePath = relativePath;
        return this;
    }

    public FileInputArgsBuilder withFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public FileInputArgs build() {
        FileInputArgs fileInputArgs = new FileInputArgs();
        fileInputArgs.setHostName(hostName);
        fileInputArgs.setBackupedPath(backupedPath);
        fileInputArgs.setRelativePath(relativePath);
        fileInputArgs.setFileName(fileName);
        return fileInputArgs;
    }
}
