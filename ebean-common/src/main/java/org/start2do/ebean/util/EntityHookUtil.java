package org.start2do.ebean.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class EntityHookUtil {

    private static Map<Class<?>, List<EntityHook>> hooks = new HashMap<>();


    public void addHooks(EntityHook entityHook) {
        Class<?> aClass = entityHook.getKey();
        List<EntityHook> entityHooks = hooks.computeIfAbsent(aClass, k -> new ArrayList<>());
        entityHooks.add(entityHook);
        entityHooks.sort(Comparator.comparingInt(EntityHook::sort));
    }

    public static List<EntityHook> get(Class<?> aClass) {
        List<EntityHook> entityHooks = hooks.get(aClass);
        return entityHooks;
    }


}
