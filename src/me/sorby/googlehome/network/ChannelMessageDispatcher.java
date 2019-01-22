package me.sorby.googlehome.network;

import extensions.api.cast_channel.CastChannelProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

public class ChannelMessageDispatcher {

    private final static Logger logger = LoggerFactory.getLogger(ChannelMessageDispatcher.class);
    private static HashMap<TransportConnection, ChannelMessageDispatcher> instances = new HashMap<>();
    private HashMap<String, ChannelMessageListener> listenerHashMap = new HashMap<>();
    private TransportConnection socket;

    private ChannelMessageDispatcher(TransportConnection socket) {
        instances.put(socket, this);
        this.socket = socket;
        start();
    }

    //Factory pattern
    public static ChannelMessageDispatcher getInstance(TransportConnection socket) {
        if (instances.containsKey(socket))
            return instances.get(socket);
        return new ChannelMessageDispatcher(socket);
    }

    public void addMessageListener(String destionationId, ChannelMessageListener listener) {
        //We use hasmap strcture, without ArrayList as every L0 message must be received by ONLY ONE L1 virtual channel
        //We identify the virtual channel using his senderId (that appear as destionationId in received messages)
        listenerHashMap.putIfAbsent(destionationId, listener);
    }

    //Observer pattern
    //Dispatch every L0 (socket protobuf) message to the correct L1 virtual channel
    private void dispatch(CastChannelProto.CastMessage msg) {
        if (listenerHashMap.containsKey(msg.getDestinationId()))
            listenerHashMap.get(msg.getDestinationId()).messageReceived(msg);
        else if (msg.getDestinationId().equals("*")) { //broadcast
            HashMap<String, ChannelMessageListener> tmpListenerHashMap = new HashMap<>(listenerHashMap);
            for (ChannelMessageListener l : tmpListenerHashMap.values())
                l.messageReceived(msg);

        } else
            logger.debug("No event listener for destination " + msg.getDestinationId());
    }

    private void start() {
        new Thread(() -> { //on another thread
            while (!socket.isClosed()) { //as long as the socket is open
                try {
                    byte[] buffer = new byte[4]; //First 4 bytes are the packet lenght in little endian format
                    int readCt = 0;
                    while (readCt < buffer.length) {
                        int nextByte = socket.read();
                        if (nextByte == -1)
                            throw new IOException("Socket closed");
                        buffer[readCt++] = (byte) nextByte;
                    }

                    int size = Utils.fromArrayLE(buffer); //Convert size from little endian to big endian int
                    buffer = new byte[size];
                    readCt = 0;
                    while (readCt < size) { //Read until end (size based)
                        int nowRead = 0;
                        nowRead = socket.read(buffer, readCt, buffer.length - readCt);
                        if (nowRead == -1)
                            throw new IOException("Socket closed");
                        readCt += nowRead;
                    }
                    dispatch(CastChannelProto.CastMessage.parseFrom(buffer)); //dispatch parsed protobuf message
                } catch (IOException e) {
                    socket.close();
                    logger.error("IO error: ", e);
                }
            }
        }).start();
    }
}
