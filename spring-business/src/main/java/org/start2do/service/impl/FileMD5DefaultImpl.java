package org.start2do.service.impl;

import org.start2do.service.IFileMd5;
import org.start2do.util.Md5Util;

/**
 * @author Lijie
 */
public class FileMD5DefaultImpl implements IFileMd5 {

    @Override
    public String md5(byte[] bytes) {
        return Md5Util.md5(bytes);
    }
}
