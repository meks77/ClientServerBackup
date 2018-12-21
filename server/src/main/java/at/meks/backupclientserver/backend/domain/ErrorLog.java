package at.meks.backupclientserver.backend.domain;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Document(collection = "errorLogs", schemaVersion = "1.0")
@Builder(builderMethodName = "anErrorLog")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ErrorLog {

    @Id
    private String errorFilePath;

    private String hostName;

    private String errorMessage;

    private LocalDateTime errorTimestamp;

}
