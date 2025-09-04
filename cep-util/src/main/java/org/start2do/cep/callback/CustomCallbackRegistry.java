package org.start2do.cep.callback;

import org.start2do.cep.dto.CEPResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomCallbackRegistry {
    private static final Logger log = LoggerFactory.getLogger(CustomCallbackRegistry.class);
    private static final Map<String, CEPCallback> callbackMap = new ConcurrentHashMap<>();

    public static void registerCallback(String callbackType, CEPCallback callback) {
        callbackMap.put(callbackType, callback);
        log.info("Registered custom callback: {}", callbackType);
    }

    public static void unregisterCallback(String callbackType) {
        callbackMap.remove(callbackType);
        log.info("Unregistered custom callback: {}", callbackType);
    }

    public static CEPCallback getCallback(String callbackType) {
        return callbackMap.get(callbackType);
    }

    public static boolean hasCallback(String callbackType) {
        return callbackMap.containsKey(callbackType);
    }

    public static void executeCallback(String callbackType, CEPResult result) {
        CEPCallback callback = callbackMap.get(callbackType);
        if (callback != null) {
            try {
                callback.execute(result);
                log.debug("Executed callback {} for rule {}", callbackType, result.getRuleId());
            } catch (Exception e) {
                log.error("Failed to execute callback {} for rule {}", callbackType, result.getRuleId(), e);
            }
        } else {
            log.warn("Callback {} not found for rule {}", callbackType, result.getRuleId());
        }
    }
}