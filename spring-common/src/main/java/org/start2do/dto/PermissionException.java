package org.start2do.dto;

/**
 * @Author Lijie
 * @date 2021/12/15:17:29
 */
@lombok.Getter
@lombok.Setter
public class PermissionException extends RuntimeException {

    public PermissionException() {
        super("没有操作权限");
    }
}
