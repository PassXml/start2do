package org.start2do.dto.req;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class ZLMediaOnServerKeepaliveReq {

    /**
     * data
     */
    private DataDTO data;
    /**
     * mediaServerId
     */
    private String mediaServerId;

    /**
     * DataDTO
     */
    @NoArgsConstructor
    @Data
    public static class DataDTO {

        /**
         * buffer
         */
        private Integer buffer;
        /**
         * bufferLikeString
         */
        private Integer bufferLikeString;
        /**
         * bufferList
         */
        private Integer bufferList;
        /**
         * bufferRaw
         */
        private Integer bufferRaw;
        /**
         * frame
         */
        private Integer frame;
        /**
         * frameImp
         */
        private Integer frameImp;
        /**
         * mediaSource
         */
        private Integer mediaSource;
        /**
         * multiMediaSourceMuxer
         */
        private Integer multiMediaSourceMuxer;
        /**
         * rtmpPacket
         */
        private Integer rtmpPacket;
        /**
         * rtpPacket
         */
        private Integer rtpPacket;
        /**
         * socket
         */
        private Integer socket;
        /**
         * tcpClient
         */
        private Integer tcpClient;
        /**
         * tcpServer
         */
        private Integer tcpServer;
        /**
         * tcpSession
         */
        private Integer tcpSession;
        /**
         * udpServer
         */
        private Integer udpServer;
        /**
         * udpSession
         */
        private Integer udpSession;
    }
}
