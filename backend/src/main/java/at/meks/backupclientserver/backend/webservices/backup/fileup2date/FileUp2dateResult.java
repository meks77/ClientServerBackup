package at.meks.backupclientserver.backend.webservices.backup.fileup2date;

public class FileUp2dateResult {

    private boolean up2date;

    public boolean isUp2date() {
        return up2date;
    }

    public void setUp2date(boolean up2date) {
        this.up2date = up2date;
    }
}
