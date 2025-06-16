# 摄像头离线-30分钟

```json
{
  "ruleId": "camera-offline-alert-01",
  "ruleName": "摄像头离线告警",
  "description": "当一个摄像头在30分钟内没有发送心跳事件时触发告警",
  "enabled": true,
  "timeWindowSeconds": 1800,
  "pattern": [
    {
      "stepName": "detectFirstHeartbeat",
      "eventType": "CameraHeartbeat",
      "filters": {},
      "quantifier": "ONE"
    }
  ],
  "action": "CREATE_OFFLINE_ALARM"
}
```
