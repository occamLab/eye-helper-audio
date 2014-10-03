package com.eyehelper.positionalaudiocvtesting;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;

public class ServerSocket implements Runnable {

    private Socket socket;
    public boolean connected;
    ImageHandler imageHandler;

    public ServerSocket(ImageHandler imageHandler) {
        this.imageHandler = imageHandler;
    }

    @Override
    public void run() {
        try {
            socket = connect();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            connected = true;
            while(connected) {
                out.write(imageHandler.getImageData());
                out.flush();
            }
        } catch (SocketException e) {
            disconnect();
        } catch (IOException e) {
            disconnect();
            e.printStackTrace();
        }
    }

    private synchronized Socket connect() throws IOException {
        return new Socket(EyeHelper.serverAddress, 9999);
    }

    public synchronized void disconnect() {
        connected = false;
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static interface ImageHandler {
        public char[] getImageData();
    }
}
