package org.start2do;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;
import org.start2do.util.ExcelUtil;
import org.start2do.util.ExcelUtil.ExcelSetting;

public class ExcelTest {

    @Test
    void test1() throws IOException {
        List<Item> items = List.of(new Item("1", "nihoa", "2","3"), new Item("2", "nihoa1", "3","4"));
        OutputStream outputStream = Files.newOutputStream(Paths.get("S:/1.xlsx"));
        ExcelUtil.write(
            outputStream, Item.class, "GOGO", items
        );
    }


    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {

        @ExcelSetting("编号")
        private String id;
        @ExcelSetting("名称")
        private String name;
        @ExcelSetting(skin = true,value = "value_1")
        private String value;
        private String value1;
    }
}
