package org.start2do;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class SnmpAlarmDTO {

    /**
     * 线路编号
     */
    private int lineCode;
    /**
     * 系统编号
     */
    private int systemCode;
    /**
     * 是否清除告警 false 正常告警  true 消除告警
     */
    private boolean cleared;
    /**
     * 告警位置名称
     */
    private String alarmManagedObjectInstanceName;
    /**
     * 告警特殊原因
     */
    private String alarmSpecificProblem;
    /**
     * 告警码
     */
    private String emsAlarmCode;
    /**
     * 网元类型
     */
    private String alarmNetype;

    /**
     * 告警时间
     */
    private String alarmTime;

    /**
     * 附加信息
     */
    private List<AlarmMessage> messages;

    public static class AlarmMessage implements Serializable {

        private String title;

        private String content;
    }

}
