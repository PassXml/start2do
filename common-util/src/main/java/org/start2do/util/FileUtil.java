package org.start2do.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtil {

    public List<String> getFiles(File file, Predicate<String> predicate) {
        List<String> result = new ArrayList<>();
        if (file.isDirectory()) {//如果是目录
            File[] listFiles = file.listFiles();//获取当前路径下的所有文件和目录,返回File对象数组
            for (File f : listFiles) {//将目录内的内容对象化并遍历
                result.addAll(getFiles(f, predicate));

            }
        } else if (file.isFile()) {//如果是文件
            if (predicate.test(file.getName())) {
                result.add(file.getAbsolutePath());
            }
        }
        return result;
    }

}
