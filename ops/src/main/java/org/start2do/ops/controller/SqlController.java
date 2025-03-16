package org.start2do.ops.controller;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.concurrent.ForkJoinPool;
import javax.sql.DataSource;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.start2do.ops.dto.sql.SqlQueryReq;
import org.start2do.util.spring.SpringBeanUtil;

@Slf4j
@Controller
@RequestMapping("sql")
@ConditionalOnProperty(prefix = "start2do.ops", name = "enable")
public class SqlController {

    /**
     * 执行sql
     */
    @PostMapping({"/", ""})
    public SseEmitter deploy(@Valid @RequestBody SqlQueryReq req) {
        SseEmitter emitter = new SseEmitter(-1L); // 无超时
        ForkJoinPool.commonPool().submit(() -> {
            try {
                DataSource bean = SpringBeanUtil.getBean(DataSource.class);
                CallableStatement callableStatement = bean.getConnection().prepareCall(req.getSql());
                //使用Map封装返回
                ResultSet query = callableStatement.executeQuery();
                int i = 0;
                ResultSetMetaData metaData = query.getMetaData();
                while (query.next()) {
                    if (i == 0) {
                        String[] headers = new String[metaData.getColumnCount()];
                        for (int j = 0; j < metaData.getColumnCount(); j++) {
                            headers[j] = metaData.getColumnName(j);
                        }
                        i = 1;
                        emitter.send(String.join(",", headers));
                    }
                    String[] objects = new String[metaData.getColumnCount()];

                    for (int j = 0; j < metaData.getColumnCount(); j++) {
                        objects[j] = query.getString(i + 1);
                    }
                    emitter.send(String.join(",", objects));
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                emitter.complete();
            }
        });
        return emitter;
    }

}
