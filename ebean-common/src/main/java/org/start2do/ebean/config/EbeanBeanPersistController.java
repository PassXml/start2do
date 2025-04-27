package org.start2do.ebean.config;

import io.ebean.event.BeanDeleteIdRequest;
import io.ebean.event.BeanPersistController;
import io.ebean.event.BeanPersistRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.start2do.ebean.util.EntityHook;
import org.start2do.ebean.util.EntityHookUtil;

@Slf4j
@RequiredArgsConstructor
public class EbeanBeanPersistController implements BeanPersistController {


    @Override
    public int getExecutionOrder() {
        return 0;
    }

    @Override
    public boolean isRegisterFor(Class<?> cls) {
        return EntityHookUtil.get(cls) != null;
    }

    @Override
    public boolean preInsert(BeanPersistRequest<?> request) {
        Object obj = request.bean();
        List<EntityHook> hooks = EntityHookUtil.get(obj.getClass());
        if (hooks != null) {
            for (EntityHook hook : hooks) {
                hook.insertBefore(obj, request.transaction());
            }
        }
        return true;
    }

    @Override
    public boolean preUpdate(BeanPersistRequest<?> request) {
        Object obj = request.bean();
        List<EntityHook> hooks = EntityHookUtil.get(obj.getClass());
        if (hooks != null) {
            for (EntityHook hook : hooks) {
                hook.updateBefore(obj, request.transaction());
            }
        }
        return true;
    }

    @Override
    public boolean preDelete(BeanPersistRequest<?> request) {
        Object obj = request.bean();
        List<EntityHook> hooks = EntityHookUtil.get(obj.getClass());
        if (hooks != null) {
            for (EntityHook hook : hooks) {
                hook.preDelete(obj, request.transaction());
            }
        }
        return true;
    }

    @Override
    public boolean preSoftDelete(BeanPersistRequest<?> request) {
        Object obj = request.bean();
        List<EntityHook> hooks = EntityHookUtil.get(obj.getClass());
        if (hooks != null) {
            for (EntityHook hook : hooks) {
                hook.preSoftDelete(obj, request.transaction());
            }
        }
        return true;
    }

    @Override
    public void preDelete(BeanDeleteIdRequest request) {
        List<EntityHook> hooks = EntityHookUtil.get(request.beanType());
        if (hooks != null) {
            for (EntityHook hook : hooks) {
                hook.preDeleteById(request.id(), request.transaction());
            }
        }
    }

    @Override
    public void postInsert(BeanPersistRequest<?> request) {
        Object obj = request.bean();
        List<EntityHook> hooks = EntityHookUtil.get(obj.getClass());
        if (hooks != null) {
            for (EntityHook hook : hooks) {
                hook.insertAfter(obj, request.transaction());
            }
        }
    }

    @Override
    public void postUpdate(BeanPersistRequest<?> request) {
        Object obj = request.bean();
        List<EntityHook> hooks = EntityHookUtil.get(obj.getClass());
        if (hooks != null) {
            for (EntityHook hook : hooks) {
                hook.updateAfter(obj, request.transaction());
            }
        }
    }

    @Override
    public void postDelete(BeanPersistRequest<?> request) {
        Object obj = request.bean();
        List<EntityHook> hooks = EntityHookUtil.get(obj.getClass());
        if (hooks != null) {
            for (EntityHook hook : hooks) {
                hook.postDelete(obj, request.transaction());
            }
        }
    }

    @Override
    public void postSoftDelete(BeanPersistRequest<?> request) {
        Object obj = request.bean();
        List<EntityHook> hooks = EntityHookUtil.get(obj.getClass());
        if (hooks != null) {
            for (EntityHook hook : hooks) {
                hook.postSoftDelete(obj, request.transaction());
            }
        }
    }

}
