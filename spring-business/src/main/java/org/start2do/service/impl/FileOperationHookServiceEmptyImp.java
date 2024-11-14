package org.start2do.service.impl;

import java.io.File;
import org.start2do.entity.business.SysFile;
import org.start2do.service.webflux.IFileOperationHookService;

public class FileOperationHookServiceEmptyImp implements IFileOperationHookService {

    @Override
    public void uploadAfter(File localFile, SysFile file) {

    }

    @Override
    public void uploadAfter(byte[] localFileBytes, SysFile file) {

    }
}
