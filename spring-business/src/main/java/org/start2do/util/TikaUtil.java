package org.start2do.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.apache.tika.Tika;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.dict.DictItems;
import org.start2do.ebean.dict.IDictItem;

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

    public boolean checkFileContentType(String result, FileType fileType) {
        if (result == null || fileType == null) {
            return false;
        }
        return result.startsWith(fileType.value());
    }

    public void checkFilePathThrow(String fileParth, FileType fileType) {
        if (!checkFileContentType(tika.detect(fileParth), fileType)) {
            throw new RuntimeException("文件格式不正确,期望格式为:" + fileType.label());
        }
    }

    public void checkFilePathThrow(String fileParth, List<FileType> fileType) {
        StringJoiner joiner = new StringJoiner(",");
        for (FileType type : fileType) {
            if (!checkFileContentType(tika.detect(fileParth), type)) {
                joiner.add(type.label());
            }
        }
        if (joiner.length() > 0) {
            throw new RuntimeException("文件格式不正确,期望格式为:" + joiner);
        }
    }

    public boolean checkFilePath(String fileParth, List<FileType> fileType) {
        for (FileType type : fileType) {
            if (checkFileContentType(tika.detect(fileParth), type)) {
                return true;
            }
        }
        return false;
    }

    public void checkFileContentThrow(String context, List<FileType> fileType) {
        StringJoiner joiner = new StringJoiner(",");
        for (FileType type : fileType) {
            if (!checkFileContentType(context, type)) {
                joiner.add(type.label());
            }
        }
        if (joiner.length() > 0) {
            throw new RuntimeException("文件格式不正确,期望格式为:" + joiner);
        }
    }

    public void checkFileContentTypeThrow(String result, FileType fileType) {
        if (!checkFileContentType(result, fileType)) {
            throw new RuntimeException("文件格式不正确,期望格式为:" + fileType.label());
        }
    }

    public enum FileType implements IDictItem {
        Txt("text/", "文本类"),
        Image("image/", "图像类"),
        ImagePNG("image/png", "png"),
        ImageJPEG("image/jpeg", "jpeg"),
        ImageBMP("image/bmp", "bmp"),
        ImageWEBP("image/webp", "webp"),
        Video("video/", "视频类"),
        VideoMp4("video/mp4", "mp4"),
        Application("application/", "应用"),
        ApplicationPDF("application/pdf", "pdf"),
        ApplicationPPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx"),
        ApplicationDOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"),
        ApplicationXlsx("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
        ApplicationExcel("application/vnd.ms-excel", "xls"),
        ApplicationWps("application/vnd.ms-works", "wps"),
        ApplicationOctetStream("application/octet-stream", "et"),
        ApplicationDOC("application/msword", "doc"),
        ApplicationZIP("application/ZIP", "ZIP");

        FileType(String value, String label) {
            putItemBean(value, label);
        }


        public static FileType get(String value) {
            return find(value).orElseThrow(() -> new BusinessException("未知字典值:" + value));
        }

        public static Optional<FileType> find(String value) {
            FileType result = DictItems.getByValue(FileType.class, value);
            if (result == null) {
                return Optional.empty();
            }
            return Optional.of(result);
        }
    }


}
