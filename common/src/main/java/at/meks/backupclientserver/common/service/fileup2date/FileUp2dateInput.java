package at.meks.backupclientserver.common.service.fileup2date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class FileUp2dateInput extends FileInputArgs {

    private String md5Checksum;

}
