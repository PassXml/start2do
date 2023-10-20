/*
 * Copyright (c) 2020 ibms4cloud Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.start2do.dto;

import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 响应信息主体
 *
 * @param <T>
 * @author lengleng
 */
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class R<T> implements Serializable,Cloneable {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private int code;

    @Getter
    @Setter
    private String msg;

    @Getter
    @Setter
    private T data;

    public static <T> R<T> ok() {
        return new R(0, null, "");
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, 0, null);
    }

    public static <T> R<T> ok(T data, String msg) {
        return restResult(data, 0, msg);
    }

    public static <T> R<T> failed() {
        return restResult(null, 1, null);
    }

    public static <T> R<T> failed(String msg) {
        return restResult(null, 1, msg);
    }

    public static <T> R<T> failed(T data) {
        return restResult(data, 1, null);
    }

    public static <T> R<T> failed(T data, String msg) {
        return restResult(data, 1, msg);
    }

    public static <T> R<T> failed(Integer code, String msg) {
        return restResult(null, code, msg);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private static <T> R<T> restResult(T data, int code, String msg) {
        R<T> apiResult = new R<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }

    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public String toJson() {
        return "{" + "\"code\":" + code + ", \"msg\":\"" + msg + "\"" + ",\"data\":\"" + data + "\"" + '}';
    }

}
