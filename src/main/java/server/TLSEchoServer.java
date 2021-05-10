
package de.fhac.rn;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TLSEchoServer {
    public static void main(String[] args) throws IOException {

        System.setProperty("javax.net.ssl.keyStore","src/main/resources/de/fhac/rn/rn-ssl.jks");
        System.setProperty("javax.net.ssl.keyStorePassword","geheim");
        System.out.println("server starts");

        ServerSocket sslServer= SSLServerSocketFactory.getDefault().createServerSocket(5001);
        Socket sslSocket=sslServer.accept();
       var dataInputStream = new DataInputStream(sslSocket.getInputStream());
        var dataOutputStream = new DataOutputStream(sslSocket.getOutputStream());
        while (true) {
            dataOutputStream.writeUTF(dataInputStream.readUTF().toUpperCase());
            System.out.println("Received message and send back");
        }
    }
}

