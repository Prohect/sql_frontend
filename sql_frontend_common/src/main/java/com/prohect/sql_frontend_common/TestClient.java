package com.prohect.sql_frontend_common;

import java.io.IOException;

public class TestClient {
    public static void main(String[] args) throws IOException {
/*        SInfoPacket infoPacket = new SInfoPacket("hello world");
        byte[] jsonBytes = JSONB.toBytes(infoPacket);//lengthBytes = 51
        byte[] packetBytes = new byte[jsonBytes.length + 4];
        byte[] lengthBytes = new byte[4];
        for (int i = 1; i < 5; i++) {
            lengthBytes[i - 1] = (byte) (jsonBytes.length >> i * 8);
        }


        //The components at positions srcPos through srcPos+length-1 in the source array are copied into positions destPos through destPos+length-1, respectively, of the destination array.
        System.arraycopy(lengthBytes, 0, packetBytes, 0, 4);
        System.arraycopy(jsonBytes, 0, packetBytes, 4, jsonBytes.length);


        Socket socket = new Socket("127.0.0.1", 19335);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(packetBytes);
        outputStream.flush();*/
        new Object();
    }
}
