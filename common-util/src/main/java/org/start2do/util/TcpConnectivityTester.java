package org.start2do.util;

import java.io.IOException;
import java.net.Socket;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TcpConnectivityTester {

    /**
     * 测试TCP连通性.
     *
     * @param host 要连接的目标主机名或IP地址
     * @param port 要连接的端口号
     * @return 如果连接成功返回true，否则返回false
     */
    public boolean testConnectivity(String host, int port, Integer timeout) {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new java.net.InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            // 连接尝试失败
            return false;
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // 忽略关闭失败
                }
            }
        }
    }
}
