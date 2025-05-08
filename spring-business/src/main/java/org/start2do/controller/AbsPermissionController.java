package org.start2do.controller;

import java.util.Set;
import org.start2do.dto.permission.PermissionDto;

public interface AbsPermissionController {

    Set<PermissionDto> getAllUrls();
}
