package me.sorby.googlehome.network;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class TransportConnection {
    private SSLSocket sslsocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public TransportConnection(String ip, int port) {

        try {
            TrustManager[] trustManager = new TrustManager[]{new X509TrustAllCerts()};
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustManager, new java.security.SecureRandom());
            sslsocket = (SSLSocket) sc.getSocketFactory().createSocket(ip, port);
            inputStream = sslsocket.getInputStream();
            outputStream = sslsocket.getOutputStream();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void write(int b) throws IOException {
        outputStream.write(b);
    }

    public void write(byte[] b) throws IOException {
        outputStream.write(b);
    }

    public int read() throws IOException {
        return inputStream.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    public boolean isClosed() {
        return sslsocket.isClosed();
    }

    public void close() {
        try {
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            sslsocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
