package org.start2do;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.start2do.util.TikaUtil;
import org.start2do.util.TikaUtil.FileType;

public class FileHeaderTest {

    @Test
    public void test() throws IOException {
        System.out.println(TikaUtil.detect("S:/1.png"));
        System.out.println(TikaUtil.detect("S:/1.jpg"));
        System.out.println(TikaUtil.detect("S:/1.bmp"));
        System.out.println(TikaUtil.detect("S:/1.webp"));
        TikaUtil.checkFilePathThrow("S:/1.webp",
            List.of(FileType.ApplicationPDF, FileType.ApplicationDOC));
    }
}
