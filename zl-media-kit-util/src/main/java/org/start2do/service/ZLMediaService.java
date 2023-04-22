package org.start2do.service;

import org.start2do.dto.req.ZLMediaOnServerKeepaliveReq;
import org.start2do.dto.req.ZLMediaOnStreamNoneReaderReq;
import org.start2do.dto.req.ZLMediaOnStreamNotFoundReq;

public interface ZLMediaService {

    void onStreamNotFound(ZLMediaOnStreamNotFoundReq req);

    void onStreamNoneReader(ZLMediaOnStreamNoneReaderReq req);

    void onServerKeepalive(ZLMediaOnServerKeepaliveReq req);
}
