package me.sorby.googlehome.network;

import extensions.api.cast_channel.CastChannelProto;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class CastChannel implements ChannelMessageListener {
    private final static Logger logger = LoggerFactory.getLogger(CastChannel.class);
    private TransportConnection socket;
    private String sourceId;
    private String destinationId;
    private String name;
    private Timer pingTimer;

    public CastChannel(TransportConnection socket, String sourceId, String destinationId) {
        this.socket = socket;
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.name = sourceId;

        ChannelMessageDispatcher.getInstance(socket).addMessageListener(sourceId, this);
    }

    public CastChannel(TransportConnection socket, String destinationId) {
        this(socket, "sender-" + Utils.randomString(8), destinationId);
    }

    public CastChannel(TransportConnection socket) {
        this(socket, "receiver-0");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private void send(CastChannelProto.CastMessage msg) {
        try {
            byte[] bmsg = Arrays.copyOf(Utils.toArrayLE(msg.getSerializedSize()), msg.toByteArray().length + 4);
            System.arraycopy(msg.toByteArray(), 0, bmsg, 4, msg.toByteArray().length);
            socket.write(bmsg);
        } catch (IOException e) {
            logger.error("IO error: ", e);
        }

    }

    public void send(String namespace, JSONObject payload) {
        CastChannelProto.CastMessage msg = CastChannelProto.CastMessage.newBuilder()
                .setProtocolVersion(CastChannelProto.CastMessage.ProtocolVersion.CASTV2_1_0)
                .setSourceId(sourceId)
                .setDestinationId(destinationId)
                .setPayloadType(CastChannelProto.CastMessage.PayloadType.STRING)
                .setNamespace(namespace)
                .setPayloadUtf8(payload.toJSONString())
                .build();
        send(msg);
    }

    public void openVirtualConnection() {
        JSONObject connectPayload = new JSONObject();
        connectPayload.put("type", "CONNECT");
        send("urn:x-cast:com.google.cast.tp.connection", connectPayload);
    }

    public void closeVirtualConnection() {
        JSONObject connectPayload = new JSONObject();
        connectPayload.put("type", "CLOSE");
        send("urn:x-cast:com.google.cast.tp.connection", connectPayload);
        if (pingTimer != null)
            pingTimer.cancel();
    }

    //keep-alive pinger on heartbeat namespace (only one for each transport connection)
    public void keepAlive() {
        pingTimer = new Timer();
        pingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                JSONObject connectPayload = new JSONObject();
                connectPayload.put("type", "PING");
                send("urn:x-cast:com.google.cast.tp.heartbeat", connectPayload);
            }
        }, 0, 5000); //repeat every 5000ms
    }

    //Conveninence method for adding Contextual message listener (L2)
    public void addMessageListener(String namespace, ContextualChannelMessageListener listener) {
        ContextualChannelMessageDispatcher.getInstance(this).addMessageListener(namespace, listener);
    }

    @Override
    public void messageReceived(CastChannelProto.CastMessage msg) {
        try {
            JSONObject msgJSON = (JSONObject) new JSONParser().parse(msg.getPayloadUtf8());
            //Send every virtual channel (L1) to the the contextual dispatcher (L2)
            ContextualChannelMessageDispatcher.getInstance(this).dispatch(msg.getNamespace(), msgJSON);
        } catch (ParseException e) {
            logger.error("ParseException: ", e);
        }
    }
}
