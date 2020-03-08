package at.meks.backupclientserver.common.service.fileup2date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class FileUp2dateInput extends FileInputArgs {

    String md5Checksum;

}
