package org.start2do.cep.callback;

import org.start2do.cep.dto.CEPResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpCallback implements CEPCallback {
    private static final Logger log = LoggerFactory.getLogger(HttpCallback.class);
    private final String callbackUrl;
    private final String httpMethod;
    private final Map<String, String> headers;

    public HttpCallback(String callbackUrl, String httpMethod, Map<String, String> headers) {
        this.callbackUrl = callbackUrl;
        this.httpMethod = httpMethod;
        this.headers = headers;
    }

    @Override
    public void execute(CEPResult result) {
        try {
            String jsonPayload = convertResultToJson(result);
            
            if ("POST".equalsIgnoreCase(httpMethod)) {
                sendHttpPostRequest(callbackUrl, jsonPayload);
            } else if ("GET".equalsIgnoreCase(httpMethod)) {
                sendHttpGetRequest(callbackUrl);
            } else {
                log.warn("Unsupported HTTP method: {}", httpMethod);
            }
            
        } catch (Exception e) {
            log.error("HTTP callback failed for rule: " + result.getRuleId(), e);
        }
    }

    private String convertResultToJson(CEPResult result) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"ruleId\":\"").append(result.getRuleId()).append("\",");
        json.append("\"ruleName\":\"").append(result.getRuleName()).append("\",");
        json.append("\"description\":\"").append(result.getDescription()).append("\",");
        json.append("\"matchedAt\":").append(result.getMatchedAt()).append(",");
        json.append("\"matchedEvents\":[");
        
        if (result.getMatchedEvents() != null && !result.getMatchedEvents().isEmpty()) {
            for (int i = 0; i < result.getMatchedEvents().size(); i++) {
                if (i > 0) json.append(",");
                json.append("{\"eventId\":\"").append(result.getMatchedEvents().get(i).getEventId()).append("\",");
                json.append("\"eventType\":\"").append(result.getMatchedEvents().get(i).getEventType()).append("\",");
                json.append("\"source\":\"").append(result.getMatchedEvents().get(i).getSource()).append("\"}");
            }
        }
        
        json.append("]}");
        return json.toString();
    }

    private void sendHttpPostRequest(String url, String jsonPayload) {
        log.info("Sending HTTP POST to {} with payload: {}", url, jsonPayload);
    }

    private void sendHttpGetRequest(String url) {
        log.info("Sending HTTP GET to {}", url);
    }
}