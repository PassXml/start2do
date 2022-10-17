package org.start2do.dto;

/**
 *
 * @Author Lijie
 * @date 2021/12/15:17:29
 */
@lombok.Getter
@lombok.Setter
public class DataNotFoundException extends RuntimeException {

    public DataNotFoundException() {
        super("找不到数据");
    }
}
