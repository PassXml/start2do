package org.start2do.service.impl;

import java.io.File;
import org.start2do.entity.business.SysFile;
import org.start2do.service.IFileOperationHookService;

public class FileOperationHookServiceEmptyImp implements IFileOperationHookService {

    @Override
    public File uploadBefore(File localFile) {
        return null;
    }

    @Override
    public byte[] uploadBefore(byte[] localFileBytes) {
        return new byte[0];
    }

    @Override
    public void uploadAfter(File localFile, SysFile file) {

    }

    @Override
    public void uploadAfter(byte[] localFileBytes, SysFile file) {

    }
}
