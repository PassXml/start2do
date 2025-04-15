package org.start2do.service;


import java.io.File;
import org.start2do.entity.business.SysFile;

public interface IFileOperationHookService {

    File uploadBefore(File localFile);

    byte[] uploadBefore(byte[] localFileBytes);

    void uploadAfter(File localFile, SysFile file);


    void uploadAfter(byte[] localFileBytes, SysFile file);
}
