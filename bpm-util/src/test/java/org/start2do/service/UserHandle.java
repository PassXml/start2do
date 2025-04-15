package org.start2do.service;

import org.springframework.stereotype.Service;
import org.start2do.bpm.service.IUserHandle;

@Service
public class UserHandle implements IUserHandle {

    @Override
    public String getCurrentUsername() {
        return "admin";
    }

    @Override
    public String getCurrentUserRealName() {
        return "管理员";
    }

    @Override
    public String getCurrentUserId() {
        return "1";
    }
}
