package at.meks.backupclientserver.common.service.health;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(builderMethodName = "anErrorReport")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ErrorReport {

    private Exception exception;

    private String message;
}
