package at.meks.backupclientserver.common;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Md5CheckSumGenerator {

    public String md5HexFor(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fis);
        }
    }

    public String md5HexFor(String text) {
        return DigestUtils.md5Hex(text);
    }
}
