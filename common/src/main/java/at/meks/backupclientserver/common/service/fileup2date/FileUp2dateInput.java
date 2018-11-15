package at.meks.backupclientserver.common.service.fileup2date;

public class FileUp2dateInput extends FileInputArgs {

    private String md5Checksum;

    public String getMd5Checksum() {
        return md5Checksum;
    }

    public void setMd5Checksum(String md5Checksum) {
        this.md5Checksum = md5Checksum;
    }
}
