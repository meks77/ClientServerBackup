package at.meks.backupclientserver.common.service.fileup2date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder(builderMethodName = "aFileInputArgs")
@Data
@NoArgsConstructor @AllArgsConstructor
public class FileInputArgs {

    private String hostName;
    private String backupedPath;
    private String[] relativePath;
    private String fileName;
    private ClientId clientId;

}
