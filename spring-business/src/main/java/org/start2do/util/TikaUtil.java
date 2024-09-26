package org.start2do.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.apache.tika.Tika;

@UtilityClass
public class TikaUtil {

    @Getter
    private static Tika tika = new Tika();

    public String detect(byte[] data) {
        return tika.detect(data);
    }

    public String detect(String path) {
        return tika.detect(path);
    }

    public String detect(Path path) {
        if (Files.exists(path)) {
            try {
                return tika.detect(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public String detect(File path) {
        try {
            if (path.exists()) {
                return tika.detect(path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
