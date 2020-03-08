package at.meks.backupclientserver.common;

import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;

public class Md5CheckSumGenerator {

    @SneakyThrows
    public String md5HexFor(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fis);
        }
    }

    public String md5HexFor(String text) {
        return DigestUtils.md5Hex(text);
    }
}
