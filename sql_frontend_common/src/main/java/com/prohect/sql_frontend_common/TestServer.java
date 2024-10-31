package com.prohect.sql_frontend_common;

import java.io.IOException;

public class TestServer {
    public static void main(String[] args) throws IOException {
/*        ServerSocket serverSocket = new ServerSocket(19335);
        Socket socket = serverSocket.accept();
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        byte[] originBytes = new byte[1024];
        int index = 0;
        while (inputStream.available() > 0) {
            index += inputStream.read(originBytes);
        }

        int a = 0;
        for (int i = 1; i < 5; i++) {
            a |= ((originBytes[i - 1] & 0xFF) << i * 8);
        }
        byte[] bytes = new byte[a];
        System.arraycopy(originBytes, 4, bytes, 0, bytes.length);
        Packet packet0 = JSONB.parseObject(bytes, Packet.class);
        Packet packet = JSONB.parseObject(bytes, PacketManager.getPacketClassByPrefix(packet0.getPrefix()));
        System.out.printf("packet = %s%n", packet);*/
        new Object();
    }
}
