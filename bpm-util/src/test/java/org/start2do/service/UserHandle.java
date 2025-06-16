package org.start2do.service;

import java.util.List;
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
    public List<String> getDeptCodes() {
        return List.of();
    }

    @Override
    public List<String> getPositionCodes() {
        return List.of("1");
    }

    @Override
    public String getCurrentUserId() {
        return "1";
    }

    @Override
    public String getUserHandler(String username) {
        return "管理员";
    }
}
