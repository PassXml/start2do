package org.start2do.ops.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.util.LinkedList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.start2do.util.FileUtil;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.ops", name = "enable")
public class TailService {

    public void tailFile(String filePath, SseEmitter emitter) {

        try {
            Path path = Paths.get(filePath);
            WatchService watchService = FileSystems.getDefault().newWatchService();
            path.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            // 使用带字符编码的文件读取
            List<String> lastLines = readLastLines(filePath, 100);
            for (String line : lastLines) {
                emitter.send(line);
            }

            // 使用 RandomAccessFile 和 BufferedReader 组合来正确处理编码
            RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r");
            FileInputStream fis = new FileInputStream(randomAccessFile.getFD());
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));

            // 移动到文件末尾
            randomAccessFile.seek(randomAccessFile.length());
            FileUtil.watch(filePath, watchEvent -> {
                try {
                    //修改了
                    if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        long len = randomAccessFile.length();
                        if (len < randomAccessFile.getFilePointer()) {
                            // 文件被截断，重新读取
                            randomAccessFile.seek(0);
                            fis.getChannel().position(0);
                            reader.skip(0);
                        } else if (len > randomAccessFile.getFilePointer()) {
                            // 读取新内容
                            String line;
                            while ((line = reader.readLine()) != null) {
                                emitter.send(line);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }, () -> {
                try {
                    reader.close();
                    fis.close();
                    randomAccessFile.close();
                    watchService.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("Failed to tail file: {}", e.getMessage());
            emitter.completeWithError(e);
        }
    }

    private List<String> readLastLines(String filePath, int numLines) throws IOException {
        LinkedList<String> lastLines = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(Files.newInputStream(Paths.get(filePath)), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lastLines.add(line);
                if (lastLines.size() > numLines) {
                    lastLines.removeFirst();
                }
            }
        }
        return lastLines;
    }
}
